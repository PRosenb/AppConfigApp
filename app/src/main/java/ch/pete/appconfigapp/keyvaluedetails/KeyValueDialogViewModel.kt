package ch.pete.appconfigapp.keyvaluedetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.KeyValue
import kotlinx.coroutines.launch

class KeyValueDialogViewModel : ViewModel() {
    lateinit var view: KeyValueDialogView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = MainActivityViewModel.UNSET
    private var keyValueId: Long? = null
    val initialised
        get() = keyValueId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?, keyValueId: Long?) {
        if (configId != null) {
            this.configId = configId
            this.keyValueId = keyValueId
        } else {
            view.close()
        }
    }

    fun onOkClicked(key: String, value: String, isNullChecked: Boolean) {
        val keyValue = KeyValue(
            id = keyValueId,
            configId = configId,
            key = key,
            value = if (isNullChecked) {
                null
            } else {
                value
            }
        )
        storeKeyValue(keyValue)
        view.close()
    }

    fun keyValueEntry() =
        keyValueId?.let {
            appConfigDao.keyValueEntryByKeyValueId(it)
        } ?: MutableLiveData()

    private fun storeKeyValue(keyValue: KeyValue) {
        viewModelScope.launch {
            if (keyValue.id == null) {
                appConfigDao.insertKeyValue(keyValue)
            } else {
                appConfigDao.updateKeyValue(keyValue)
            }
        }
    }
}
