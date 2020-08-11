package ch.pete.appconfigapp.api

import ch.pete.appconfigapp.api.model.ExternalConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException


class ExternalConfigLocationService {
    private lateinit var externalConfigLocationApi: ExternalConfigLocationApi

    fun init() {
        val mapper =
            ObjectMapper(YAMLFactory())
                .registerModule(KotlinModule())

        val retrofit = Retrofit.Builder()
            // baseUrl must be set and end with /
            .baseUrl("https://pete.ch/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        externalConfigLocationApi = retrofit.create(ExternalConfigLocationApi::class.java)
    }

    @Throws(IOException::class)
    suspend fun fetchCentalConfigConfig(url: String): List<ExternalConfig> =
        externalConfigLocationApi.fetchExternalConfigs(url)
}
