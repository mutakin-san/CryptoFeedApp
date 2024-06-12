package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.ConnectivityException
import com.mutakindv.cryptofeed.api.CryptoFeedRetrofitHttpClient
import com.mutakindv.cryptofeed.api.CryptoFeedService
import com.mutakindv.cryptofeed.api.HttpClientResult
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
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

        coVerify {
            service.get()
        }
    }
}


