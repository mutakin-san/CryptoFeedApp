package com.mutakindv.cryptofeed

import app.cash.turbine.test
import com.mutakindv.cryptofeed.cache.CacheCryptoFeedUseCase
import com.mutakindv.cryptofeed.cache.CryptoFeedStore
import com.mutakindv.cryptofeed.domain.CoinInfo
import com.mutakindv.cryptofeed.domain.CryptoFeed
import com.mutakindv.cryptofeed.domain.Raw
import com.mutakindv.cryptofeed.domain.Usd
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID



class CacheCryptoFeedUseCaseTest {

    private val store: CryptoFeedStore = spyk<CryptoFeedStore>()
    private lateinit var sut: CacheCryptoFeedUseCase


    private val feeds = listOf(
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

    private val timestamp = Date()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = CacheCryptoFeedUseCase(store, timestamp)
    }


    @Test
    fun testInitDoesNotDeleteCacheWhenCalled() = runTest {

        verify(exactly = 0) {
            store.deleteCache()
        }

        confirmVerified(store)

    }


    @Test
    fun testSaveCacheRequestDeletion() = runTest {

        every {
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds).test {
            assertEquals(Exception::class.java, awaitItem()!!::class.java)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        confirmVerified(store)
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
            store.deleteCache()
        } returns flowOf(Exception())

        sut.save(feeds).test {
            assertEquals(Exception::class.java, awaitItem()!!::class.java)
            awaitComplete()
        }
        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 0) {
            store.insert(feeds, timestamp)
        }

        confirmVerified(store)
    }


    @Test
    fun testSaveRequestNewCacheInsertionOnSuccessfulDeletion() = runTest {
        val captureFeeds = slot<List<CryptoFeed>>()
        val captureTimestamp = slot<Date>()

        every {
            store.deleteCache()
        } returns flowOf(null)

        every {
            store.insert(capture(captureFeeds), capture(captureTimestamp))
        } returns flowOf(null)


        sut.save(feeds).test {
            assertNull(awaitItem())
            assertEquals(feeds, captureFeeds.captured)
            awaitComplete()
        }

        verify(exactly = 1) {
            store.deleteCache()
        }

        verify(exactly = 1) {
            store.insert(feeds, timestamp)
        }

        verifyOrder {
            store.deleteCache()
            store.insert(feeds, timestamp)
        }

        confirmVerified(store)


    }

}