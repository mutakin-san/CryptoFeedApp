package com.mutakindv.cryptofeed

import com.mutakindv.cryptofeed.api_infra.CoinInfoResponse
import com.mutakindv.cryptofeed.api_infra.CryptoFeedResponse
import com.mutakindv.cryptofeed.api_infra.RawResponse
import com.mutakindv.cryptofeed.api_infra.UsdResponse

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
        coinInfoResponse = CoinInfoResponse("2", "BTC2", "Bitcoin 2", "imageUrl"),
        rawResponse = RawResponse(
            usdResponse = UsdResponse(
                price = 2.0,
                changePctDay = 2F
            )
        )
    ),
)