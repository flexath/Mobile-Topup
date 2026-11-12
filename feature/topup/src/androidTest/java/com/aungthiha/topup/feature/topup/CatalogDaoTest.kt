package com.aungthiha.topup.feature.topup

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aungthiha.topup.feature.topup.data.local.dao.CatalogDao
import com.aungthiha.topup.feature.topup.data.local.CatalogDatabase
import com.aungthiha.topup.feature.topup.data.local.entity.CatalogProductEntity
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorEntity

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogDaoTest {

    private lateinit var database: CatalogDatabase
    private lateinit var dao: CatalogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CatalogDatabase::class.java).build()
        dao = database.catalogDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun observesOperatorsWithTheirProductsInSortOrder() = runTest {
        dao.replaceCatalog(
            operators = listOf(operator("ATOM", 2), operator("MPT", 1)),
            products = listOf(product("MPT", 1_000), product("MPT", 2_000), product("ATOM", 1_000)),
        )

        val catalog = dao.observeCatalog().first()

        assertEquals(listOf("MPT", "ATOM"), catalog.map { it.operator.code })
        assertEquals(setOf(1_000L, 2_000L), catalog.first().products.map { it.priceMmk }.toSet())
        assertEquals(listOf("ATOM"), catalog.last().products.map { it.operatorCode })
    }

    @Test
    fun replaceCatalogSwapsEverythingAtomically() = runTest {
        dao.replaceCatalog(listOf(operator("MPT", 1)), listOf(product("MPT", 1_000)))

        dao.replaceCatalog(listOf(operator("MYTEL", 1)), listOf(product("MYTEL", 700)))

        val catalog = dao.observeCatalog().first()
        assertEquals(listOf("MYTEL"), catalog.map { it.operator.code })
        assertEquals(listOf(700L), catalog.single().products.map { it.priceMmk })
    }

    @Test
    fun failedReplacementRollsBackAndKeepsTheOldCatalog() = runTest {
        dao.replaceCatalog(listOf(operator("MPT", 1)), listOf(product("MPT", 1_000)))

        try {
            // Product references an operator that is not inserted: the FK fails mid-transaction.
            dao.replaceCatalog(listOf(operator("ATOM", 1)), listOf(product("OOREDOO", 1_000)))
            fail("Expected a foreign key failure")
        } catch (_: SQLiteConstraintException) {
        }

        assertEquals(listOf("MPT"), dao.observeCatalog().first().map { it.operator.code })
    }

    @Test
    fun deletingOperatorsCascadesToProducts() = runTest {
        dao.replaceCatalog(listOf(operator("MPT", 1)), listOf(product("MPT", 1_000)))

        dao.deleteAllOperators()
        dao.insertOperators(listOf(operator("MPT", 1)))

        assertEquals(emptyList<CatalogProductEntity>(), dao.observeCatalog().first().single().products)
    }

    private fun operator(code: String, sortOrder: Int) = OperatorEntity(
        code = code,
        displayName = code,
        fullName = code,
        logoUrl = null,
        customAmountMinMmk = 1_000,
        customAmountMaxMmk = 50_000,
        customAmountStepMmk = 100,
        sortOrder = sortOrder,
    )

    private fun product(operatorCode: String, price: Long) = CatalogProductEntity(
        id = "$operatorCode-$price",
        sku = "$operatorCode-AIRTIME-$price",
        operatorCode = operatorCode,
        productType = "DENOMINATION",
        name = "Airtime top-up",
        priceMmk = price,
        dataAllowanceMb = null,
        callMinutes = null,
        services = emptyList(),
        validityDays = null,
        isDemo = true,
        sortOrder = 0,
    )
}
