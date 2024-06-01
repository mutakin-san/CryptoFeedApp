package com.mutakindv.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClientSpy()
        LoadCryptoFeedRemoteUseCase()

        assert(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {
        val client = HttpClientSpy()
        HttpClient.instance = client
        val sut = LoadCryptoFeedRemoteUseCase()

        sut.load()

        assertEquals(1, client.getCount)

    }
}

class LoadCryptoFeedRemoteUseCase {
    fun load() {
        HttpClient.instance.get()
    }

}

open class HttpClient{
    companion object {
        var instance = HttpClient()
    }


    open fun get() {

    }
}


class HttpClientSpy : HttpClient() {
    var getCount: Int = 0

    override fun get() {
        getCount += 1
    }

}