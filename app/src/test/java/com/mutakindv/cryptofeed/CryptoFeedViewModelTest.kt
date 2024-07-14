package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequest
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.InternalServerError
import com.mutakindv.cryptofeed.api.InvalidData
import com.mutakindv.cryptofeed.api.NotFound
import com.mutakindv.cryptofeed.api.Unexpected
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedUseCase
import com.mutakindv.cryptofeed.presentation.CryptoFeedViewModel
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


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
        expect(LoadCryptoFeedResult.Error(Connectivity()), sut, "Tidak ada internet")
    }

    @Test
    fun testLoadFailedInvalidDataShowsInvalidDataError() = runTest {
        expect(LoadCryptoFeedResult.Error(InvalidData()), sut, "Terjadi kesalahan")
    }

    @Test
    fun testLoadBadRequestShowsBadRequestError() = runTest {
        expect(LoadCryptoFeedResult.Error(BadRequest()), sut, "Permintaan gagal, coba lagi")
    }

    @Test
    fun testLoadNotFoundShowsNotFoundError() = runTest {
        expect(LoadCryptoFeedResult.Error(NotFound()), sut, "Tidak ditemukan, coba lagi")
    }

    @Test
    fun testLoadInternalServerErrorShowsInternalServerError() = runTest {
        expect(LoadCryptoFeedResult.Error(InternalServerError()), sut, "Server sedang dalam perbaikan, coba lagi")
    }

    @Test
    fun testLoadUnexpectedErrorShowsUnexpectedError() = runTest {
        expect(LoadCryptoFeedResult.Error(Unexpected()), sut, "Terjadi kesalahan")
    }




    private fun expect(
        result: LoadCryptoFeedResult,
        sut: CryptoFeedViewModel,
        expectedFailedResult: String
    ) = runTest {
        every {
            useCase.load()
        } returns flowOf(result)

        sut.load()

        sut.uiState.take(1).test {
            val receivedResult = awaitItem()
            assertEquals(false, receivedResult.isLoading)
            assertEquals(expectedFailedResult, receivedResult.failed)
            awaitComplete()
        }

        verify(exactly = 1) {
            useCase.load()
        }

        confirmVerified(useCase)
    }

}
