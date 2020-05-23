package ch.pete.appconfigapp.nameauthority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.pete.appconfigapp.MainActivityViewModel
import ch.pete.appconfigapp.db.AppConfigDao
import kotlinx.coroutines.launch

class NameAuthorityViewModel : ViewModel() {
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val appConfigDao: AppConfigDao by lazy {
        mainActivityViewModel.appConfigDatabase.appConfigDao()
    }

    fun configByKeyValueId(keyValueId: Long) =
        appConfigDao.fetchConfigById(keyValueId)

    fun storeNameAndAuthority(name: String, authority: String, configId: Long) {
        mainActivityViewModel.viewModelScope.launch {
            appConfigDao.updateConfigNameAndAuthority(name, authority, configId)
        }
    }
}
