package com.mutakindv.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotLoad() {

        val (_, client) = makeSut()

        assert(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {

        val (sut, client) = makeSut()
        sut.load()

        assertEquals(1, client.getCount)

    }

    private fun makeSut() : Pair<LoadCryptoFeedRemoteUseCase, HttpClientSpy> {
        val client = HttpClientSpy()
        val sut = LoadCryptoFeedRemoteUseCase(client = client)
        return Pair(sut, client)
    }

    private class HttpClientSpy : HttpClient {
        var getCount: Int = 0

        override fun get() {
            getCount += 1
        }

    }
}

class LoadCryptoFeedRemoteUseCase(private val client: HttpClient) {
    fun load() {
        client.get()
    }
}

interface HttpClient{
    fun get()
}

