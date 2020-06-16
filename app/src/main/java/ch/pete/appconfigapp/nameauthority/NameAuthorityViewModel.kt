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
    val initialised
        get() = configId != MainActivityViewModel.UNSET

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun init(configId: Long?) {
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
            appConfigDao.updateConfigNameAndAuthority(name, authority, configId)
        }
    }
}
