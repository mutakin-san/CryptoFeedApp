package com.mutakindv.cryptofeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.InvalidData
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedUseCase
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


data class UiState(
    val isLoading: Boolean = false,
    val cryptoFeed: List<CryptoFeed> = emptyList(),
    val failed: String = "",
)

class CryptoFeedViewModel(val useCase: LoadCryptoFeedUseCase): ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true)
            }

            useCase.load().collect { result ->
                _uiState.update {
                    when(result) {
                        is LoadCryptoFeedResult.Success -> TODO()
                        is LoadCryptoFeedResult.Error -> {
                            it.copy(failed = when(result.exception) {
                                is Connectivity -> "Tidak ada internet"
                                else -> "Terjadi kesalahan!"
                            })
                        }

                    }
                }
            }
        }
    }

}


class CryptoFeedViewModelTest {
    private val useCase = spyk<LoadCryptoFeedUseCase>()
    private lateinit var sut: CryptoFeedViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = CryptoFeedViewModel(useCase)

        Dispatchers.setMain(UnconfinedTestDispatcher())
    }


    @Test
    fun testInitInitialState() {
        val uiState = sut.uiState.value

        assertFalse(uiState.isLoading)
        assertTrue(uiState.cryptoFeed.isEmpty())
        assertTrue(uiState.failed.isEmpty())
    }


    @Test
    fun testInitDoesNotLoad() = runTest {
        verify(exactly = 0) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadRequestData() = runTest{
        every {
            useCase.load()
        } returns flowOf()

        sut.load()
        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }


    @Test
    fun testLoadTwiceRequestDataTwice() {
        every {
            useCase.load()
        } returns flowOf()

        sut.load()
        sut.load()

        verify(exactly = 2) {
            useCase.load()
        }

        confirmVerified(useCase)
    }


     @Test
    fun testLoadIsLoadingState() = runTest {
        every {
            useCase.load()
        } returns flowOf()

        sut.load()

         sut.uiState.take(1).test {
             val receivedResult = awaitItem()

             assertEquals(true, receivedResult.isLoading)
             awaitComplete()
         }

         verify(exactly = 1) {
             useCase.load()
         }

        confirmVerified(useCase)
    }

    @Test
    fun testLoadFailedConnectivityShowsConnectivityError() = runTest {
        every {
            useCase.load()
        } returns flowOf(LoadCryptoFeedResult.Error(Connectivity()))

        sut.load()

        sut.uiState.take(1).test {
            val receivedResult = awaitItem()
            assertEquals("Tidak ada internet", receivedResult.failed)
            awaitComplete()
        }

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }




}
