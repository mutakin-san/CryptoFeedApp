package com.mutakindv.cryptofeed.cache

import kotlinx.coroutines.flow.Flow

interface CryptoFeedStore {

    fun deleteCache(): Flow<Exception?>

    fun insert(): Flow<Exception>

}
