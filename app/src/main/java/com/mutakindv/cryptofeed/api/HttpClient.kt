package com.mutakindv.cryptofeed.api

import kotlinx.coroutines.flow.Flow

sealed class HttpClientResult {
    data class Success(val root: RemoteCryptoFeed) : HttpClientResult()
    data class Failure(val exception: Exception) : HttpClientResult()
}
interface HttpClient{
    fun get(): Flow<HttpClientResult>
}
