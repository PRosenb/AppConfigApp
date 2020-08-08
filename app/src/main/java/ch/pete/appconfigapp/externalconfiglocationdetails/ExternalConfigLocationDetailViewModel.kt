package ch.pete.appconfigapp.externalconfiglocationdetails

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

class ExternalConfigLocationDetailViewModel(application: Application) :
    AndroidViewModel(application) {
    lateinit var view: ExternalConfigLocationDetailView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var externalConfigLocationId: Long = MainActivityViewModel.UNSET
    val initialised
        get() = externalConfigLocationId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(externalConfigLocationId: Long?) {
        logEvent("onInitExternalConfigDetails")
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
            if (name.isNotBlank() || url.isNotBlank()) {
                appConfigDao.updateExternalConfigLocation(name, url, externalConfigLocationId)
            } else {
                appConfigDao.deleteExternalConfigLocation(externalConfigLocationId)
            }
        }
    }

    private fun logEvent(eventName: String) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "ExternalConfigLocationDetailViewModel")
                putLong("externalConfigLocationId", externalConfigLocationId)
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
