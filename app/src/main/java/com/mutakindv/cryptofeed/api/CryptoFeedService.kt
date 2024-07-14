package com.mutakindv.cryptofeed.api


interface CryptoFeedService {
    suspend fun get(): RemoteCryptoFeed
}
