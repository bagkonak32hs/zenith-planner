package com.selcuk.zenithplanner.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "SubscriptionManager"
private const val MAX_RETRY_ATTEMPTS = 5
private const val RETRY_BASE_DELAY_MS = 2_000L

const val PRO_PRODUCT_ID = "pro_monthly_85"
const val FREE_NOTES_LIMIT = 5
const val FREE_GOALS_LIMIT = 5
const val FREE_FINANCE_LIMIT = 30

class SubscriptionManager(context: Context) : PurchasesUpdatedListener {

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var retryAttempt = 0

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    init {
        connect()
    }

    private fun connect() {
        retryAttempt = 0
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                retryAttempt = 0
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refreshPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max billing reconnect attempts reached")
            return
        }
        val delayMs = RETRY_BASE_DELAY_MS * (1L shl retryAttempt)
        retryAttempt++
        scope.launch {
            delay(delayMs)
            Log.d(TAG, "Reconnecting to billing (attempt $retryAttempt)")
            startConnection()
        }
    }

    fun refreshPurchases() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }
        scope.launch {
            val purchasesResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchasesResult.purchasesList)
            } else {
                Log.w(TAG, "queryPurchases failed: ${purchasesResult.billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        _isPro.value = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.contains(PRO_PRODUCT_ID)
        }
        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { purchase ->
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { r ->
                    Log.d(TAG, "Acknowledge result: ${r.responseCode}")
                }
            }
    }

    suspend fun launchBillingFlow(activity: Activity) {
        if (!billingClient.isReady) {
            startConnection()
            return
        }
        val details = queryProductDetails() ?: run {
            Log.w(TAG, "No product details found for $PRO_PRODUCT_ID")
            return
        }
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: run {
            Log.w(TAG, "No offer token available")
            return
        }
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
        )
    }

    private suspend fun queryProductDetails(): ProductDetails? = suspendCancellableCoroutine { cont ->
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        billingClient.queryProductDetailsAsync(
            params,
            ProductDetailsResponseListener { billingResult: BillingResult, queryResult: QueryProductDetailsResult ->
                cont.resume(
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                        queryResult.productDetailsList?.firstOrNull()
                    else null
                )
            }
        )
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else {
            Log.w(TAG, "onPurchasesUpdated: ${result.responseCode} — ${result.debugMessage}")
        }
    }

    fun endConnection() {
        scope.coroutineContext[Job]?.cancel()
        billingClient.endConnection()
    }
}
