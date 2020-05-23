package ch.pete.appconfigapp.configdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.Config
import kotlinx.coroutines.launch

class ConfigDetailViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: ConfigDetailView
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun configById(configId: Long): LiveData<Config> =
        appConfigDao.fetchConfigById(configId)

    fun keyValueEntriesByConfigId(configId: Long) =
        appConfigDao.keyValueEntriesByConfigId(configId)

    fun executionResultEntriesByConfigId(configId: Long) =
        appConfigDao.fetchExecutionResultEntriesByConfigId(configId)

    fun onNewItem(configId: Long) =
        view.showNameAuthorityFragment(configId)

    fun onEditNameAuthorityClicked(configId: Long) {
        view.showNameAuthorityFragment(configId)
    }

    fun onEditKeyValueClicked(configId: Long) {
        view.showKeyValuesFragment(configId)
    }

    fun onDetailExecuteClicked(configId: Long) {
        viewModelScope.launch {
            mainActivityViewModel.callContentProvider(configId)
        }
    }
}
