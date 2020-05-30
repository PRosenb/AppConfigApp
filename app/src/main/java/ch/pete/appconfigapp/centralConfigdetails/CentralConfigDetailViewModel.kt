package ch.pete.appconfigapp.centralConfigdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import kotlinx.coroutines.launch

class CentralConfigDetailViewModel : ViewModel() {
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun centralConfigByKeyValueId(keyValueId: Long) =
        appConfigDao.centralConfigById(keyValueId)

    fun storeCentralConfig(name: String, url: String, id: Long) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.updateCentralConfig(name, url, id)
        }
    }
}
