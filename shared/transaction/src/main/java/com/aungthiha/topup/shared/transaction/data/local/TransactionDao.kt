package com.aungthiha.topup.shared.transaction.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Every query is scoped by [TransactionEntity.userId] so a cached history is never visible to a
 * different account. Filter semantics mirror `public.list_my_transactions` on the backend.
 */
@Dao
internal interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId
          AND (:search = '' OR phone_number LIKE '%' || :search || '%' OR id LIKE '%' || :search || '%')
          AND (:filterByOperator = 0 OR operator_code IN (:operatorCodes))
          AND created_at >= :fromMillis AND created_at < :toMillis
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
        """,
    )
    fun observeNewestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE user_id = :userId
          AND (:search = '' OR phone_number LIKE '%' || :search || '%' OR id LIKE '%' || :search || '%')
          AND (:filterByOperator = 0 OR operator_code IN (:operatorCodes))
          AND created_at >= :fromMillis AND created_at < :toMillis
        ORDER BY created_at ASC, id ASC
        LIMIT :limit
        """,
    )
    fun observeOldestFirst(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
        limit: Int,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE user_id = :userId
          AND (:search = '' OR phone_number LIKE '%' || :search || '%' OR id LIKE '%' || :search || '%')
          AND (:filterByOperator = 0 OR operator_code IN (:operatorCodes))
          AND created_at >= :fromMillis AND created_at < :toMillis
        """,
    )
    fun observeCount(
        userId: String,
        search: String,
        filterByOperator: Boolean,
        operatorCodes: List<String>,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<Int>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND id = :transactionId")
    fun observeById(userId: String, transactionId: String): Flow<TransactionEntity?>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
