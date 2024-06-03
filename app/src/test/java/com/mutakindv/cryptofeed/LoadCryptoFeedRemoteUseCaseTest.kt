package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.HttpClient
import com.mutakindv.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
    }
    @Test
    fun testLoadDeliverConnectivityErrorOnClientError() = runBlocking {
        every {
            client.get()
        } returns flowOf(Connectivity())

        sut.load().test{
            assertEquals(Connectivity::class.java, awaitItem()::class.java)
            awaitComplete()
        }


        verify(exactly = 1) {
            client.get()
        }

        confirmVerified(client)
    }


}
