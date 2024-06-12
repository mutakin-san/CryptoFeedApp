package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.mutakindv.cryptofeed.api.CryptoFeedService
import com.mutakindv.cryptofeed.api.HttpClientResult
import com.mutakindv.cryptofeed.api.NotFoundException
import com.mutakindv.cryptofeed.api.RemoteCryptoFeed
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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


    private fun expect(
        withStatusCode: Int? = null,
        sut: CryptoFeedRetrofitHttpClient,
        expectedResult: Exception
    ) = runTest {
        when {
            withStatusCode != null -> {
                val response = Response.error<RemoteCryptoFeed>(withStatusCode, ResponseBody.create(null, ""))
                coEvery {
                    service.get()
                } throws HttpException(response)
            }
            expectedResult is ConnectivityException -> {
                coEvery {
                    service.get()
                } throws IOException()
            }
        }

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(expectedResult::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}


