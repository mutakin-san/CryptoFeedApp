package com.mutakindv.cryptofeed.api

class RootRemoteCryptoFeed(
    val data: List<RemoteCryptoFeed>)
class RemoteCryptoFeed(
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