package com.mutakindv.cryptofeed

import com.mutakindv.cryptofeed.api.RemoteCoinInfo
import com.mutakindv.cryptofeed.api.RemoteCryptoFeedItem
import com.mutakindv.cryptofeed.api.RemoteDisplay
import com.mutakindv.cryptofeed.api.RemoteUsd

val remoteCryptoFeedResponse = listOf(
    RemoteCryptoFeedItem(
        remoteCoinInfo = RemoteCoinInfo("1", "BTC", "Bitcoin", "imageUrl"),
        remoteRaw = RemoteDisplay(
            usd = RemoteUsd(
                price = 1.0,
                changePctDay = 1F
            )
        )
    ),
    RemoteCryptoFeedItem(
        remoteCoinInfo = RemoteCoinInfo("2", "BTC2", "Bitcoin 2", "imageUrl"),
        remoteRaw = RemoteDisplay(
            usd = RemoteUsd(
                price = 2.0,
                changePctDay = 2F
            )
        )
    ),
)