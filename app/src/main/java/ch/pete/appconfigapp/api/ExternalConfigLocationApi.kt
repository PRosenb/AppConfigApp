package ch.pete.appconfigapp.api

import ch.pete.appconfigapp.model.KeyValue
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.Calendar

data class ExternalConfig(
    val id: String? = null,
    val name: String,
    val authority: String,
    val creationTimestamp: Calendar? = Calendar.getInstance(),

    val keyValues: List<KeyValue> = emptyList()
)

interface ExternalConfigLocationApi {
    @GET
    suspend fun fetchExternalConfigs(@Url url: String): List<ExternalConfig>
}
