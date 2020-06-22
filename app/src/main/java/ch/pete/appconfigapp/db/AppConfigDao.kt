package ch.pete.appconfigapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.model.ConfigEntry
import ch.pete.appconfigapp.model.ExecutionResult
import ch.pete.appconfigapp.model.ExternalConfigLocation
import ch.pete.appconfigapp.model.KeyValue
import timber.log.Timber
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

    @Query("SELECT * FROM config WHERE externalConfigLocationId = :externalConfigLocationId")
    suspend fun fetchConfigByExternalConfigLocationId(externalConfigLocationId: Long): List<Config>

    @Transaction
    @Query("SELECT * FROM execution_result WHERE configId = :configId ORDER BY timestamp DESC")
    fun fetchExecutionResultEntriesByConfigId(configId: Long): LiveData<List<ExecutionResult>>

    @Query("SELECT * FROM key_value WHERE configId = :configId ORDER BY `key`")
    fun keyValueEntriesByConfigId(configId: Long): LiveData<List<KeyValue>>

    @Query("SELECT * FROM key_value WHERE id = :keyValueId")
    fun keyValueEntryByKeyValueId(keyValueId: Long): LiveData<KeyValue>

    @Query("SELECT * FROM external_config_location")
    fun externalConfigLocations(): LiveData<List<ExternalConfigLocation>>

    @Query("SELECT * FROM external_config_location")
    suspend fun externalConfigLocationsSuspend(): List<ExternalConfigLocation>

    @Query("SELECT * FROM external_config_location where id = :id")
    fun externalConfigLocationById(id: Long): LiveData<ExternalConfigLocation>

    @Transaction
    suspend fun deleteConfigEntry(configEntry: ConfigEntry) {
        deleteConfig(configEntry.config)
        deleteKeyValues(configEntry.keyValues)
        deleteExecutionResults(configEntry.executionResults)
    }

    @Transaction
    suspend fun cloneConfigEntryWithoutResultsAndExternalConfigLocation(
        configEntry: ConfigEntry,
        newName: String
    ) {
        val configId = insertConfig(
            configEntry.config.copy(
                id = null,
                externalConfigId = null,
                externalConfigLocationId = null,
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
            insertKeyValues(keyValues)
        } else {
            Timber.e("config.id is null")
        }
    }

    /**
     * Insert creationTimestamp with help of Calendar because sqlite
     * does not support setting the timezone when using defaults.
     */
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

    @Query("INSERT INTO external_config_location (name, url) VALUES ('','')")
    suspend fun insertExternalConfigLocation(): Long

    @Update
    suspend fun updateConfig(config: Config): Int

    @Query("UPDATE config SET name = :name, authority = :authority WHERE id = :configId")
    suspend fun updateConfigNameAndAuthority(name: String, authority: String, configId: Long)

    @Update
    suspend fun updateKeyValue(keyValue: KeyValue): Int

    @Update
    suspend fun updateExecutionResult(executionResults: List<ExecutionResult>): Int

    @Query("UPDATE external_config_location SET name = :name, url = :url WHERE id = :id")
    suspend fun updateExternalConfigLocation(name: String, url: String, id: Long)

    @Query("DELETE FROM config WHERE externalConfigLocationId NOT NULL")
    suspend fun deleteAllExternalConfigLocations()

    @Delete
    suspend fun deleteConfig(config: Config): Int

    @Query("DELETE FROM config WHERE id = :id")
    suspend fun deleteConfig(id: Long): Int

    @Delete
    suspend fun deleteKeyValues(keyValues: List<KeyValue>): Int

    @Delete
    suspend fun deleteKeyValue(keyValue: KeyValue): Int

    @Query("DELETE FROM key_value WHERE configId = :configId")
    suspend fun deleteKeyValuesByConfigId(configId: Long): Int

    @Delete
    suspend fun deleteExecutionResults(executionResults: List<ExecutionResult>): Int

    @Query("DELETE FROM external_config_location WHERE id = :id")
    suspend fun deleteExternalConfigLocation(id: Long)

    @Query("DELETE FROM config WHERE id IN (:ids)")
    suspend fun deleteConfigs(ids: List<Long>)
}
