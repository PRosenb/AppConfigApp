package ch.pete.appconfigapp.sync

import android.database.sqlite.SQLiteConstraintException
import ch.pete.appconfigapp.api.ExternalConfigLocationService
import ch.pete.appconfigapp.api.model.ExternalConfig
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.model.ExternalConfigLocation
import ch.pete.appconfigapp.model.KeyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.Calendar

class ExternalConfigsSyncer(
    private val appConfigDao: AppConfigDao,
    private val externalConfigLocationService: ExternalConfigLocationService = ExternalConfigLocationService()
) {
    companion object {
        const val DEFAULT_SORT = 9999L
    }

    suspend fun init() {
        withContext(Dispatchers.IO) {
            externalConfigLocationService.init()
        }
    }

    /**
     * @return the total amount of ExternalConfig items synced
     */
    suspend fun sync(): Int =
        withContext(Dispatchers.IO) {
            val externalConfigLocations = appConfigDao.externalConfigLocationsSuspend()

            externalConfigLocations
                .map {
                    sync(it)
                }
                .sum()
        }

    private suspend fun sync(externalConfigLocation: ExternalConfigLocation): Int {
        if (externalConfigLocation.id == null) {
            Timber.e("externalConfigLocation.id null")
            return 0
        }
        return try {
            val apiConfigEntries =
                if (externalConfigLocation.enabled) {
                    fetchAndPreprocessExternalConfigLocationEntries(externalConfigLocation)
                } else emptyList()

            val receivedExternalConfigLocationIds =
                apiConfigEntries.mapNotNull { it.id }.toSet()

            val existingConfigs =
                appConfigDao.fetchConfigByExternalConfigLocationId(externalConfigLocation.id)

            val deletedConfigs = existingConfigs
                .filter { !receivedExternalConfigLocationIds.contains(it.externalConfigId) }
            val deletedConfigIds = deletedConfigs.mapNotNull { it.id }
            appConfigDao.deleteConfigs(deletedConfigIds)

            apiConfigEntries.forEach { apiConfigEntry ->
                val existingConfig =
                    existingConfigs.find { it.externalConfigId == apiConfigEntry.id }
                if (existingConfig != null) {
                    updateConfig(apiConfigEntry, existingConfig)
                } else {
                    insertConfig(apiConfigEntry, externalConfigLocation.id)
                }
            }
            apiConfigEntries.size
        } catch (e: HttpException) {
            Timber.e(e, "Could not fetch external config location '%s'", externalConfigLocation)
            0
        } catch (e: IOException) {
            Timber.e(e, "Could not fetch external config location '%s'", externalConfigLocation)
            0
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Could not fetch external config location '%s'", externalConfigLocation)
            0
        } catch (e: SQLiteConstraintException) {
            Timber.e(
                e,
                "Could not sync external config location '%s' because it was deleted.",
                externalConfigLocation
            )
            0
        }
    }

    private suspend fun fetchAndPreprocessExternalConfigLocationEntries(externalConfigLocation: ExternalConfigLocation):
            List<ExternalConfig> =
        externalConfigLocationService.fetchCentalConfigConfig(externalConfigLocation.url)
            .map { unchangedApiConfigEntry ->
                var apiConfigEntry = unchangedApiConfigEntry

                apiConfigEntry = if (apiConfigEntry.creationTimestamp == null) {
                    apiConfigEntry.copy(
                        creationTimestamp = Calendar.getInstance()
                    )
                } else {
                    apiConfigEntry
                }

                apiConfigEntry = if (apiConfigEntry.id == null) {
                    apiConfigEntry.copy(id = apiConfigEntry.name)
                } else {
                    apiConfigEntry
                }

                apiConfigEntry
            }

    private suspend fun insertConfig(
        externalConfig: ExternalConfig,
        externalConfigLocationId: Long
    ) {
        appConfigDao.insertConfigWithKeyValues(
            config = Config(
                name = externalConfig.name,
                authority = externalConfig.authority,
                sort = externalConfig.sort ?: DEFAULT_SORT,
                creationTimestamp = externalConfig.creationTimestamp
                    ?: Calendar.getInstance(),
                externalConfigId = externalConfig.id,
                externalConfigLocationId = externalConfigLocationId
            ),
            keyValues = externalConfig.keyValues.map {
                // configId is overwritten with the id config gets
                KeyValue(configId = -1, key = it.key, value = it.value)
            }
        )
    }

    private suspend fun updateConfig(externalConfig: ExternalConfig, existingConfig: Config) {
        val config = if (externalConfig.creationTimestamp != null) {
            existingConfig.copy(
                name = externalConfig.name,
                authority = externalConfig.authority,
                sort = externalConfig.sort ?: DEFAULT_SORT,
                creationTimestamp = externalConfig.creationTimestamp
            )
        } else {
            existingConfig.copy(
                name = externalConfig.name,
                authority = externalConfig.authority,
                sort = externalConfig.sort ?: DEFAULT_SORT
            )
        }
        if (config.id != null) {
            appConfigDao.updateConfigWithKeyValues(
                config = config,
                keyValues = externalConfig.keyValues.map {
                    KeyValue(configId = config.id, key = it.key, value = it.value)
                }
            )
        } else {
            // should not happen
            Timber.e("config.id is null, insert instead.")
            appConfigDao.insertConfigWithKeyValues(
                config = config,
                keyValues = externalConfig.keyValues.map {
                    KeyValue(configId = -1, key = it.key, value = it.value)
                }
            )
        }
    }
}
