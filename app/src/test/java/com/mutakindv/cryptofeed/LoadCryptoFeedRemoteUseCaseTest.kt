package com.mutakindv.cryptofeed

import org.junit.Assert.assertEquals
import org.junit.Test

class LoadCryptoFeedRemoteUseCaseTest {
    @Test
    fun testInitDoesNotLoad() {
        val client = HttpClient.instance
        LoadCryptoFeedRemoteUseCase()

        assert(client.getCount == 0)
    }

    @Test
    fun testLoadRequestData() {
        val client = HttpClient.instance
        val sut = LoadCryptoFeedRemoteUseCase()

        sut.load()

        assertEquals(1, client.getCount)

    }
}

class LoadCryptoFeedRemoteUseCase {
    fun load() {
        HttpClient.instance.getCount = 1
    }

}

class HttpClient private constructor(){

    var getCount: Int = 0

    companion object {
        val instance = HttpClient()
    }
}
