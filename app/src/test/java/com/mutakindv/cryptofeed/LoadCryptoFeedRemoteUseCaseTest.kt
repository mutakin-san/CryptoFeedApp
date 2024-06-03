package com.mutakindv.cryptofeed

import com.mutakindv.cryptofeed.api.BadResponse
import com.mutakindv.cryptofeed.api.Connectivity
import com.mutakindv.cryptofeed.api.HttpClient
import com.mutakindv.cryptofeed.api.LoadCryptoFeedRemoteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotRequestData() {

        val (_, client) = makeSut()

        assert(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {

        val (sut, client) = makeSut()
        sut.load()

        assertEquals(1, client.getCount)

    }
    @Test
    fun testLoadRequestDataTwice() {

        val (sut, client) = makeSut()
        sut.load()
        sut.load()

        assertEquals(2, client.getCount)

    }
    @Test
    fun testLoadDeliverErrorOnClientSendConnectivityError() = runBlocking {
        val (sut, client) = makeSut()

        client.error = Connectivity()
        var capturedError: Exception? = null
        sut.load().collect{ error ->
            capturedError = error
        }


        assertEquals(Connectivity::class.java, capturedError?.javaClass)
    }
    @Test
    fun testLoadDeliverErrorOnClientSendBadResponseError() = runBlocking {
        val (sut, client) = makeSut()

        client.error = BadResponse()
        var capturedError: Exception? = null
        sut.load().collect{ error ->
            capturedError = error
        }

        assertEquals(BadResponse::class.java, capturedError?.javaClass)
    }

    private fun makeSut() : Pair<LoadCryptoFeedRemoteUseCase, HttpClientSpy> {
        val client = HttpClientSpy()
        val sut = LoadCryptoFeedRemoteUseCase(client = client)
        return Pair(sut, client)
    }

    private class HttpClientSpy : HttpClient {
        var getCount: Int = 0
        var error: Exception? = null
        override fun get(): Flow<Exception> = flow {
            getCount += 1
            error?.let { emit(it) }
        }

    }
}
