package com.mutakindv.cryptofeed.cache

import com.mutakindv.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow

interface CryptoFeedStore {

    fun deleteCache(): Flow<Exception?>

    fun insert(feeds: List<CryptoFeed>): Flow<Exception>

}
