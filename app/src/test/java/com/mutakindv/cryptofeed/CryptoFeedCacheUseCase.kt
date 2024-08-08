package com.mutakindv.cryptofeed

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


interface CryptoFeedCache {

    fun deleteCache()

}

class CryptoFeedCacheUseCase(private val cache: CryptoFeedCache) {

}

class CryptoFeedCacheUseCaseTest {

    private val cache: CryptoFeedCache = mockk<CryptoFeedCache>()
    private lateinit var sut: CryptoFeedCacheUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = CryptoFeedCacheUseCase(cache)
    }


    @Test
    fun testInitDoesNotDeleteCacheWhenCalled() = runTest {

        verify(exactly = 0) {
            cache.deleteCache()
        }

        confirmVerified(cache)

    }
}