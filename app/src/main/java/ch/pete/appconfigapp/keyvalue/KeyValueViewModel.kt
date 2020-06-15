package ch.pete.appconfigapp.keyvalue

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.KeyValue
import kotlinx.coroutines.launch

class KeyValueViewModel : ViewModel() {
    companion object {
        private const val UNSET = -1L
    }

    lateinit var view: KeyValueView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = UNSET
    val initialised
        get() = configId != UNSET

    var readonly = false

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?) {
        if (configId != null) {
            this.configId = configId
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
        view.showKeyValueDetails(configId, null)
    }

    fun onKeyValueEntryClicked(keyValue: KeyValue) {
        view.showKeyValueDetails(configId, keyValue.id)
    }

    fun onKeyValueDeleteClicked(keyValue: KeyValue) {
        viewModelScope.launch {
            appConfigDao.deleteKeyValue(keyValue)
        }
    }
}
