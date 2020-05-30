package ch.pete.appconfigapp.keyvalue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.KeyValue
import kotlinx.coroutines.launch

class KeyValueViewModel : ViewModel() {
    lateinit var view: KeyValueView
    lateinit var mainActivityViewModel: MainActivityViewModel

    var readonly = false

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun keyValueEntriesByConfigId(configId: Long) =
        appConfigDao.keyValueEntriesByConfigId(configId)

    fun onAddKeyValueClicked(configId: Long) {
        view.showKeyValueDetails(configId, null)
    }

    fun onKeyValueEntryClicked(keyValue: KeyValue) {
        view.showKeyValueDetails(keyValue.configId, keyValue.id)
    }

    fun onKeyValueDeleteClicked(keyValue: KeyValue) {
        viewModelScope.launch {
            appConfigDao.deleteKeyValue(keyValue)
        }
    }
}
