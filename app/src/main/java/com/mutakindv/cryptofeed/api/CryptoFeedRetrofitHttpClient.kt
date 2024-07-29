package com.mutakindv.cryptofeed.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class CryptoFeedRetrofitHttpClient(private val service: CryptoFeedService): HttpClient {
    override fun get(): Flow<HttpClientResult> = flow {
        try {
            val result = service.get()
            emit(HttpClientResult.Success(toRemoteCryptoFeed(result)))
        } catch (exception: Exception) {
            when(exception) {
                is IOException -> {
                    emit(HttpClientResult.Failure(ConnectivityException()))
                }
                is HttpException -> {
                    when(exception.code()) {
                        400 -> {
                            emit(HttpClientResult.Failure(BadRequestException()))
                        }
                        404 -> {
                            emit(HttpClientResult.Failure(NotFoundException()))
                        }
                        422 -> {
                            emit(HttpClientResult.Failure(InvalidDataException()))
                        }
                        500 -> {
                            emit(HttpClientResult.Failure(InternalServerErrorException()))
                        }
                    }
                }
                else -> {
                    emit(HttpClientResult.Failure(UnexpectedException()))
                }
            }

        }
    }
    private fun toRemoteCryptoFeed(response: RootCryptoFeedResponse): RootRemoteCryptoFeed {
        return RootRemoteCryptoFeed( data = response.data.map {
            RemoteCryptoFeed(
                remoteCoinInfo = RemoteCoinInfo(
                    id = it.coinInfoResponse.id,
                    name = it.coinInfoResponse.name,
                    fullName = it.coinInfoResponse.fullName,
                    imageUrl = it.coinInfoResponse.imageUrl
                ),
                remoteRaw = RemoteDisplay(usd = RemoteUsd(
                    price = it.rawResponse.usdResponse.price,
                    changePctDay = it.rawResponse.usdResponse.changePctDay
                ))
            )
        } )
    }
}

