package ch.pete.appconfigapp.keyvaluedetails

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.KeyValue
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

class KeyValueDialogViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: KeyValueDialogView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = MainActivityViewModel.UNSET
    private var keyValueId: Long? = null
    val initialised
        get() = configId != MainActivityViewModel.UNSET
                && keyValueId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?, keyValueId: Long?) {
        logEvent("onInitKeyValueDetails")
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
            val hasValidData =
                keyValue.key.isNotBlank()
                        || (keyValue.value != null && keyValue.value.isNotBlank())
            if (keyValue.id == null) {
                if (hasValidData) {
                    appConfigDao.insertKeyValue(keyValue)
                }
            } else if (!hasValidData) {
                appConfigDao.deleteKeyValue(keyValue)
            } else {
                appConfigDao.updateKeyValue(keyValue)
            }
        }
    }

    private fun logEvent(eventName: String) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "KeyValueDialogViewModel")
                putLong("configId", configId)
                keyValueId?.let { putLong("keyValueId", it) }
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
