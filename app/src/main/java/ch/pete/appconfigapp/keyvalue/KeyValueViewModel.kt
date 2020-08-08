package ch.pete.appconfigapp.keyvalue

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.KeyValue
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

class KeyValueViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: KeyValueView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = MainActivityViewModel.UNSET
    val initialised
        get() = configId != MainActivityViewModel.UNSET

    var readOnly = false
        private set

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?, readOnly: Boolean?) {
        logEvent("onInitKeyValue", null)
        if (configId != null) {
            this.configId = configId
            this.readOnly = readOnly ?: false
            keyValueEntriesByConfigId().observe(view, Observer {
                if (it.isEmpty()) {
                    view.showEmptyView()
                } else {
                    view.hideEmptyView()
                }
            })
        } else {
            view.close()
        }
    }

    fun keyValueEntriesByConfigId() =
        appConfigDao.keyValueEntriesByConfigId(configId)

    fun onAddKeyValueClicked() {
        logEvent("onAddKeyValue", null)
        view.showKeyValueDetails(configId, null)
    }

    fun onKeyValueEntryClicked(keyValue: KeyValue) {
        logEvent("onShowKeyValueDetails", keyValue.id)
        view.showKeyValueDetails(configId, keyValue.id)
    }

    fun onKeyValueDeleteClicked(keyValue: KeyValue) {
        logEvent("onDeleteKeyValue", keyValue.id)
        viewModelScope.launch {
            appConfigDao.deleteKeyValue(keyValue)
        }
    }

    private fun logEvent(eventName: String, keyValueId: Long?) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "KeyValueViewModel")
                putLong("configId", configId)
                keyValueId?.let { putLong("keyValueId", it) }
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
