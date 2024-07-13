package com.mutakindv.cryptofeed.api

import kotlinx.coroutines.flow.Flow

interface CryptoFeedService {
    fun get(): Flow<HttpClientResult>
}
