package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequest
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.HttpClient
import com.mutakindv.cryptofeed.api.HttpClientResult
import com.mutakindv.cryptofeed.api.InternalServerError
import com.mutakindv.cryptofeed.api.InternalServerErrorException
import com.mutakindv.cryptofeed.api.InvalidData
import com.mutakindv.cryptofeed.api.InvalidDataException
import com.mutakindv.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import com.mutakindv.cryptofeed.api.NotFound
import com.mutakindv.cryptofeed.api.NotFoundException
import com.mutakindv.cryptofeed.api.RootRemoteCryptoFeed
import com.mutakindv.cryptofeed.api.RemoteCryptoFeed
import com.mutakindv.cryptofeed.api.Unexpected
import com.mutakindv.cryptofeed.api.UnexpectedException
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
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
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(ConnectivityException()),
            expectedResult = Connectivity(),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliverInvalidDataError() = runTest {
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(InvalidDataException()),
            expectedResult = InvalidData(),
            exactly = 1,
        )
    }
    @Test
    fun testLoadDeliverBadRequestError() = runTest {
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(BadRequestException()),
            expectedResult = BadRequest(),
            exactly = 1,
        )
    }
    @Test
    fun testLoadDeliverUnexpectedError() = runTest {
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(UnexpectedException()),
            expectedResult = Unexpected(),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliverNotFoundError() = runTest {
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(NotFoundException()),
            expectedResult = NotFound(),
            exactly = 1,
        )
    }
    @Test
    fun testLoadDeliverInternalServerError() = runTest {
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(InternalServerErrorException()),
            expectedResult = InternalServerError(),
            exactly = 1,
        )
    }


    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithData() = runTest {

        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Success(
                RootRemoteCryptoFeed(
                    remoteCryptoFeedResponse
                )
            ),
            expectedResult = LoadCryptoFeedResult.Success(cryptoFeedItems),
            exactly = 1,
        )
    }

    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithEmptyData() = runTest {
        val remoteCryptoFeedResponse =  emptyList<RemoteCryptoFeed>()
        val cryptoFeedItems = emptyList<CryptoFeed>()
        expect(
            sut = sut,
            receivedHttpClient = HttpClientResult.Success(
                RootRemoteCryptoFeed(
                    remoteCryptoFeedResponse
                )
            ),
            expectedResult = LoadCryptoFeedResult.Success(cryptoFeedItems),
            exactly = 1,
        )
    }

    private fun expect(
        sut: LoadCryptoFeedRemoteUseCase,
        receivedHttpClient: HttpClientResult,
        expectedResult: Any,
        exactly: Int = -1,
    ) = runTest {
        every {
            client.get()
        } returns flowOf(receivedHttpClient)

        sut.load().test {
            when(val receivedResult = awaitItem()) {
                is LoadCryptoFeedResult.Success -> {
                    assertEquals(expectedResult, receivedResult)
                }
                is LoadCryptoFeedResult.Error -> {
                    assertEquals(expectedResult::class.java, receivedResult.exception::class.java)
                }
            }
            awaitComplete()
        }

        verify(exactly = exactly) {
            client.get()
        }

        confirmVerified(client)
    }
}
