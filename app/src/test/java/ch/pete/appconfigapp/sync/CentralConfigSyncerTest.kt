package ch.pete.appconfigapp.sync

import ch.pete.appconfigapp.api.ApiConfigEntry
import ch.pete.appconfigapp.api.CentralConfigService
import ch.pete.appconfigapp.db.AppConfigDao
import ch.pete.appconfigapp.model.CentralConfig
import ch.pete.appconfigapp.model.Config
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

@ExtendWith(MockitoExtension::class)
internal class CentralConfigSyncerTest {
    @kotlinx.coroutines.ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    @kotlinx.coroutines.ObsoleteCoroutinesApi
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        centralConfigSyncer = CentralConfigSyncer(appConfigDao, centralConfigService)
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
    private lateinit var centralConfigService: CentralConfigService

    private suspend fun initMocks() {
        whenever(appConfigDao.fetchConfigByCentralConfigId(0))
            .thenReturn(existingConfigsOfCentralConfig0)
        whenever(appConfigDao.fetchConfigByCentralConfigId(1))
            .thenReturn(existingConfigsOfCentralConfig1)
        whenever(appConfigDao.centralConfigsSuspend()).thenReturn(centralConfigs)
        whenever(centralConfigService.fetchConfig("url0")).thenReturn(apiConfigs0)
        whenever(centralConfigService.fetchConfig("url1")).thenReturn(apiConfigs1)
    }

    private val existingConfigsOfCentralConfig0: MutableList<Config> = mutableListOf(
        Config(
            id = 0,
            centralConfigId = 0,
            name = "LocalConfig 0.0",
            authority = "authority0.0"
        ),
        Config(
            id = 1,
            centralConfigId = 0,
            name = "LocalConfig 0.1",
            authority = "authority0.1"
        )
    )
    private val existingConfigsOfCentralConfig1: MutableList<Config> = mutableListOf(
        Config(
            id = 2,
            centralConfigId = 1,
            name = "LocalConfig 1.0",
            authority = "authority1.0"
        ),
        Config(
            id = 3,
            centralConfigId = 1,
            name = "LocalConfig 1.1",
            authority = "authority1.1"
        )
    )
    private val centralConfigs: MutableList<CentralConfig> = mutableListOf(
        CentralConfig(
            id = 0,
            name = "CentralConfig 0",
            url = "url0"
        ),
        CentralConfig(
            id = 1,
            name = "CentralConfig 1",
            url = "url1"
        )
    )
    private val apiConfigs0: MutableList<ApiConfigEntry> = mutableListOf(
        ApiConfigEntry(
            name = "ApiConfigEntry 0.0",
            authority = "authority0.0"
        ),
        ApiConfigEntry(
            name = "ApiConfigEntry 0.1",
            authority = "authority0.1"
        )
    )
    private val apiConfigs1: MutableList<ApiConfigEntry> = mutableListOf(
        ApiConfigEntry(
            name = "ApiConfigEntry 1.0",
            authority = "authority1.0"
        ),
        ApiConfigEntry(
            name = "ApiConfigEntry 1.1",
            authority = "authority1.1"
        )
    )

    private lateinit var centralConfigSyncer: CentralConfigSyncer

    @Test
    fun init() = runBlocking {
        // given
        // when
        centralConfigSyncer.init()
        // then
        verify(centralConfigService).init()
    }

    @Test
    fun `add 2 new entries each`() = runBlocking {
        // given
        existingConfigsOfCentralConfig0.clear()
        existingConfigsOfCentralConfig1.clear()
        initMocks()
        // when
        val count = centralConfigSyncer.sync()
        // then
        assertThat(count).isEqualTo(4)
        verify(appConfigDao, times(2)).deleteConfigs(eq(emptyList()))
        verify(appConfigDao, times(4)).insertConfigWithKeyValues(any(), any())
        verify(appConfigDao, never()).updateConfigWithKeyValues(any(), any())
    }

    @Test
    fun `delete all existing`() = runBlocking {
        // given
        apiConfigs0.clear()
        apiConfigs1.clear()
        initMocks()
        // when
        val count = centralConfigSyncer.sync()
        // then
        assertThat(count).isEqualTo(0)
        verify(appConfigDao).deleteConfigs(eq(listOf(0L, 1)))
        verify(appConfigDao).deleteConfigs(eq(listOf(2L, 3)))
        verify(appConfigDao, never()).insertConfigWithKeyValues(any(), any())
        verify(appConfigDao, never()).updateConfigWithKeyValues(any(), any())
    }
}
