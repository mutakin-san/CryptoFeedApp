package com.mutakindv.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClientSpy()
        LoadCryptoFeedRemoteUseCase(client = client)

        assert(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {
        val client = HttpClientSpy()
        val sut = LoadCryptoFeedRemoteUseCase(client = client)

        sut.load()

        assertEquals(1, client.getCount)

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


class HttpClientSpy : HttpClient {
    var getCount: Int = 0

    override fun get() {
        getCount += 1
    }

}