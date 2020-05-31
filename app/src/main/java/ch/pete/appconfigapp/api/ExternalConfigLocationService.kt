package ch.pete.appconfigapp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class ExternalConfigLocationService {
    private lateinit var externalConfigLocationApi: ExternalConfigLocationApi

    fun init() {
        val mapper =
            ObjectMapper(YAMLFactory())
                .findAndRegisterModules()

        val retrofit = Retrofit.Builder()
            // baseUrl must be set and end with /
            .baseUrl("https://pete.ch/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        externalConfigLocationApi = retrofit.create(ExternalConfigLocationApi::class.java)
    }

    suspend fun fetchCentalConfigConfig(url: String): List<ExternalConfig> =
        externalConfigLocationApi.fetchExternalConfigs(url)
}
