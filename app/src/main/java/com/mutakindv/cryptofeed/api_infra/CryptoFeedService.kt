package com.mutakindv.cryptofeed.api_infra


interface CryptoFeedService {
    suspend fun get(): RootCryptoFeedResponse
}
