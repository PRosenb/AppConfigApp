package ch.pete.appconfigapp

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.db.DatabaseBuilder
import ch.pete.appconfigapp.model.ConfigEntry
import ch.pete.appconfigapp.model.ExecutionResult
import ch.pete.appconfigapp.model.ResultType
import ch.pete.appconfigapp.sync.ExternalConfigsSyncer
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val UNSET = -1L
    }
    lateinit var view: MainActivityView

    val appConfigDatabase = DatabaseBuilder.builder(application).build()
    private val appConfigDao = appConfigDatabase.appConfigDao()
    val externalConfigsSyncer = ExternalConfigsSyncer(appConfigDao)

    fun init() {
        viewModelScope.launch {
            externalConfigsSyncer.init()
            externalConfigsSyncer.sync()
        }
    }

    fun onMenuExternalConfigLocation() {
        logEvent("onMenuExternalConfig")
        view.showExternalConfigLocation()
    }

    fun onMenuSync() {
        logEvent("onMenuSync")
        viewModelScope.launch {
            val syncedItemsCount = externalConfigsSyncer.sync()
            if (syncedItemsCount == 0) {
                view.showSnackbar(
                    getApplication<Application>().getString(R.string.no_external_config_location)
                )
            }
        }
    }

    suspend fun callContentProvider(configId: Long) {
        val foundItem = withContext(Dispatchers.IO) {
            val configEntry = appConfigDao.fetchConfigEntryById(configId)
            if (configEntry != null) {
                callContentProviderAndStoreResult(configEntry)
                true
            } else {
                Timber.e("ConfigEntry with id '%s' not found.", configId)
                false
            }
        }
        if (!foundItem) {
            Toast.makeText(getApplication(), R.string.error_occurred, Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun callContentProviderAndStoreResult(configEntry: ConfigEntry) {
        val contentValues = configEntry.keyValues
            .fold(ContentValues()) { contentValues, keyValue ->
                contentValues.put(keyValue.key, keyValue.value)
                contentValues
            }

        val authorityUri = Uri.parse("content://${configEntry.config.authority}")
        try {
            val appliedValuesCount = getApplication<Application>().contentResolver.update(
                authorityUri,
                contentValues,
                null,
                null
            )

            addExecutionResult(
                configEntry,
                resultType = ResultType.SUCCESS,
                valuesCount = appliedValuesCount
            )
        } catch (e: SecurityException) {
            addExecutionResult(
                configEntry = configEntry,
                resultType = ResultType.ACCESS_DENIED
            )
        } catch (e: RuntimeException) {
            addExecutionResult(
                configEntry = configEntry,
                resultType = ResultType.EXCEPTION,
                message = e.message
            )
        }
    }

    private suspend fun addExecutionResult(
        configEntry: ConfigEntry,
        resultType: ResultType,
        valuesCount: Int = 0,
        message: String? = null
    ) {
        val executionResult = ExecutionResult(
            configId = configEntry.config.id
                ?: throw IllegalArgumentException("config.id must not be null"),
            resultType = resultType,
            valuesCount = valuesCount,
            message = message
        )
        appConfigDao.insertExecutionResult(executionResult)
    }

    private fun logEvent(eventName: String) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "MainActivityViewModel")
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
