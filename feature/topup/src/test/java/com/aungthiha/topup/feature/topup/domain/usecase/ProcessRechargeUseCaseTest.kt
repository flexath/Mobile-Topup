package com.aungthiha.topup.feature.topup.domain.usecase

import com.aungthiha.topup.core.common.result.DataError
import com.aungthiha.topup.core.common.result.DataResult
import com.aungthiha.topup.core.common.result.NetworkError
import com.aungthiha.topup.feature.topup.domain.model.OperatorCode
import com.aungthiha.topup.feature.topup.domain.model.RechargeError
import com.aungthiha.topup.feature.topup.domain.model.RechargeReceipt
import com.aungthiha.topup.feature.topup.domain.model.RechargeRequest
import com.aungthiha.topup.feature.topup.domain.model.WalletBalance
import com.aungthiha.topup.feature.topup.domain.repository.RechargeRepository
import com.aungthiha.topup.feature.topup.domain.repository.WalletRepository
import com.aungthiha.topup.shared.transaction.domain.model.PurchasedProduct
import com.aungthiha.topup.shared.transaction.domain.model.RechargeProductType
import com.aungthiha.topup.shared.transaction.domain.model.Transaction
import com.aungthiha.topup.shared.transaction.domain.model.TransactionOperator
import com.aungthiha.topup.shared.transaction.domain.model.TransactionQuery
import com.aungthiha.topup.shared.transaction.domain.model.TransactionStatus
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionPageSync
import com.aungthiha.topup.shared.transaction.domain.repository.TransactionRepository
import com.aungthiha.topup.shared.transaction.domain.usecase.SaveConfirmedTransactionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessRechargeUseCaseTest {

    private val transactions = RecordingTransactionRepository()
    private val wallet = RecordingWalletRepository()
    private val request = RechargeRequest("09420000001", OperatorCode.MPT, "product", null, "key")

    @Test
    fun `success caches the backend transaction and backend balance`() = runTest {
        val receipt = RechargeReceipt(transaction(), walletBalanceMmk = 480_000, replayed = false)
        val useCase = ProcessRechargeUseCase(StubRechargeRepository(DataResult.Success(receipt)), wallet, SaveConfirmedTransactionUseCase(transactions))

        val result = useCase(request)

        assertEquals(DataResult.Success(receipt), result)
        assertEquals(listOf(receipt.transaction), transactions.saved)
        assertEquals(listOf(480_000L), wallet.cached)
    }

    @Test
    fun `insufficient balance stores nothing in history and never deducts locally`() = runTest {
        val error = RechargeError.InsufficientBalance(balanceMmk = 5_000, amountMmk = 20_000)
        val useCase = ProcessRechargeUseCase(StubRechargeRepository(DataResult.Error(error)), wallet, SaveConfirmedTransactionUseCase(transactions))

        useCase(request)

        assertTrue(transactions.saved.isEmpty())
        assertEquals(listOf(5_000L), wallet.cached)
    }

    @Test
    fun `network failure changes no local state`() = runTest {
        val useCase = ProcessRechargeUseCase(
            StubRechargeRepository(DataResult.Error(RechargeError.Network(NetworkError.TIMEOUT))),
            wallet,
            SaveConfirmedTransactionUseCase(transactions),
        )

        useCase(request)

        assertTrue(transactions.saved.isEmpty())
        assertTrue(wallet.cached.isEmpty())
    }

    private fun transaction() = Transaction(
        id = "tx-1",
        phoneNumber = "09420000001",
        operator = TransactionOperator("MPT", "MPT", null),
        product = PurchasedProduct(null, null, RechargeProductType.DENOMINATION, "Airtime top-up", null, null, emptyList(), null),
        amountMmk = 20_000,
        currency = "MMK",
        status = TransactionStatus.SUCCESS,
        balanceAfterMmk = 480_000,
        createdAtMillis = 0,
    )

    private class StubRechargeRepository(
        private val result: DataResult<RechargeReceipt, RechargeError>,
    ) : RechargeRepository {
        override suspend fun submit(request: RechargeRequest) = result
    }

    private class RecordingWalletRepository : WalletRepository {
        val cached = mutableListOf<Long>()
        override fun observeBalance(): Flow<WalletBalance?> = emptyFlow()
        override suspend fun refresh(): DataResult<WalletBalance, DataError> = error("unused")
        override suspend fun cacheConfirmedBalance(balanceMmk: Long) {
            cached += balanceMmk
        }
    }

    private class RecordingTransactionRepository : TransactionRepository {
        val saved = mutableListOf<Transaction>()
        override fun observeTransactions(query: TransactionQuery, limit: Int): Flow<List<Transaction>> = emptyFlow()
        override fun observeTransactionCount(query: TransactionQuery): Flow<Int> = emptyFlow()
        override fun observeTransaction(transactionId: String): Flow<Transaction?> = emptyFlow()
        override suspend fun syncPage(query: TransactionQuery, pageIndex: Int): DataResult<TransactionPageSync, DataError> = error("unused")
        override suspend fun saveConfirmedTransaction(transaction: Transaction) {
            saved += transaction
        }
        override suspend fun clearCache() = Unit
    }
}
