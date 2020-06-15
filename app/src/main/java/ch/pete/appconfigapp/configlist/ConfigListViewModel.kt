package ch.pete.appconfigapp.configlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.R
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.ConfigEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar


class ConfigListViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: ConfigListView
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }
    val configEntries: LiveData<List<ConfigEntry>> by lazy {
        appConfigDao.fetchConfigEntries()
    }

    fun init() {
        configEntries.observe(view, Observer {
            if (it.isEmpty()) {
                view.showEmptyView()
            } else {
                view.hideEmptyView()
            }
        })
    }

    fun onAddConfigClicked() {
        viewModelScope.launch {
            val configId = withContext(Dispatchers.IO) {
                appConfigDao.insertEmptyConfig(Calendar.getInstance())
            }
            view.showDetailsOfNewItem(configId)
        }
    }

    fun onConfigEntryClicked(configEntry: ConfigEntry) {
        configEntry.config.id?.let {
            view.showDetails(it)
        } ?: throw IllegalArgumentException("config.id is null")
    }

    fun onConfigEntryCloneClicked(configEntry: ConfigEntry) {
        viewModelScope.launch {
            appConfigDao.cloneConfigEntryWithoutResultsAndExternalConfigLocation(
                configEntry,
                String.format(
                    getApplication<Application>().getString(R.string.cloned_name),
                    configEntry.config.name
                )
            )
        }
    }

    fun onConfigEntryDeleteClicked(configEntry: ConfigEntry) {
        viewModelScope.launch {
            appConfigDao.deleteConfigEntry(configEntry)
        }
    }

    fun onExecuteClicked(configEntry: ConfigEntry) {
        viewModelScope.launch {
            configEntry.config.id?.let {
                mainActivityViewModel.callContentProvider(it)
            } ?: Timber.e("configEntry.config.id is null")
        }
    }
}
