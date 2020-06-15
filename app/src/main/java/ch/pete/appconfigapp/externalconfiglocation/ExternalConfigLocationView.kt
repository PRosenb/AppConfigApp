package ch.pete.appconfigapp.externalconfiglocation

import androidx.lifecycle.LifecycleOwner

interface ExternalConfigLocationView : LifecycleOwner {
    fun showEmptyView()
    fun hideEmptyView()
    fun showExternalConfigLocationDetailFragment(externalConfigLocationId: Long)
}
