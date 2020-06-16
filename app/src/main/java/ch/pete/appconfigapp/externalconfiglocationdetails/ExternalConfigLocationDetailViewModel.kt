package ch.pete.appconfigapp.externalconfiglocationdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import kotlinx.coroutines.launch

class ExternalConfigLocationDetailViewModel : ViewModel() {
    lateinit var view: ExternalConfigLocationDetailView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var externalConfigLocationId: Long = MainActivityViewModel.UNSET
    val initialised
        get() = externalConfigLocationId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(externalConfigLocationId: Long?) {
        if (externalConfigLocationId != null) {
            this.externalConfigLocationId = externalConfigLocationId
        } else {
            view.close()
        }
    }

    fun externalConfigLocation() =
        appConfigDao.externalConfigLocationById(externalConfigLocationId)

    fun storeExternalConfigLocation(name: String, url: String) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.updateExternalConfigLocation(name, url, externalConfigLocationId)
        }
    }
}
