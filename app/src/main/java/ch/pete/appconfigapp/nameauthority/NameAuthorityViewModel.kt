package ch.pete.appconfigapp.nameauthority

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

class NameAuthorityViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var view: NameAuthorityView
    lateinit var mainActivityViewModel: MainActivityViewModel
    private var configId: Long = MainActivityViewModel.UNSET
    private var newItem = false
    val initialised
        get() = configId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?, newItem: Boolean?) {
        logEvent("onInitNameAuthority")
        this.newItem = newItem ?: false
        if (configId != null) {
            this.configId = configId
        } else {
            view.close()
        }
    }

    fun config() =
        appConfigDao.fetchConfigById(configId)

    fun storeNameAndAuthority(name: String, authority: String) {
        mainActivityViewModel.viewModelScope.launch {
            if (newItem && name.isBlank() && authority.isBlank()) {
                appConfigDao.deleteConfig(configId)
            } else {
                appConfigDao.updateConfigNameAndAuthority(name, authority, configId)
            }
        }
    }

    private fun logEvent(eventName: String) {
        val params = Bundle()
            .apply {
                putString("ViewModel", "NameAuthorityViewModel")
                putLong("configId", configId)
                putBoolean("newItem", newItem)
            }
        FirebaseAnalytics.getInstance(getApplication()).logEvent(eventName, params)
    }
}
