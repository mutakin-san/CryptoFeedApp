package com.mutakindv.cryptofeed

import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd

val cryptoFeedItems = listOf(
    CryptoFeed(
        coinInfo = CoinInfo("1", "BTC", "Bitcoin", "imageUrl"),
        raw = Raw(
            usd = Usd(
                price = 1.0,
                changePctDay = 1f
            )
        )
    ),
    CryptoFeed(
        coinInfo = CoinInfo("2", "BTC2", "Bitcoin 2", "imageUrl"),
        raw = Raw(
            usd = Usd(
                price = 2.0,
                changePctDay = 2f
            )
        )
    )
)