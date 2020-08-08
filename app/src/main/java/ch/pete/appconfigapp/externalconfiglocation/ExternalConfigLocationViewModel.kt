package ch.pete.appconfigapp.externalconfiglocation

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.ExternalConfigLocation
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExternalConfigLocationViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: ExternalConfigLocationView
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init() {
        logEvent("onInitExternalConfig", null)
        externalConfigLocations().observe(view, Observer {
            if (it.isEmpty()) {
                view.showEmptyView()
            } else {
                view.hideEmptyView()
            }
        })
    }

    fun onAddExternalConfigLocationClicked() {
        logEvent("onAddExternalConfig", null)
        viewModelScope.launch {
            val externalConfigLocationId = withContext(Dispatchers.IO) {
                appConfigDao.insertExternalConfigLocation()
            }
            view.showExternalConfigLocationDetailFragment(externalConfigLocationId)
        }
    }

    fun onExternalConfigLocationEntryClicked(externalConfigLocation: ExternalConfigLocation) {
        logEvent("onShowExternalConfigDetails", externalConfigLocation.id)
        externalConfigLocation.id?.let {
            view.showExternalConfigLocationDetailFragment(it)
        } ?: throw IllegalArgumentException("config.id is null")
    }

    fun externalConfigLocations() =
        appConfigDao.externalConfigLocations()

    fun onDeleteExternalConfigLocation(externalConfigLocationId: Long) {
        logEvent("onDeleteExternalConfig", externalConfigLocationId)
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.deleteExternalConfigLocation(externalConfigLocationId)
        }
    }

    fun onPause() {
        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.externalConfigsSyncer.sync()
        }
    }

    private fun logEvent(eventName: String, externalConfigLocationId: Long?) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "ExternalConfigLocationViewModel")
                externalConfigLocationId?.let { putLong("externalConfigLocationId", it) }
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
