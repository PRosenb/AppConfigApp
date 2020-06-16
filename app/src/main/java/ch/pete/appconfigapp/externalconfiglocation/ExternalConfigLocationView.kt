package ch.pete.appconfigapp.externalconfiglocation

import androidx.lifecycle.LifecycleOwner

interface ExternalConfigLocationView : LifecycleOwner {
    fun close()
    fun showEmptyView()
    fun hideEmptyView()
    fun showExternalConfigLocationDetailFragment(externalConfigLocationId: Long)
}
