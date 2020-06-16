package ch.pete.appconfigapp.configdetail

interface ConfigDetailView {
    fun close()
    fun showNameAuthorityFragment(configId: Long)
    fun showKeyValuesFragment(configId: Long, readonly: Boolean)
}
