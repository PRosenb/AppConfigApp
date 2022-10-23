package ch.pete.appconfigapp.sync

import android.database.sqlite.SQLiteConstraintException
import ch.pete.appconfigapp.api.ExternalConfigLocationService
import ch.pete.appconfigapp.api.model.ExternalConfig
import ch.pete.appconfigapp.api.model.ExternalKeyValue
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.Config
import ch.pete.appconfigapp.model.ExternalConfigLocation
import ch.pete.appconfigapp.model.KeyValue
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException

@ExtendWith(MockitoExtension::class)
internal class ExternalConfigLocationSyncerTest {
    @Suppress("OPT_IN_USAGE")
    @kotlinx.coroutines.ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    @kotlinx.coroutines.ObsoleteCoroutinesApi
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        externalConfigsSyncer = ExternalConfigsSyncer(appConfigDao, externalConfigService)
    }

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    @kotlinx.coroutines.ObsoleteCoroutinesApi
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Mock
    private lateinit var appConfigDao: AppConfigDao

    @Mock
    private lateinit var externalConfigService: ExternalConfigLocationService

    private suspend fun initMocks() {
        whenever(appConfigDao.fetchConfigByExternalConfigLocationId(0))
            .thenReturn(existingConfigsOfExternalConfigLocation0)
        whenever(appConfigDao.fetchConfigByExternalConfigLocationId(1))
            .thenReturn(existingConfigsOfExternalConfigLocation1)
        whenever(appConfigDao.externalConfigLocationsSuspend()).thenReturn(externalConfigLocations)
        whenever(externalConfigService.fetchCentalConfigConfig("url0")).thenReturn(externalConfigs0)
        whenever(externalConfigService.fetchCentalConfigConfig("url1")).thenReturn(externalConfigs1)
    }

    private val existingConfigsOfExternalConfigLocation0: MutableList<Config> = mutableListOf(
        Config(
            id = 0,
            externalConfigId = "externalConfigId0",
            externalConfigLocationId = 0,
            name = "LocalConfig 0.0",
            authority = "authority0.0"
        ),
        Config(
            id = 1,
            externalConfigId = "externalConfigId1",
            externalConfigLocationId = 0,
            name = "LocalConfig 0.1",
            authority = "authority0.1"
        )
    )
    private val existingConfigsOfExternalConfigLocation1: MutableList<Config> = mutableListOf(
        Config(
            id = 2,
            externalConfigId = "externalConfigId2",
            externalConfigLocationId = 1,
            name = "LocalConfig 1.0",
            authority = "authority1.0"
        ),
        Config(
            id = 3,
            externalConfigId = "externalConfigId3",
            externalConfigLocationId = 1,
            name = "LocalConfig 1.1",
            authority = "authority1.1"
        )
    )
    private val externalConfigLocations: MutableList<ExternalConfigLocation> = mutableListOf(
        ExternalConfigLocation(
            id = 0,
            name = "ExternalConfigLocation 0",
            url = "url0"
        ),
        ExternalConfigLocation(
            id = 1,
            name = "ExternalConfigLocation 1",
            url = "url1"
        )
    )
    private val externalConfigs0: MutableList<ExternalConfig> = mutableListOf(
        ExternalConfig(
            id = "externalConfigId0",
            name = "ApiConfigEntry 0.0",
            authority = "authority0.0",
            sort = 1
        ),
        ExternalConfig(
            id = "externalConfigId1",
            name = "ApiConfigEntry 0.1",
            authority = "authority0.1",
            sort = 2
        )
    )
    private val externalConfigs1: MutableList<ExternalConfig> = mutableListOf(
        ExternalConfig(
            id = "externalConfigId2",
            name = "ApiConfigEntry 1.0",
            authority = "authority1.0",
            sort = 3
        ),
        ExternalConfig(
            id = "externalConfigId3",
            name = "ApiConfigEntry 1.1",
            authority = "authority1.1",
            sort = 4
        )
    )

    private lateinit var externalConfigsSyncer: ExternalConfigsSyncer

    @Test
    fun init() = runBlocking {
        // given
        // when
        externalConfigsSyncer.init()
        // then
        verify(externalConfigService).init()
    }

    @Test
    fun `add 2 new entries each`() = runBlocking {
        // given
        existingConfigsOfExternalConfigLocation0.clear()
        existingConfigsOfExternalConfigLocation1.clear()
        initMocks()
        // when
        val count = externalConfigsSyncer.sync()
        // then
        assertThat(count).isEqualTo(4)
        verify(appConfigDao, times(2)).deleteConfigs(eq(emptyList()))
        verify(appConfigDao, times(4)).insertConfigWithKeyValues(any(), eq(emptyList()))
        verify(appConfigDao, never()).updateConfigWithKeyValues(any(), any())
    }

    @Test
    fun `delete all existing`() = runBlocking {
        // given
        externalConfigs0.clear()
        externalConfigs1.clear()
        initMocks()
        // when
        val count = externalConfigsSyncer.sync()
        // then
        assertThat(count).isEqualTo(0)
        verify(appConfigDao).deleteConfigs(eq(listOf(0L, 1)))
        verify(appConfigDao).deleteConfigs(eq(listOf(2L, 3)))
        verify(appConfigDao, never()).insertConfigWithKeyValues(any(), any())
        verify(appConfigDao, never()).updateConfigWithKeyValues(any(), any())
    }

    @Test
    fun `add entries with keys`() = runBlocking {
        // given
        externalConfigs0.add(
            ExternalConfig(
                name = "name",
                authority = "authority",
                sort = 1,
                keyValues = listOf(
                    ExternalKeyValue(key = "key0", value = "value0"),
                    ExternalKeyValue(key = "key1", value = "value1")
                )
            )
        )
        initMocks()
        // when
        externalConfigsSyncer.sync()
        // then
        verify(appConfigDao, times(1)).insertConfigWithKeyValues(
            any(),
            eq(
                listOf(
                    KeyValue(configId = -1, key = "key0", value = "value0"),
                    KeyValue(configId = -1, key = "key1", value = "value1")
                )
            )
        )
    }

    @Test
    fun `server error http 500`() = runBlocking {
        // given
        whenever(appConfigDao.externalConfigLocationsSuspend()).thenReturn(externalConfigLocations)
        whenever(externalConfigService.fetchCentalConfigConfig(any())).thenThrow(HttpException::class.java)
        var logMessage: String? = null
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logMessage = message
            }
        })
        // when
        val result = externalConfigsSyncer.sync()
        // then
        assertThat(result).describedAs("amount synced").isEqualTo(0)
        assertThat(logMessage).describedAs("log message").isNotBlank
            .startsWith("Could not fetch external config location")
        assertThat(logMessage).describedAs("log message with wrong exception").isNotBlank
            .endsWith("HttpException\n")
        Unit
    }

    @Test
    fun `parse error`() = runBlocking {
        // given
        whenever(appConfigDao.externalConfigLocationsSuspend()).thenReturn(externalConfigLocations)
        whenever(externalConfigService.fetchCentalConfigConfig(any())).thenThrow(
            IllegalArgumentException::class.java
        )
        var logMessage: String? = null
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logMessage = message
            }
        })
        // when
        val result = externalConfigsSyncer.sync()
        // then
        assertThat(result).describedAs("amount synced").isEqualTo(0)
        assertThat(logMessage).describedAs("log message").isNotBlank
            .startsWith("Could not fetch external config location")
        assertThat(logMessage).describedAs("log message with wrong exception").isNotBlank
            .endsWith("IllegalArgumentException\n")
        Unit
    }

    @Test
    fun `SQL constrant error`() = runBlocking {
        // given
        whenever(appConfigDao.externalConfigLocationsSuspend()).thenReturn(externalConfigLocations)
        whenever(externalConfigService.fetchCentalConfigConfig(any())).thenThrow(
            SQLiteConstraintException::class.java
        )
        var logMessage: String? = null
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logMessage = message
            }
        })
        // when
        val result = externalConfigsSyncer.sync()
        // then
        assertThat(result).describedAs("amount synced").isEqualTo(0)
        assertThat(logMessage).describedAs("error not logged").isNotBlank
            .endsWith("SQLiteConstraintException\n")
        Unit
    }

    @Test
    fun `server not available`() = runBlocking {
        // given
        whenever(appConfigDao.externalConfigLocationsSuspend()).thenReturn(externalConfigLocations)
        whenever(externalConfigService.fetchCentalConfigConfig(any())).thenThrow(
            ConnectException::class.java
        )
        var logMessage: String? = null
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logMessage = message
            }
        })
        // when
        val result = externalConfigsSyncer.sync()
        // then
        assertThat(result).describedAs("amount synced").isEqualTo(0)
        assertThat(logMessage).describedAs("error not logged").isNotBlank
            .endsWith("ConnectException\n")
        Unit
    }
}
