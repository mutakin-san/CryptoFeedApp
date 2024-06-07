package com.mutakindv.cryptofeed.api

import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.LoadCryptoFeedResult
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd
import com.squareup.moshi.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class RemoteCryptoFeed(
    @Json(name = "Data")
    val data: List<RemoteCryptoFeedItem>)
class RemoteCryptoFeedItem(
    val remoteCoinInfo: RemoteCoinInfo,
    val remoteRaw: RemoteDisplay
)

data class RemoteCoinInfo(
    val id: String,
    val name: String,
    val fullName: String,
    val imageUrl: String,
)

data class RemoteDisplay(
    val usd: RemoteUsd
)

class RemoteUsd(
    val price: Double,
    val changePctDay: Float
)
sealed class HttpClientResult {
    data class Success(val root: RemoteCryptoFeed) : HttpClientResult()
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
class InternalServerError : Exception()

