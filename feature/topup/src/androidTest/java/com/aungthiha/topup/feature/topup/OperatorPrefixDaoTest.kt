package com.aungthiha.topup.feature.topup

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aungthiha.topup.feature.topup.data.local.OperatorPrefixDatabase
import com.aungthiha.topup.feature.topup.data.local.OperatorPrefixEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OperatorPrefixDaoTest {

    private lateinit var database: OperatorPrefixDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OperatorPrefixDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceAllSwapsPrefixesAtomically() = runTest {
        val dao = database.operatorPrefixDao()
        dao.replaceAll(listOf(OperatorPrefixEntity("0942", "MPT")))
        dao.replaceAll(listOf(OperatorPrefixEntity("0977", "ATOM"), OperatorPrefixEntity("0968", "MYTEL")))

        assertEquals(
            listOf(
                OperatorPrefixEntity("0968", "MYTEL"),
                OperatorPrefixEntity("0977", "ATOM"),
            ),
            dao.observeAll().first(),
        )
    }
}
