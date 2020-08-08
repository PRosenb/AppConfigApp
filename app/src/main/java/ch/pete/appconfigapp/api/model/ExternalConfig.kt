package ch.pete.appconfigapp.api.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Calendar

data class ExternalKeyValue(
    val key: String,
    val value: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalConfig(
    val id: String? = null,
    val name: String,
    val authority: String,
    val sort: Long?,
    val creationTimestamp: Calendar? = Calendar.getInstance(),

    val keyValues: List<ExternalKeyValue> = emptyList()
)
