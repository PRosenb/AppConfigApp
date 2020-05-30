package ch.pete.appconfigapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ch.pete.appconfigapp.model.CentralConfig
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.model.ConfigEntry
import ch.pete.appconfigapp.model.ExecutionResult
import ch.pete.appconfigapp.model.KeyValue
import java.util.Calendar

@Suppress("TooManyFunctions")
@Dao
interface AppConfigDao {
    @Transaction
    @Query("SELECT * FROM config")
    fun fetchConfigEntries(): LiveData<List<ConfigEntry>>

    @Transaction
    @Query("SELECT * FROM config WHERE config.id = :configId")
    fun fetchConfigEntryByIdAsLiveData(configId: Long): LiveData<ConfigEntry>?

    @Transaction
    @Query("SELECT * FROM config WHERE config.id = :configId")
    suspend fun fetchConfigEntryById(configId: Long): ConfigEntry?

    @Query("SELECT * FROM config WHERE config.id = :configId")
    fun fetchConfigById(configId: Long): LiveData<Config>

    @Query("SELECT * FROM config WHERE centralConfigId = :centralConfigId")
    suspend fun fetchConfigByCentralConfigId(centralConfigId: Long): List<Config>

    @Transaction
    @Query("SELECT * FROM execution_result WHERE configId = :configId ORDER BY timestamp DESC")
    fun fetchExecutionResultEntriesByConfigId(configId: Long): LiveData<List<ExecutionResult>>

    @Query("SELECT * FROM key_value WHERE configId = :configId ORDER BY `key`")
    fun keyValueEntriesByConfigId(configId: Long): LiveData<List<KeyValue>>

    @Query("SELECT * FROM key_value WHERE id = :keyValueId")
    fun keyValueEntryByKeyValueId(keyValueId: Long): LiveData<KeyValue>

    @Query("SELECT * FROM central_config")
    fun centralConfigs(): LiveData<List<CentralConfig>>

    @Query("SELECT * FROM central_config")
    suspend fun centralConfigsSuspend(): List<CentralConfig>

    @Query("SELECT * FROM central_config where id = :id")
    fun centralConfigById(id: Long): LiveData<CentralConfig>

    @Transaction
    suspend fun deleteConfigEntry(configEntry: ConfigEntry) {
        deleteConfig(configEntry.config)
        deleteKeyValues(configEntry.keyValues)
        deleteExecutionResults(configEntry.executionResults)
    }

    @Transaction
    suspend fun cloneConfigEntryWithoutResultsAndCentralConfig(
        configEntry: ConfigEntry,
        newName: String
    ) {
        val configId = insertConfig(
            configEntry.config.copy(
                id = null,
                centralConfigExternalId = null,
                centralConfigId = null,
                name = newName
            )
        )

        // https://issuetracker.google.com/issues/62848977
        val keyValues =
            configEntry.keyValues.map {
                it.copy(
                    id = null,
                    configId = configId
                )
            }

        insertKeyValues(keyValues)
    }

    @Transaction
    suspend fun insertConfigWithKeyValues(config: Config, keyValues: List<KeyValue>) {
        val configId = insertConfig(config)

        insertKeyValues(
            keyValues.map {
                it.copy(
                    configId = configId
                )
            }
        )
    }

    @Transaction
    suspend fun updateConfigWithKeyValues(config: Config, keyValues: List<KeyValue>) {
        updateConfig(config)

        if (config.id != null) {
            deleteKeyValuesByConfigId(config.id)
            insertKeyValues(
                keyValues.map {
                    it.copy(
                        configId = config.id
                    )
                }
            )
        }
    }

    @Query("INSERT INTO config (name, authority, creationTimestamp) VALUES ('','', :creationTimestamp)")
    suspend fun insertEmptyConfig(creationTimestamp: Calendar): Long

    @Insert
    suspend fun insertConfig(config: Config): Long

    @Insert
    suspend fun insertKeyValue(keyValue: KeyValue): Long

    @Insert
    suspend fun insertKeyValues(keyValues: List<KeyValue>)

    @Insert
    suspend fun insertExecutionResult(executionResult: ExecutionResult): Long

    @Query("INSERT INTO central_config (name, url) VALUES ('','')")
    suspend fun insertCentralConfig(): Long

    @Update
    suspend fun updateConfig(config: Config): Int

    @Query("UPDATE config SET name = :name, authority = :authority WHERE id = :configId")
    suspend fun updateConfigNameAndAuthority(name: String, authority: String, configId: Long)

    @Update
    suspend fun updateKeyValue(keyValue: KeyValue): Int

    @Update
    suspend fun updateExecutionResult(executionResults: List<ExecutionResult>): Int

    @Query("UPDATE central_config SET name = :name, url = :url WHERE id = :id")
    suspend fun updateCentralConfig(name: String, url: String, id: Long)

    @Query("DELETE FROM config WHERE centralConfigId NOT NULL")
    suspend fun deleteAllCentralConfigs()

    @Delete
    suspend fun deleteConfig(config: Config): Int

    @Delete
    suspend fun deleteKeyValues(keyValues: List<KeyValue>): Int

    @Delete
    suspend fun deleteKeyValue(keyValue: KeyValue): Int

    @Query("DELETE FROM key_value WHERE configId = :configId")
    suspend fun deleteKeyValuesByConfigId(configId: Long): Int

    @Delete
    suspend fun deleteExecutionResults(executionResults: List<ExecutionResult>): Int

    @Delete
    suspend fun deleteCentralConfig(centralConfig: CentralConfig)

    @Query("DELETE FROM config WHERE id IN (:ids)")
    suspend fun deleteConfigs(ids: List<Long>)
}
