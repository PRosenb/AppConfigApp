package ch.pete.appconfigapp.centralconfig

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.CentralConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CentralConfigViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: CentralConfigView
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun onAddCentralConfigClicked() {
        viewModelScope.launch {
            val centralConfigId = withContext(Dispatchers.IO) {
                appConfigDao.insertCentralConfig()
            }
            view.showCentralConfigDetailFragment(centralConfigId)
        }
    }

    fun onCentralConfigEntryClicked(centralConfig: CentralConfig) {
        centralConfig.id?.let {
            view.showCentralConfigDetailFragment(it)
        } ?: throw IllegalArgumentException("config.id is null")
    }

    fun centralConfigs() =
        appConfigDao.centralConfigs()

    fun deleteCentralConfig(centralConfig: CentralConfig) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.deleteCentralConfig(centralConfig)
        }
    }

    fun onPause() {
        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.syncCentralConfig()
        }
    }
}
