package com.habithut.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BillingManager(private val context: Context, private val premiumAccessImpl: PremiumAccessImpl) : PurchasesUpdatedListener {
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    fun connect(onReady: () -> Unit = {}) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onReady()
                    queryActivePurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val productDetails = productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    private fun queryActivePurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { _, purchases ->
            val hasActive = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            premiumAccessImpl.setPremium(true)
                        }
                    }
                }
            }
            if (purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }) {
                premiumAccessImpl.setPremium(true)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { result ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            premiumAccessImpl.setPremium(true)
                        }
                    }
                }
            }
        }
    }

    fun premiumFlow(): Flow<Boolean> = callbackFlow {
        trySend(premiumAccessImpl.isPremium())
        awaitClose { }
    }

    companion object {
        const val PRODUCT_MONTHLY = "habit_hut_monthly"
        const val PRODUCT_YEARLY = "habit_hut_yearly"
    }
}