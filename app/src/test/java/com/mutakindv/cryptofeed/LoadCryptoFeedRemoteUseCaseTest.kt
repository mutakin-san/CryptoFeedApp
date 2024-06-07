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
import com.mutakindv.cryptofeed.api.RemoteCoinInfo
import com.mutakindv.cryptofeed.api.RemoteCryptoFeed
import com.mutakindv.cryptofeed.api.RemoteCryptoFeedItem
import com.mutakindv.cryptofeed.api.RemoteDisplay
import com.mutakindv.cryptofeed.api.RemoteUsd
import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd
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
            client = client,
            sut = sut,
            receivedHttpClient = HttpClientResult.Failure(ConnectivityException()),
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
            receivedHttpClient = HttpClientResult.Failure(InvalidDataException()),
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
            receivedHttpClient = HttpClientResult.Failure(BadRequestException()),
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
            receivedHttpClient = HttpClientResult.Failure(InternalServerErrorException()),
            expectedResult = InternalServerError(),
            exactly = 1,
            confirmVerified = client
        )
    }


    private val remoteCryptoFeedResponse = listOf(
        RemoteCryptoFeedItem(
            remoteCoinInfo = RemoteCoinInfo("1", "BTC", "Bitcoin", "imageUrl"),
            remoteRaw = RemoteDisplay(
                usd = RemoteUsd(
                    price = 1.0,
                    changePctDay = 1F
                )
            )
        ),
        RemoteCryptoFeedItem(
            remoteCoinInfo = RemoteCoinInfo("2", "BTC2", "Bitcoin 2", "imageUrl"),
            remoteRaw = RemoteDisplay(
                usd = RemoteUsd(
                    price = 2.0,
                    changePctDay = 2F
                )
            )
        ),
    )


    private val cryptoFeedItems = listOf(
        CryptoFeed(
            coinInfo = CoinInfo("1", "BTC", "Bitcoin", "imageUrl"),
            raw = Raw(
                usd = Usd(
                    price = 1.0,
                    changePctDay = 1f
                )
            )
        ),
        CryptoFeed(
            coinInfo = CoinInfo("2", "BTC2", "Bitcoin 2", "imageUrl"),
            raw = Raw(
                usd = Usd(
                    price = 2.0,
                    changePctDay = 2f
                )
            )
        )
    )

    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithData() = runTest {
        expect(
            client = client,
            sut = sut,
            receivedHttpClient = HttpClientResult.Success(
                RemoteCryptoFeed(
                    remoteCryptoFeedResponse
                )
            ),
            expectedResult = LoadCryptoFeedResult.Success(cryptoFeedItems),
            exactly = 1,
            confirmVerified = client
        )
    }

    private fun expect(
        client: HttpClient,
        sut: LoadCryptoFeedRemoteUseCase,
        receivedHttpClient: HttpClientResult,
        expectedResult: Any,
        exactly: Int = -1,
        confirmVerified: HttpClient
    ) = runTest {
        every {
            client.get()
        } returns flowOf(receivedHttpClient)

        sut.load().test {
            when(val receivedResult = awaitItem()) {
                is LoadCryptoFeedResult.Success -> {
                    assertEquals(expectedResult::class.java, receivedResult::class.java)
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

        confirmVerified(confirmVerified)
    }
}
