package com.mutakindv.cryptofeed.api


interface HttpClient{
    fun get()
}

class LoadCryptoFeedRemoteUseCase(private val client: HttpClient) {
    fun load() {
        client.get()
    }
}

