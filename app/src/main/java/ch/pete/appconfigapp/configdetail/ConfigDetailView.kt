package ch.pete.appconfigapp.configdetail

interface ConfigDetailView {
    fun close()
    fun showNameAuthorityFragment(configId: Long, newItem: Boolean)
    fun showKeyValuesFragment(configId: Long, readonly: Boolean)
}
