package ch.pete.appconfigapp.configdetail

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.Config
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import timber.log.Timber

class ConfigDetailViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: ConfigDetailView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = MainActivityViewModel.UNSET
    val initialised
        get() = configId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?) {
        logEvent("onInitConfigDetails")
        if (configId != null) {
            this.configId = configId
        } else {
            view.close()
        }
    }

    fun onCreateView() {
        val configLiveData = config()
        configLiveData.observe(view, object : Observer<Config> {
            override fun onChanged(config: Config?) {
                if (config != null) {
                    configLiveData.removeObserver(this)
                    view.initViewWithConfig(config)
                } else {
                    Timber.w("config is null, close fragment")
                    view.close()
                }
            }
        })
    }

    fun config(): LiveData<Config> =
        appConfigDao.fetchConfigById(configId)

    fun keyValueEntriesByConfigId(configId: Long) =
        appConfigDao.keyValueEntriesByConfigId(configId)

    fun executionResultEntries() =
        appConfigDao.fetchExecutionResultEntriesByConfigId(configId)

    fun onAddConfig() {
        logEvent("onAddConfig")
        view.showNameAuthorityFragment(configId, true)
    }

    fun onEditNameAuthorityClicked() {
        logEvent("onEditNameAuthority")
        view.showNameAuthorityFragment(configId, false)
    }

    fun onEditKeyValueClicked(config: Config) {
        logEvent("onEditKeyValue")
        config.id?.let {
            view.showKeyValuesFragment(it, config.readonly)
        } ?: Timber.e("config.id null")
    }

    fun onDetailExecuteClicked() {
        viewModelScope.launch {
            logEvent("onExecuteOnConfigDetails")
            mainActivityViewModel.callContentProvider(configId)
        }
    }

    private fun logEvent(eventName: String) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "ConfigDetailViewModel")
                putLong("configId", configId)
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
