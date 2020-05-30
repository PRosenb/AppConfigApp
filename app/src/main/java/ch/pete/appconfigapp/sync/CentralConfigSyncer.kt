package ch.pete.appconfigapp.sync

import ch.pete.appconfigapp.api.ApiConfigEntry
import ch.pete.appconfigapp.api.CentralConfigService
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.CentralConfig
import ch.pete.appconfigapp.model.Config
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar

class CentralConfigSyncer(private val appConfigDao: AppConfigDao) {
    private val apiService = CentralConfigService()

    suspend fun init() {
        withContext(Dispatchers.IO) {
            apiService.init()
        }
    }

    suspend fun sync() {
        withContext(Dispatchers.IO) {
            val centralConfigs = appConfigDao.centralConfigsSuspend()

            centralConfigs
                .filter { it.enabled }
                .forEach {
                    sync(it)
                }
        }
    }

    private suspend fun sync(centralConfig: CentralConfig) {
        try {
            if (centralConfig.id == null) {
                Timber.e("centralConfig.id null")
                return
            }

            val apiConfigEntries =
                fetchAndPreprocessCentralConfigEntries(centralConfig)

            val receivedCentralConfigExternalIds =
                apiConfigEntries.mapNotNull { it.centralConfigId }.toSet()

            val existingConfigs =
                appConfigDao.fetchConfigByCentralConfigId(centralConfig.id)

            val deletedConfigs = existingConfigs
                .filter { !receivedCentralConfigExternalIds.contains(it.centralConfigExternalId) }
            val deletedConfigIds = deletedConfigs.mapNotNull { it.id }
            appConfigDao.deleteConfigs(deletedConfigIds)

            apiConfigEntries.forEach { apiConfigEntry ->
                val existingConfig =
                    existingConfigs.find { it.centralConfigExternalId == apiConfigEntry.centralConfigId }
                if (existingConfig != null) {
                    updateConfig(apiConfigEntry, existingConfig)
                } else {
                    insertConfig(apiConfigEntry, centralConfig.id)
                }
            }
        } catch (e: MismatchedInputException) {
            Timber.e("Could not fetch central config", e)
        }
    }

    private suspend fun fetchAndPreprocessCentralConfigEntries(centralConfig: CentralConfig):
            List<ApiConfigEntry> =
        apiService.fetchConfig(centralConfig.url)
            .map { unchangedApiConfigEntry ->
                var apiConfigEntry = unchangedApiConfigEntry

                apiConfigEntry = if (apiConfigEntry.creationTimestamp == null) {
                    apiConfigEntry.copy(
                        creationTimestamp = Calendar.getInstance()
                    )
                } else {
                    apiConfigEntry
                }

                apiConfigEntry = if (apiConfigEntry.centralConfigId == null) {
                    apiConfigEntry.copy(centralConfigId = apiConfigEntry.name)
                } else {
                    apiConfigEntry
                }

                apiConfigEntry
            }

    private suspend fun insertConfig(apiConfigEntry: ApiConfigEntry, centralConfigId: Long) {
        appConfigDao.insertConfigWithKeyValues(
            config = Config(
                name = apiConfigEntry.name,
                authority = apiConfigEntry.authority,
                creationTimestamp = apiConfigEntry.creationTimestamp
                    ?: Calendar.getInstance(),
                centralConfigExternalId = apiConfigEntry.centralConfigId,
                centralConfigId = centralConfigId
            ),
            keyValues = apiConfigEntry.keyValues
        )
    }

    private suspend fun updateConfig(apiConfigEntry: ApiConfigEntry, existingConfig: Config) {
        val config = if (apiConfigEntry.creationTimestamp != null) {
            existingConfig.copy(
                name = apiConfigEntry.name,
                authority = apiConfigEntry.authority,
                creationTimestamp = apiConfigEntry.creationTimestamp
            )
        } else {
            existingConfig.copy(
                name = apiConfigEntry.name,
                authority = apiConfigEntry.authority
            )
        }
        appConfigDao.updateConfigWithKeyValues(
            config = config,
            keyValues = apiConfigEntry.keyValues
        )
    }

}