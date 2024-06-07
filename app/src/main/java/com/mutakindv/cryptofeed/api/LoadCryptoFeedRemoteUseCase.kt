package com.mutakindv.cryptofeed.api

import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConnectivityException : Exception()
class InvalidDataException : Exception()
class BadRequestException : Exception()
class NotFoundException : Exception()
class InternalServerErrorException : Exception()

class LoadCryptoFeedRemoteUseCase(private val client: HttpClient) {
    fun load() : Flow<LoadCryptoFeedResult> = flow {
        client.get().collect { result ->
            when (result) {
                is HttpClientResult.Success -> {
                    emit(LoadCryptoFeedResult.Success(result.root.data.toModels()))
                }
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
                        is NotFoundException -> {
                            emit(LoadCryptoFeedResult.Error(NotFound()))
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


private fun List<RemoteCryptoFeedItem>.toModels(): List<CryptoFeed> {
    return map {
        CryptoFeed(
            coinInfo = CoinInfo(it.remoteCoinInfo.id, it.remoteCoinInfo.name, it.remoteCoinInfo.fullName, it.remoteCoinInfo.imageUrl),
            raw = Raw(usd = Usd(it.remoteRaw.usd.price, it.remoteRaw.usd.changePctDay))
        )
    }
}

class Connectivity : Exception()
class InvalidData : Exception()
class BadRequest : Exception()
class NotFound : Exception()
class InternalServerError : Exception()

