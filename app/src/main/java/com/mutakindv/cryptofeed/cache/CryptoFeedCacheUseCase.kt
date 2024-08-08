package com.mutakindv.cryptofeed.cache

import com.mutakindv.cryptofeed.domain.CryptoFeed

class CryptoFeedCacheUseCase(private val store: CryptoFeedStore) {
    fun save(feeds: List<CryptoFeed>) {
        store.deleteCache()
    }

}