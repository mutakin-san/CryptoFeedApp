package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api_infra.CryptoFeedResponse
import com.mutakindv.cryptofeed.api_infra.CryptoFeedRetrofitHttpClient
import com.mutakindv.cryptofeed.api_infra.CryptoFeedService
import com.mutakindv.cryptofeed.api.HttpClientResult
import com.mutakindv.cryptofeed.api.InternalServerErrorException
import com.mutakindv.cryptofeed.api.InvalidDataException
import com.mutakindv.cryptofeed.api.NotFoundException
import com.mutakindv.cryptofeed.api.RootRemoteCryptoFeed
import com.mutakindv.cryptofeed.api_infra.RootCryptoFeedResponse
import com.mutakindv.cryptofeed.api.UnexpectedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

        expect(
            sut= sut,
            expectedResult = HttpClientResult.Success(RootRemoteCryptoFeed(remoteCryptoFeedResponse)),
            receivedResult = RootCryptoFeedResponse(cryptoFeedResponses),
        )
    }

    @Test
    fun testLoadDeliverItemsOn200HttpResponseWithEmptyData() = runTest {
        val cryptoFeedResponse =  emptyList<CryptoFeedResponse>()
        expect(
            sut = sut,
            expectedResult = HttpClientResult.Success(RootRemoteCryptoFeed(emptyList())),
            receivedResult = RootCryptoFeedResponse(cryptoFeedResponse),
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
        expectedResult: Any,
        receivedResult: Any? = null,
    ) = runTest {
        when {
            withStatusCode != null -> {
                val response = Response.error<RootRemoteCryptoFeed>(withStatusCode, ResponseBody.create(null, ""))
                coEvery {
                    service.get()
                } throws HttpException(response)

            }
            expectedResult is ConnectivityException -> {
                coEvery {
                    service.get()
                } throws IOException()
            }
            expectedResult is HttpClientResult.Success -> {
                coEvery {
                    service.get()
                } returns (receivedResult as RootCryptoFeedResponse)
            }
        }

        sut.get().test {
            when(val receivedValue = awaitItem()) {
                is HttpClientResult.Success -> {
                    assertEquals(expectedResult, receivedValue)
                }
                is HttpClientResult.Failure -> {
                    assertEquals(expectedResult::class.java, receivedValue.exception::class.java)
                }
            }
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}


