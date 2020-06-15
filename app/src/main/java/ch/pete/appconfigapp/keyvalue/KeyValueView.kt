package ch.pete.appconfigapp.keyvalue

import androidx.lifecycle.LifecycleOwner

interface KeyValueView : LifecycleOwner {
    fun close()
    fun showEmptyView()
    fun hideEmptyView()
    fun showKeyValueDetails(configId: Long, keyValueId: Long?)
}
