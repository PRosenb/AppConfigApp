package ch.pete.appconfigapp.keyvalue

interface KeyValueView {
    fun showKeyValueDetails(configId: Long, keyValueId: Long?)
}