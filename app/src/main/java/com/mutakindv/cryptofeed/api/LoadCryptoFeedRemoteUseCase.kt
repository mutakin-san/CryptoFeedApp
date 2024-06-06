package com.mutakindv.cryptofeed.api

import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
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
    fun load() : Flow<LoadCryptoFeedResult> = flow {
        client.get().collect { result ->
            when (result) {
                is HttpClientResult.Failure -> {
                    when(result.exception) {
                        is ConnectivityException -> {
                            emit(LoadCryptoFeedResult.Error(Connectivity()))
                        }
                        is InvalidDataException -> {
                            emit(LoadCryptoFeedResult.Error(InvalidData()))
                        }
                        is BadRequestException -> {
                            emit(LoadCryptoFeedResult.Error(BadRequest()))
                        }
                        is InternalServerErrorException -> {
                            emit(LoadCryptoFeedResult.Error(InternalServerError()))
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

