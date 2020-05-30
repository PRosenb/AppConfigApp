package ch.pete.appconfigapp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class CentralConfigService {
    private lateinit var api: Api

    fun init() {
        val mapper =
            ObjectMapper(YAMLFactory())
                .findAndRegisterModules()

        val retrofit = Retrofit.Builder()
            // baseUrl must be set and end with /
            .baseUrl("https://pete.ch/")
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        api = retrofit.create(Api::class.java)
    }

    suspend fun fetchConfig(url: String): List<ApiConfigEntry> =
        api.fetchConfig(url)
}
