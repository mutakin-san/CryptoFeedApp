package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.cache.CryptoFeedCacheUseCase
import com.mutakindv.cryptofeed.cache.CryptoFeedStore
import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID



class CryptoFeedStoreUseCaseTest {

    private val cache: CryptoFeedStore = spyk<CryptoFeedStore>()
    private lateinit var sut: CryptoFeedCacheUseCase


    val feeds = listOf(
        CryptoFeed(
            coinInfo = CoinInfo(UUID.randomUUID().toString(), "BTC", "Bitcoin", "imageUrl"),
            raw = Raw(
                usd = Usd(
                    price = 1.0,
                    changePctDay = 1f
                )
            )
        ),
        CryptoFeed(
            coinInfo = CoinInfo(UUID.randomUUID().toString(), "BTC2", "Bitcoin 2", "imageUrl"),
            raw = Raw(
                usd = Usd(
                    price = 2.0,
                    changePctDay = 2f
                )
            )
        )
    )

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


    @Test
    fun testSaveCacheRequestDeletion() = runTest {

        every {
            cache.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            cache.deleteCache()
        }

        confirmVerified(cache)
    }

    @Test
    fun testSaveDoesNotRequestCacheInsertionOnDeletionError() = runTest {

        val feeds = listOf(
            CryptoFeed(
                coinInfo = CoinInfo(UUID.randomUUID().toString(), "BTC", "Bitcoin", "imageUrl"),
                raw = Raw(
                    usd = Usd(
                        price = 1.0,
                        changePctDay = 1f
                    )
                )
            ),
            CryptoFeed(
                coinInfo = CoinInfo(UUID.randomUUID().toString(), "BTC2", "Bitcoin 2", "imageUrl"),
                raw = Raw(
                    usd = Usd(
                        price = 2.0,
                        changePctDay = 2f
                    )
                )
            )
        )

        every {
            cache.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds).test {
            awaitComplete()
        }
        verify(exactly = 1) {
            cache.deleteCache()
        }

        verify(exactly = 0) {
            cache.insert()
        }

        confirmVerified(cache)
    }


    @Test
    fun testSaveRequestNewCacheInsertionOnSuccessfulDeletion() = runTest {
        every {
            cache.deleteCache()
        } returns flowOf(null)

        every {
            cache.insert()
        } returns flowOf()


        sut.save(feeds).test {
            awaitComplete()
        }

        verify(exactly = 1) {
            cache.deleteCache()
        }

        verify(exactly = 1) {
            cache.insert()
        }

        confirmVerified(cache)


    }

}