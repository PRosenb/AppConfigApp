package ch.pete.appconfigapp.configlist

import androidx.lifecycle.LifecycleOwner

interface ConfigListView : LifecycleOwner {
    fun showEmptyView()
    fun hideEmptyView()
    fun showDetailsOfNewItem(configId: Long)
    fun showDetails(configId: Long)
}
