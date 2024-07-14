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

        expect(
            withStatusCode = 200,
            sut= sut,
            expectedResult = RemoteCryptoFeed(remoteCryptoFeedResponse)
        )
    }

    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithEmptyData() = runTest {
        val remoteCryptoFeedResponse =  emptyList<RemoteCryptoFeedItem>()
        expect(
            withStatusCode = 200,
            sut = sut,
            expectedResult = RemoteCryptoFeed(remoteCryptoFeedResponse),
        )
    }

    @Test
    fun testGetFailsOnConnectivityError() {
        expect(sut = sut, expectedResult =  ConnectivityException())
    }


    @Test
    fun testGetFailsOn400HttpResponse() {
        expect(400, sut, BadRequestException())
    }

    @Test
    fun testGetFailsOn404HttpResponse() {
        expect(404, sut, NotFoundException())
    }
    @Test
    fun testGetFailsOn500HttpResponse() {
        expect(500, sut, InternalServerErrorException())
    }

    @Test
    fun testGetFailsOnInvalidDataError() {
        expect(withStatusCode = 422, sut = sut, expectedResult  = InvalidDataException())
    }
    @Test
    fun testGetFailsOnUnexpectedError() {
        expect(sut = sut, expectedResult  = UnexpectedException())
    }

    private fun expect(
        withStatusCode: Int? = null,
        sut: CryptoFeedRetrofitHttpClient,
        expectedResult: Any
    ) = runTest {
        when {
            withStatusCode != null -> {
                when(withStatusCode) {
                    200 -> {
                        coEvery {
                            service.get()
                        } returns (expectedResult as RemoteCryptoFeed)
                    }
                    else -> {
                        val response = Response.error<RemoteCryptoFeed>(withStatusCode, ResponseBody.create(null, ""))
                        coEvery {
                            service.get()
                        } throws HttpException(response)
                    }
                }
            }
            expectedResult is ConnectivityException -> {
                coEvery {
                    service.get()
                } throws IOException()
            }
        }

        sut.get().test {
            when(val receivedValue = awaitItem()) {
                is HttpClientResult.Success -> {
                    assertEquals((expectedResult as RemoteCryptoFeed)::class.java, receivedValue.root::class.java)
                    assertEquals(expectedResult.data, receivedValue.root.data)
                }
                is HttpClientResult.Failure -> {
                    assertEquals((expectedResult as Exception)::class.java, receivedValue.exception::class.java)
                }
            }
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}


