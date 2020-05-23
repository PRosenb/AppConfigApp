package ch.pete.appconfigapp.configdetail

interface ConfigDetailView {
    fun showNameAuthorityFragment(configId: Long)
    fun showKeyValuesFragment(configId: Long)
}
