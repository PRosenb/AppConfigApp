package ch.pete.appconfigapp.configdetail

import androidx.lifecycle.LifecycleOwner
import ch.pete.appconfigapp.model.Config

interface ConfigDetailView : LifecycleOwner {
    fun initViewWithConfig(config: Config)
    fun close()
    fun showNameAuthorityFragment(configId: Long, newItem: Boolean)
    fun showKeyValuesFragment(configId: Long, readonly: Boolean)
}
