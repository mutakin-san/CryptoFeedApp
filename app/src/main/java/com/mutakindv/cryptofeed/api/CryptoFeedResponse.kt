package com.mutakindv.cryptofeed.api

import com.squareup.moshi.Json

class RootCryptoFeedResponse(
    @Json(name = "Data")
    val data: List<CryptoFeedResponse>)
class CryptoFeedResponse(
    @Json(name = "CoinInfo")
    val coinInfoResponse: CoinInfoResponse,
    @Json(name = "Raw")
    val rawResponse: RawResponse
)

data class CoinInfoResponse(

    @Json(name = "Id")
    val id: String,
    @Json(name = "Name")
    val name: String,
    @Json(name = "FullName")
    val fullName: String,
    @Json(name = "ImageUrl")
    val imageUrl: String,
)

data class RawResponse(
    @Json(name = "USD")
    val usdResponse: UsdResponse
)

class UsdResponse(
    @Json(name = "PRICE")
    val price: Double,
    @Json(name = "CHANGEPCTDAY")
    val changePctDay: Float
)