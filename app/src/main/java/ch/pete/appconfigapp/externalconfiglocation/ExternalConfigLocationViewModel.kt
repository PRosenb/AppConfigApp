package ch.pete.appconfigapp.externalconfiglocation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.ExternalConfigLocation
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
        externalConfigLocations().observe(view, Observer {
            if (it.isEmpty()) {
                view.showEmptyView()
            } else {
                view.hideEmptyView()
            }
        })
    }

    fun onAddExternalConfigLocationClicked() {
        viewModelScope.launch {
            val externalConfigLocationId = withContext(Dispatchers.IO) {
                appConfigDao.insertExternalConfigLocation()
            }
            view.showExternalConfigLocationDetailFragment(externalConfigLocationId)
        }
    }

    fun onExternalConfigLocationEntryClicked(externalConfigLocation: ExternalConfigLocation) {
        externalConfigLocation.id?.let {
            view.showExternalConfigLocationDetailFragment(it)
        } ?: throw IllegalArgumentException("config.id is null")
    }

    fun externalConfigLocations() =
        appConfigDao.externalConfigLocations()

    fun deleteExternalConfigLocation(externalConfigLocationId: Long) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.deleteExternalConfigLocation(externalConfigLocationId)
        }
    }

    fun onPause() {
        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.externalConfigsSyncer.sync()
        }
    }
}
