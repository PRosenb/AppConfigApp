package ch.pete.appconfigapp.nameauthority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import kotlinx.coroutines.launch

class NameAuthorityViewModel : ViewModel() {
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
}
