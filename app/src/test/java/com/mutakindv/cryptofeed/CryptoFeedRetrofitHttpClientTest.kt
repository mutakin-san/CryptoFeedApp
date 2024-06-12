package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.BadRequestException
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.mutakindv.cryptofeed.api.CryptoFeedService
import com.mutakindv.cryptofeed.api.HttpClientResult
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
    fun testGetFailsOnConnectivityError() = runTest {
        every {
            service.get()
        } throws IOException()
        sut.get().test {
            when (val receivedValue = awaitItem()) {
                is HttpClientResult.Failure -> {
                    assertEquals(ConnectivityException()::class.java, receivedValue.exception::class.java)
                }

                else -> {}
            }
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }


    @Test
    fun testGetFailsOn400HttpResponse() = runTest {
        val response = Response.error<RemoteCryptoFeed>(400, ResponseBody.create(null, ""))
        coEvery {
            service.get()
        } throws HttpException(response)

        sut.get().test {
            val receivedValue = awaitItem() as HttpClientResult.Failure
            assertEquals(BadRequestException()::class.java, receivedValue.exception::class.java)
            awaitComplete()
        }

        coVerify(exactly = 1) {
            service.get()
        }
    }
}


