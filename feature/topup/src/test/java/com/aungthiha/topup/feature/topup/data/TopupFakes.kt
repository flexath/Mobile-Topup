package com.aungthiha.topup.feature.topup.data

import com.aungthiha.topup.core.common.session.CurrentUserProvider
import com.aungthiha.topup.core.datastore.UserPreferencesDataSource
import com.aungthiha.topup.core.datastore.model.AppLanguage
import com.aungthiha.topup.core.datastore.model.AppearanceMode
import com.aungthiha.topup.core.datastore.model.CachedWalletBalance
import com.aungthiha.topup.core.network.StorageUrlResolver
import com.aungthiha.topup.feature.topup.data.local.dao.CatalogDao
import com.aungthiha.topup.feature.topup.data.local.entity.CatalogProductEntity
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorEntity
import com.aungthiha.topup.feature.topup.data.local.dao.OperatorPrefixDao
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorPrefixEntity
import com.aungthiha.topup.feature.topup.data.local.entity.OperatorWithProducts
import com.aungthiha.topup.feature.topup.data.remote.response.CatalogProductDto
import com.aungthiha.topup.feature.topup.data.remote.response.OperatorDto
import com.aungthiha.topup.feature.topup.data.remote.response.OperatorPrefixDto
import com.aungthiha.topup.feature.topup.data.remote.response.RechargeRequestDto
import com.aungthiha.topup.feature.topup.data.remote.service.TopupRemoteService
import com.aungthiha.topup.feature.topup.data.remote.response.WalletDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

internal class FakeCatalogDao : CatalogDao {
    val operators = MutableStateFlow<List<OperatorEntity>>(emptyList())
    val products = MutableStateFlow<List<CatalogProductEntity>>(emptyList())
    var replaceCount = 0

    override fun observeCatalog(): Flow<List<OperatorWithProducts>> = operators.map { ops ->
        ops.sortedBy { it.sortOrder }.map { op -> OperatorWithProducts(op, products.value.filter { it.operatorCode == op.code }) }
    }

    override suspend fun operatorCount(): Int = operators.value.size

    override suspend fun insertOperators(operators: List<OperatorEntity>) {
        this.operators.value += operators
    }

    override suspend fun insertProducts(products: List<CatalogProductEntity>) {
        this.products.value += products
    }

    override suspend fun deleteAllOperators() {
        products.value = emptyList()
        operators.value = emptyList()
    }

    override suspend fun replaceCatalog(operators: List<OperatorEntity>, products: List<CatalogProductEntity>) {
        replaceCount++
        super.replaceCatalog(operators, products)
    }
}

internal class FakeOperatorPrefixDao : OperatorPrefixDao {
    val rows = MutableStateFlow<List<OperatorPrefixEntity>>(emptyList())
    var replaceCount = 0

    override fun observeAll(): Flow<List<OperatorPrefixEntity>> = rows

    override suspend fun getAll(): List<OperatorPrefixEntity> = rows.value

    override suspend fun count(): Int = rows.value.size

    override suspend fun insertAll(prefixes: List<OperatorPrefixEntity>) {
        rows.value = (rows.value + prefixes).sortedBy { it.prefix }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    override suspend fun replaceAll(prefixes: List<OperatorPrefixEntity>) {
        replaceCount++
        super.replaceAll(prefixes)
    }
}

internal class FakeTopupRemoteService : TopupRemoteService {
    /** Each call consumes the next handler, letting tests control ordering and failures. */
    val catalogResponses = ArrayDeque<suspend () -> List<OperatorDto>>()
    val prefixResponses = ArrayDeque<suspend () -> List<OperatorPrefixDto>>()
    var wallet: WalletDto? = WalletDto(balanceMmk = 500_000, currency = "MMK")
    var walletError: Throwable? = null
    var rechargeBody: suspend (RechargeRequestDto) -> String = { error("not stubbed") }
    var catalogCalls = 0
    var prefixCalls = 0

    override suspend fun fetchCatalog(): List<OperatorDto> {
        catalogCalls++
        return catalogResponses.removeFirst().invoke()
    }

    override suspend fun fetchOperatorPrefixes(): List<OperatorPrefixDto> {
        prefixCalls++
        return prefixResponses.removeFirst().invoke()
    }

    override suspend fun fetchWallet(): WalletDto? {
        walletError?.let { throw it }
        return wallet
    }

    override suspend fun processRecharge(request: RechargeRequestDto): String = rechargeBody(request)
}

internal class FakeStorage : StorageUrlResolver {
    override fun publicUrl(bucket: String, path: String) = "https://storage.test/$bucket/$path"
}

internal class FakeCurrentUser(initial: String? = "user-a") : CurrentUserProvider {
    val state = MutableStateFlow(initial)
    override val userId: StateFlow<String?> = state
}

internal class FakePreferences : UserPreferencesDataSource {
    val wallets = MutableStateFlow<Map<String, CachedWalletBalance>>(emptyMap())
    private val appearance = MutableStateFlow(AppearanceMode.SYSTEM)
    private val language = MutableStateFlow(AppLanguage.ENGLISH)

    override val appearanceMode: Flow<AppearanceMode> = appearance

    override suspend fun setAppearanceMode(mode: AppearanceMode) {
        appearance.value = mode
    }

    override val appLanguage: Flow<AppLanguage> = language

    override suspend fun setAppLanguage(language: AppLanguage) {
        this.language.value = language
    }

    override fun walletBalance(userId: String): Flow<CachedWalletBalance?> = wallets.map { it[userId] }

    override suspend fun saveWalletBalance(userId: String, balance: CachedWalletBalance) {
        wallets.value += (userId to balance)
    }

    override suspend fun clearWalletBalances() {
        wallets.value = emptyMap()
    }
}

internal fun operatorDto(code: String, productPrices: List<Long>, sortOrder: Int = 1) = OperatorDto(
    code = code,
    name = "$code full name",
    displayName = code,
    logoBucket = "telecom",
    logoPath = "${code.lowercase()}.jpeg",
    customAmountMinMmk = 1_000,
    customAmountMaxMmk = 50_000,
    customAmountStepMmk = 100,
    sortOrder = sortOrder,
    products = productPrices.mapIndexed { index, price ->
        CatalogProductDto(
            id = "$code-$price",
            sku = "$code-AIRTIME-$price",
            operatorCode = code,
            productType = "DENOMINATION",
            name = "Airtime top-up",
            priceMmk = price,
            sortOrder = index,
        )
    },
)
