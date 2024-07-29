package com.mutakindv.cryptofeed

import com.mutakindv.cryptofeed.api.CoinInfoResponse
import com.mutakindv.cryptofeed.api.CryptoFeedResponse
import com.mutakindv.cryptofeed.api.RawResponse
import com.mutakindv.cryptofeed.api.UsdResponse

val cryptoFeedResponses = listOf(
    CryptoFeedResponse(
        coinInfoResponse = CoinInfoResponse("1", "BTC", "Bitcoin", "imageUrl"),
        rawResponse = RawResponse(
            usdResponse = UsdResponse(
                price = 1.0,
                changePctDay = 1F
            )
        )
    ),
    CryptoFeedResponse(
        coinInfoResponse = CoinInfoResponse("2", "BTC 2", "Bitcoin 2", "imageUrl"),
        rawResponse = RawResponse(
            usdResponse = UsdResponse(
                price = 1.0,
                changePctDay = 1F
            )
        )
    ),
)