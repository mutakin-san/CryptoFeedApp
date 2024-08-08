package com.mutakindv.cryptofeed.cache

import com.mutakindv.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CryptoFeedCacheUseCase(private val store: CryptoFeedStore) {
    fun save(feeds: List<CryptoFeed>) : Flow<Exception> = flow {
        store.deleteCache().collect {
        }
    }

}