package ch.pete.appconfigapp.configdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.Config
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
        if (configId != null) {
            this.configId = configId
        } else {
            view.close()
        }
    }

    fun config(): LiveData<Config> =
        appConfigDao.fetchConfigById(configId)

    fun keyValueEntriesByConfigId(configId: Long) =
        appConfigDao.keyValueEntriesByConfigId(configId)

    fun executionResultEntries() =
        appConfigDao.fetchExecutionResultEntriesByConfigId(configId)

    fun onNewItem() =
        view.showNameAuthorityFragment(configId, true)

    fun onEditNameAuthorityClicked() =
        view.showNameAuthorityFragment(configId, false)

    fun onEditKeyValueClicked(config: Config) {
        config.id?.let {
            view.showKeyValuesFragment(it, config.readonly)
        } ?: Timber.e("config.id null")
    }

    fun onDetailExecuteClicked() {
        viewModelScope.launch {
            mainActivityViewModel.callContentProvider(configId)
        }
    }
}
