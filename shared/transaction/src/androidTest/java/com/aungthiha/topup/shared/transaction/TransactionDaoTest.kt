package com.aungthiha.topup.shared.transaction

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aungthiha.topup.shared.transaction.data.local.TransactionDao
import com.aungthiha.topup.shared.transaction.data.local.TransactionDatabase
import com.aungthiha.topup.shared.transaction.data.local.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: TransactionDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TransactionDatabase::class.java).build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() = database.close()

    private suspend fun newestFirst(
        userId: String = USER_A,
        search: String = "",
        operators: List<String> = emptyList(),
        from: Long = Long.MIN_VALUE,
        to: Long = Long.MAX_VALUE,
        limit: Int = 100,
    ) = dao.observeNewestFirst(userId, search, operators.isNotEmpty(), operators.ifEmpty { listOf("") }, from, to, limit).first()

    @Test
    fun upsertInsertsAndReplacesById() = runTest {
        dao.upsertAll(listOf(entity("tx-1", amount = 1_000)))
        dao.upsertAll(listOf(entity("tx-1", amount = 2_000)))

        val rows = newestFirst()
        assertEquals(1, rows.size)
        assertEquals(2_000L, rows.single().amountMmk)
    }

    @Test
    fun queriesAreScopedToTheUser() = runTest {
        dao.upsertAll(listOf(entity("a-1"), entity("b-1", userId = USER_B)))

        assertEquals(listOf("a-1"), newestFirst(USER_A).map { it.id })
        assertEquals(listOf("b-1"), newestFirst(USER_B).map { it.id })
        assertNull(dao.observeById(USER_B, "a-1").first())
    }

    @Test
    fun searchMatchesPartialPhoneOrTransactionId() = runTest {
        dao.upsertAll(
            listOf(
                entity("aaaa-1111", phone = "09771234567"),
                entity("bbbb-2222", phone = "09420000001"),
            ),
        )

        assertEquals(listOf("aaaa-1111"), newestFirst(search = "771234").map { it.id })
        assertEquals(listOf("bbbb-2222"), newestFirst(search = "bbbb").map { it.id })
        assertEquals(2, newestFirst(search = "09").size)
    }

    @Test
    fun operatorAndDateFiltersCombine() = runTest {
        dao.upsertAll(
            listOf(
                entity("mpt-old", operator = "MPT", createdAt = 100),
                entity("atom-old", operator = "ATOM", createdAt = 100),
                entity("atom-new", operator = "ATOM", createdAt = 500),
                entity("mytel-new", operator = "MYTEL", createdAt = 500),
            ),
        )

        assertEquals(listOf("atom-new", "mytel-new").sorted(), newestFirst(operators = listOf("ATOM", "MYTEL"), from = 200, to = 600).map { it.id }.sorted())
        assertEquals(listOf("atom-old"), newestFirst(operators = listOf("ATOM"), from = 0, to = 200).map { it.id })
        assertEquals(
            2,
            dao.observeCount(USER_A, "", true, listOf("ATOM"), Long.MIN_VALUE, Long.MAX_VALUE).first(),
        )
    }

    @Test
    fun sortingAndPaginationAreDeterministic() = runTest {
        // Two rows share a timestamp: the id breaks the tie in both directions.
        dao.upsertAll((1..25).map { entity("tx-%02d".format(it), createdAt = (it / 2).toLong()) })

        val newest = newestFirst(limit = 20)
        assertEquals(20, newest.size)
        assertEquals("tx-25", newest.first().id)
        assertEquals(newest.sortedWith(compareByDescending<TransactionEntity> { it.createdAtMillis }.thenByDescending { it.id }), newest)

        val oldest = dao.observeOldestFirst(USER_A, "", false, listOf(""), Long.MIN_VALUE, Long.MAX_VALUE, 20).first()
        assertEquals("tx-01", oldest.first().id)
        assertEquals(oldest.sortedWith(compareBy<TransactionEntity> { it.createdAtMillis }.thenBy { it.id }), oldest)

        assertEquals(25, newestFirst(limit = 40).size)
        assertEquals(25, dao.observeCount(USER_A, "", false, listOf(""), Long.MIN_VALUE, Long.MAX_VALUE).first())
    }

    @Test
    fun deleteAllClearsEveryUsersCache() = runTest {
        dao.upsertAll(listOf(entity("a-1"), entity("b-1", userId = USER_B)))

        dao.deleteAll()

        assertEquals(0, newestFirst(USER_A).size + newestFirst(USER_B).size)
    }

    @Test
    fun servicesListRoundTrips() = runTest {
        dao.upsertAll(listOf(entity("pkg", services = listOf("YouTube", "TikTok"))))

        assertEquals(listOf("YouTube", "TikTok"), dao.observeById(USER_A, "pkg").first()?.services)
    }

    private fun entity(
        id: String,
        userId: String = USER_A,
        phone: String = "09420000001",
        operator: String = "MPT",
        amount: Long = 1_000,
        createdAt: Long = 0,
        services: List<String> = emptyList(),
    ) = TransactionEntity(
        id = id,
        userId = userId,
        phoneNumber = phone,
        operatorCode = operator,
        operatorName = operator,
        operatorLogoUrl = null,
        productId = null,
        productSku = null,
        productType = "DENOMINATION",
        productName = "Airtime top-up",
        dataAllowanceMb = null,
        callMinutes = null,
        services = services,
        validityDays = null,
        amountMmk = amount,
        currency = "MMK",
        status = "SUCCESS",
        balanceAfterMmk = 0,
        createdAtMillis = createdAt,
    )

    private companion object {
        const val USER_A = "user-a"
        const val USER_B = "user-b"
    }
}
