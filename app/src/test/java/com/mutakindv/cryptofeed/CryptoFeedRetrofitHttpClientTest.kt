package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.mutakindv.cryptofeed.api.CryptoFeedService
import com.mutakindv.cryptofeed.api.HttpClientResult
import com.mutakindv.cryptofeed.api.InternalServerErrorException
import com.mutakindv.cryptofeed.api.InvalidDataException
import com.mutakindv.cryptofeed.api.NotFoundException
import com.mutakindv.cryptofeed.api.RemoteCoinInfo
import com.mutakindv.cryptofeed.api.RemoteCryptoFeed
import com.mutakindv.cryptofeed.api.RemoteCryptoFeedItem
import com.mutakindv.cryptofeed.api.RemoteDisplay
import com.mutakindv.cryptofeed.api.RemoteUsd
import com.mutakindv.cryptofeed.api.UnexpectedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CryptoFeedRetrofitHttpClientTest {
    private val service = mockk<CryptoFeedService>()
    private lateinit var sut: CryptoFeedRetrofitHttpClient

    @Before
    fun setup() {
        sut = CryptoFeedRetrofitHttpClient(service = service)
    }


    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithData() {
        val remoteCryptoFeedResponse = listOf(
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

        expect(withStatusCode = 200, sut= sut, expectedResult = HttpClientResult.Success(root = RemoteCryptoFeed(remoteCryptoFeedResponse)))
    }

    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithEmptyData() = runTest {
        val remoteCryptoFeedResponse =  emptyList<RemoteCryptoFeedItem>()
        expect(
            withStatusCode = 200,
            sut = sut,
            expectedResult = HttpClientResult.Success(
                RemoteCryptoFeed(
                    remoteCryptoFeedResponse
                )
            ),
        )
    }

    @Test
    fun testGetFailsOnConnectivityError() {
        expect(sut = sut, expectedResult =  HttpClientResult.Failure(ConnectivityException()))
    }


    @Test
    fun testGetFailsOn400HttpResponse() {
        expect(400, sut, HttpClientResult.Failure(BadRequestException()))
    }

    @Test
    fun testGetFailsOn404HttpResponse() {
        expect(404, sut, HttpClientResult.Failure(NotFoundException()))
    }
    @Test
    fun testGetFailsOn500HttpResponse() {
        expect(500, sut, HttpClientResult.Failure(InternalServerErrorException()))
    }

    @Test
    fun testGetFailsOnInvalidDataError() {
        expect(withStatusCode = 422, sut = sut, expectedResult  = HttpClientResult.Failure(InvalidDataException()))
    }
    @Test
    fun testGetFailsOnUnexpectedError() {
        expect(sut = sut, expectedResult  = HttpClientResult.Failure(UnexpectedException()))
    }

    private fun expect(
        withStatusCode: Int? = null,
        sut: CryptoFeedRetrofitHttpClient,
        expectedResult: HttpClientResult
    ) = runTest {
        when {
            withStatusCode != null -> {
                when(withStatusCode) {
                    200 -> {
                        coEvery {
                            service.get()
                        } returns flowOf(expectedResult)
                    }
                    else -> {
                        val response = Response.error<RemoteCryptoFeed>(withStatusCode, ResponseBody.create(null, ""))
                        coEvery {
                            service.get()
                        } throws HttpException(response)
                    }
                }
            }
            expectedResult is HttpClientResult.Failure -> {
                when(expectedResult.exception) {
                    is ConnectivityException -> {
                        coEvery {
                            service.get()
                        } throws IOException()
                    }
                }
            }
        }

        sut.get().test {
            when(expectedResult) {
                is HttpClientResult.Success -> {
                    val receivedValue = awaitItem() as HttpClientResult.Success
                    assertEquals(expectedResult.root::class.java, receivedValue.root::class.java)
                }
                is HttpClientResult.Failure -> {
                    val receivedValue = awaitItem() as HttpClientResult.Failure
                    assertEquals(expectedResult.exception::class.java, receivedValue.exception::class.java)

                }

            }
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}


