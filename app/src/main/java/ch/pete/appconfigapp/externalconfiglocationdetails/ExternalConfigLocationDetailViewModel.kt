package ch.pete.appconfigapp.externalconfiglocationdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import kotlinx.coroutines.launch

class ExternalConfigLocationDetailViewModel : ViewModel() {
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun externalConfigLocationById(keyValueId: Long) =
        appConfigDao.externalConfigLocationById(keyValueId)

    fun storeExternalConfigLocation(name: String, url: String, id: Long) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.updateExternalConfigLocation(name, url, id)
        }
    }
}
