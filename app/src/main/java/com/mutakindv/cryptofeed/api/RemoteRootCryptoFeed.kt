package com.mutakindv.cryptofeed.api

import com.squareup.moshi.Json


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