package com.mutakindv.cryptofeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequest
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.InternalServerError
import com.mutakindv.cryptofeed.api.InvalidData
import com.mutakindv.cryptofeed.api.NotFound
import com.mutakindv.cryptofeed.api.Unexpected
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
                            it.copy(
                                isLoading = false,
                                failed = when(result.exception) {
                                    is Connectivity -> "Tidak ada internet"
                                    is InvalidData -> "Terjadi kesalahan"
                                    is BadRequest -> "Permintaan gagal, coba lagi"
                                    is NotFound -> "Tidak ditemukan, coba lagi"
                                    is InternalServerError -> "Server sedang dalam perbaikan, coba lagi"
                                    else -> "Terjadi kesalahan"
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
        expect(LoadCryptoFeedResult.Error(Connectivity()), sut, false, "Tidak ada internet")
    }

    @Test
    fun testLoadFailedInvalidDataShowsInvalidDataError() = runTest {
        expect(LoadCryptoFeedResult.Error(InvalidData()), sut, false, "Terjadi kesalahan")
    }

    @Test
    fun testLoadBadRequestShowsBadRequestError() = runTest {
        expect(LoadCryptoFeedResult.Error(BadRequest()), sut, false, "Permintaan gagal, coba lagi")
    }

    @Test
    fun testLoadNotFoundShowsNotFoundError() = runTest {
        expect(LoadCryptoFeedResult.Error(NotFound()), sut, false, "Tidak ditemukan, coba lagi")
    }

    @Test
    fun testLoadInternalServerErrorShowsInternalServerError() = runTest {
        expect(LoadCryptoFeedResult.Error(InternalServerError()), sut, false, "Server sedang dalam perbaikan, coba lagi")
    }

    @Test
    fun testLoadUnexpectedErrorShowsUnexpectedError() = runTest {
        expect(LoadCryptoFeedResult.Error(Unexpected()), sut, false, "Terjadi kesalahan")
    }




    private fun expect(
        result: LoadCryptoFeedResult,
        sut: CryptoFeedViewModel,
        expectedLoadingResult: Boolean,
        expectedFailedResult: String
    ) = runTest {
        every {
            useCase.load()
        } returns flowOf(result)

        sut.load()

        sut.uiState.take(1).test {
            val receivedResult = awaitItem()
            assertEquals(expectedLoadingResult, receivedResult.isLoading)
            assertEquals(expectedFailedResult, receivedResult.failed)
            awaitComplete()
        }

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

}
