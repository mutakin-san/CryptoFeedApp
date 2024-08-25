package com.mutakindv.cryptofeed.cache

import com.mutakindv.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface CryptoFeedStore {

    fun deleteCache(): Flow<Exception?>

    fun insert(feeds: List<CryptoFeed>, timestamp: Date): Flow<Exception?>

}
