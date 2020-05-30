package ch.pete.appconfigapp.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Calendar

data class ConfigEntry(
    @Embedded
    val config: Config,

    @Relation(
        parentColumn = "id",
        entityColumn = "configId"
    )
    val keyValues: List<KeyValue> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "configId"
    )
    val executionResults: List<ExecutionResult> = emptyList()
)

@Entity(
    tableName = "config", foreignKeys = [
        ForeignKey(
            entity = CentralConfig::class,
            parentColumns = ["id"],
            childColumns = ["centralConfigId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["centralConfigId"])
    ]
)
data class Config(
    // 0L means not set
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val authority: String,
    val creationTimestamp: Calendar = Calendar.getInstance(),
    val centralConfigExternalId: String? = null,
    val centralConfigId: Long? = null,
    @ColumnInfo(defaultValue = "0")
    val sort: Long = 0
)

@Entity(
    tableName = "key_value",
    foreignKeys = [
        ForeignKey(
            entity = Config::class,
            parentColumns = ["id"],
            childColumns = ["configId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["configId"])
    ]
)
data class KeyValue(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val configId: Long,
    val key: String,
    val value: String?
)

@Entity(
    tableName = "execution_result",
    foreignKeys = [
        ForeignKey(
            entity = Config::class,
            parentColumns = ["id"],
            childColumns = ["configId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["configId"])
    ]
)
data class ExecutionResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val configId: Long,
    val timestamp: Calendar = Calendar.getInstance(),
    val resultType: ResultType,
    val valuesCount: Int = 0,
    val message: String? = null
)

enum class ResultType(val id: Int) {
    SUCCESS(0), ACCESS_DENIED(1), EXCEPTION(2)
}

data class CentralConfigWithConfig(
    @Embedded
    val centralConfig: CentralConfig,

    @Relation(
        parentColumn = "id",
        entityColumn = "centralConfigId"
    )
    val configs: List<Config> = emptyList()
)

@Entity(tableName = "central_config")
data class CentralConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val url: String,
    @ColumnInfo(defaultValue = "1")
    val enabled: Boolean = true
)
