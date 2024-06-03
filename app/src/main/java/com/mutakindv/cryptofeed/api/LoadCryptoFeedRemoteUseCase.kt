package com.mutakindv.cryptofeed.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


interface HttpClient{
    fun get(): Flow<Exception>
}

class LoadCryptoFeedRemoteUseCase(private val client: HttpClient) {
    fun load() : Flow<Exception> = flow {
        client.get().collect { error ->
            if(error is Connectivity) {
                emit(Connectivity())
            }

            if(error is BadResponse) {
                emit(BadResponse())
            }
        }
    }
}


class Connectivity : Exception()
class BadResponse : Exception()

