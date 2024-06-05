package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequest
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.HttpClient
import com.mutakindv.cryptofeed.api.InternalServerError
import com.mutakindv.cryptofeed.api.InternalServerErrorException
import com.mutakindv.cryptofeed.api.InvalidData
import com.mutakindv.cryptofeed.api.InvalidDataException
import com.mutakindv.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class LoadCryptoFeedRemoteUseCaseTest {

    private val client = mockk<HttpClient>()
    private lateinit var sut: LoadCryptoFeedRemoteUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = LoadCryptoFeedRemoteUseCase(client)
    }

    @Test
    fun testInitDoesNotRequestData() {
        verify(exactly = 0) {
            client.get()
        }

        confirmVerified(client)
    }

    @Test
    fun testLoadRequestData() = runTest{
        every {
            client.get()
        } returns flowOf()
        sut.load().test {
            awaitComplete()
        }
        verify(exactly = 1) {
            client.get()
        }

        confirmVerified(client)
    }
    @Test
    fun testLoadRequestDataTwice() = runTest{

        every {
            client.get()
        } returns flowOf()
        sut.load().test {
            awaitComplete()
        }
        sut.load().test {
            awaitComplete()
        }
        verify(exactly = 2) {
            client.get()
        }
        confirmVerified(client)
    }

    @Test
    fun testLoadDeliverConnectivityErrorOnClientError() = runTest {
        expect(
            client = client,
            sut = sut,
            receivedHttpClient = ConnectivityException(),
            expectedResult = Connectivity(),
            exactly = 1,
            confirmVerified = client
        )
    }

    @Test
    fun testLoadDeliverInvalidDataError() = runTest {
        expect(
            client = client,
            sut = sut,
            receivedHttpClient = InvalidDataException(),
            expectedResult = InvalidData(),
            exactly = 1,
            confirmVerified = client
        )
    }
    @Test
    fun testLoadDeliverBadRequestError() = runTest {
        expect(
            client = client,
            sut = sut,
            receivedHttpClient = BadRequestException(),
            expectedResult = BadRequest(),
            exactly = 1,
            confirmVerified = client
        )
    }
    @Test
    fun testLoadDeliverInternalServerError() = runTest {
        expect(
            client = client,
            sut = sut,
            receivedHttpClient = InternalServerErrorException(),
            expectedResult = InternalServerError(),
            exactly = 1,
            confirmVerified = client
        )
    }

    private fun expect(
        client: HttpClient,
        sut: LoadCryptoFeedRemoteUseCase,
        receivedHttpClient: Exception,
        expectedResult: Any,
        exactly: Int = -1,
        confirmVerified: HttpClient
    ) = runTest {
        every {
            client.get()
        } returns flowOf(receivedHttpClient)

        sut.load().test {
            assertEquals(expectedResult::class.java, awaitItem()::class.java)
            awaitComplete()
        }

        verify(exactly = exactly) {
            client.get()
        }

        confirmVerified(confirmVerified)
    }
}
