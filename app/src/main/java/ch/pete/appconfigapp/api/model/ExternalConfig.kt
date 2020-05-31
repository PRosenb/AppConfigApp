package ch.pete.appconfigapp.api.model

import java.util.Calendar

data class ExternalKeyValue(
    val key: String,
    val value: String?
)

data class ExternalConfig(
    val id: String? = null,
    val name: String,
    val authority: String,
    val creationTimestamp: Calendar? = Calendar.getInstance(),

    val keyValues: List<ExternalKeyValue> = emptyList()
)
