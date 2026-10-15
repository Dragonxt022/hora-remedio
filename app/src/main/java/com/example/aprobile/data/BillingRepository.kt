package com.example.aprobile.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams

/**
 * Integração com o Google Play Billing para a assinatura Premium.
 *
 * Enquanto os produtos não estiverem publicados na Play Store, a consulta
 * retorna vazio e o fluxo cai no fallback local (ver [grantPremiumFallback]).
 */
class BillingRepository(private val context: Context) {

    private var billingClient: BillingClient? = null

    var onEntitlement: (() -> Unit)? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledge(purchase)
                    onEntitlement?.invoke()
                }
            }
        }
    }

    fun connectAndPurchase(activity: Activity, onUnavailable: () -> Unit) {
        val client = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient = client

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAndLaunch(activity, onUnavailable)
                } else {
                    onUnavailable()
                }
            }

            override fun onBillingServiceDisconnected() {
                onUnavailable()
            }
        })
    }

    fun restorePurchase(onUnavailable: () -> Unit) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onUnavailable()
            return
        }
        client.queryPurchasesAsync(
            com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { _, purchases ->
            val active = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            if (active) onEntitlement?.invoke() else onUnavailable()
        }
    }

    private fun queryAndLaunch(activity: Activity, onUnavailable: () -> Unit) {
        val client = billingClient ?: run { onUnavailable(); return }

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { billingResult, details ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !details.isNullOrEmpty()) {
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(details.first())
                                .build()
                        )
                    )
                    .build()
                activity.runOnUiThread {
                    client.launchBillingFlow(activity, flowParams)
                }
            } else {
                onUnavailable()
            }
        }
    }

    private fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params) { }
    }

    companion object {
        const val PRODUCT_ID = "premium_monthly"
    }
}
