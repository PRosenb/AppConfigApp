package ch.pete.appconfigapp.api

import ch.pete.appconfigapp.model.KeyValue
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.Calendar

data class ApiConfigEntry(
    val centralConfigId: String?,
    val name: String,
    val authority: String,
    val creationTimestamp: Calendar?,

    val keyValues: List<KeyValue>
)

interface Api {
    @GET
    suspend fun fetchConfig(@Url url: String): List<ApiConfigEntry>
}
