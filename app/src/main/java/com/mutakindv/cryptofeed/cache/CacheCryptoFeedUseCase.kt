package com.mutakindv.cryptofeed.cache

import com.mutakindv.cryptofeed.domain.CryptoFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class CacheCryptoFeedUseCase(private val store: CryptoFeedStore, private val currentDate: Date) {
    fun save(feeds: List<CryptoFeed>): Flow<Exception?> = flow {
        store.deleteCache().collect { error ->
            if (error == null) {
                store.insert(feeds, currentDate).collect { insertionError ->
                    emit(insertionError)
                }
            } else {
                emit(error)
            }
        }
    }

}