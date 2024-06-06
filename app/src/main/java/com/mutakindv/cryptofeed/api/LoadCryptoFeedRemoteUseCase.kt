package com.mutakindv.cryptofeed.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

sealed class HttpClientResult {
    data class Failure(val exception: Exception) : HttpClientResult()
}
interface HttpClient{
    fun get(): Flow<HttpClientResult>
}

class ConnectivityException : Exception()
class InvalidDataException : Exception()
class BadRequestException : Exception()
class InternalServerErrorException : Exception()

class LoadCryptoFeedRemoteUseCase(private val client: HttpClient) {
    fun load() : Flow<Exception> = flow {
        client.get().collect { result ->
            when (result) {
                is HttpClientResult.Failure -> {
                    when(result.exception) {
                        is ConnectivityException -> {
                            emit(Connectivity())
                        }
                        is InvalidDataException -> {
                            emit(InvalidData())
                        }
                        is BadRequestException -> {
                            emit(BadRequest())
                        }
                        is InternalServerErrorException -> {
                            emit(InternalServerError())
                        }
                    }
                }

            }

        }
    }
}


class Connectivity : Exception()
class InvalidData : Exception()
class BadRequest : Exception()
class InternalServerError : Exception()

