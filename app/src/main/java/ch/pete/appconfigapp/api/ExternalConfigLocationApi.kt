package ch.pete.appconfigapp.api

import ch.pete.appconfigapp.api.model.ExternalConfig
import retrofit2.http.GET
import retrofit2.http.Url

interface ExternalConfigLocationApi {
    @GET
    suspend fun fetchExternalConfigs(@Url url: String): List<ExternalConfig>
}
