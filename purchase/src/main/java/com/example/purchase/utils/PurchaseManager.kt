package com.example.purchase.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import java.util.concurrent.TimeUnit

object PurchaseManager {
    private  var mListKey = listOf<String>()
    private var isIapTest: Boolean = true
    private  var mBillingClient: BillingClient? = null
    var isBought = false
    private var isEnableAds = true
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { _, _ -> }

    fun initIap(context: Context, isDebug: Boolean, listenner: InitIapListenner) {
        isIapTest = isDebug
        mBillingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        mBillingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                mBillingClient?.queryPurchasesAsync(params) { billingClient, purchases ->
                    if (checkTimeIap(context) || purchases.isNotEmpty()) {
                        isBought = true
                        isEnableAds = false
                        for (purchase in purchases) {
                            handlePurchase2(purchase)
                        }
                        listenner.initPurcahseSussces(billingClient, purchases)
                    } else {
                        val params2 = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                        mBillingClient?.queryPurchasesAsync(
                            params2
                        ) { _, purchasesSub ->
                            if (checkTimeIap(context)) {
                                isBought = true
                                isEnableAds = false
                                for (purchase in purchasesSub) {
                                    handlePurchase2(purchase)
                                }
                            } else {
                                isBought = false
                                isEnableAds = true
                            }
                            listenner.initPurcahseSussces(billingClient, purchases)
                        }
                    }
                }
            }
        })
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
            mBillingClient?.acknowledgePurchase(
                acknowledgePurchaseParams.build()
            ) {

            }
        }
    }

    private fun handlePurchase2(purchase: Purchase) {
        Log.e("===IAP", "handlePurchase2:isLifetime:$isLifetime ")
        if (isLifetime) {
            if (isIapTest) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    val prams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    mBillingClient?.consumeAsync(prams) { billing, purchaseToken ->
                    }
                }
            } else {
                handlePurchase(purchase)
            }

        } else {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val prams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                mBillingClient?.consumeAsync(prams) { billing, purchaseToken ->

                }

            }
        }

    }

    fun reStorePurchase(context: Context, listenner: InitIapListenner) {
        mBillingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                mBillingClient?.queryPurchasesAsync(params) { billingClient, purchases ->
                    if (billingClient.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (purchases.isNotEmpty() || checkTimeIap(context)) {
                            isBought = true
                            isEnableAds = false
                            for (purchase in purchases) {
                                handlePurchase2(purchase)
                            }
                            listenner.initPurcahseSussces(billingClient, purchases)
                        } else {
                            val paramsSub =
                                QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            mBillingClient?.queryPurchasesAsync(
                                paramsSub
                            ) { _, purchasesSub ->
                                if (purchasesSub.isNotEmpty() && checkTimeIap(context)) {
                                    isBought = true
                                    isEnableAds = false
                                    for (purchase in purchasesSub) {
                                        handlePurchase2(purchase)
                                    }
                                } else {
                                    isBought = false
                                    isEnableAds = true
                                }

                                listenner.initPurcahseSussces(billingClient, purchasesSub)


                            }
                        }
                    } else {
                        listenner.initPurcahseSussces(billingClient, purchases)
                    }

                }

            }
        })
    }

    private var currentProductId: String = ""
    var isLifetime = false

    fun queryDetailPurchase(
        context: Context,
        listKey: List<String>,
        isInapp: Boolean,
        queryIap: QueryPurChaseListenner,
    ) {
        mListKey = listKey.sortedBy { it }
        Log.d("===IAP", "queryDetailPurchase: $mListKey", )
        mBillingClient =
            BillingClient.newBuilder(context).enablePendingPurchases().setListener { p0, p1 ->
                if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
                    for (purchase in p1) {
                        handlePurchase2(purchase)
                    }
                    isBought = true
                    isEnableAds = false
                    setCurrentTimeBoughtIap(context)
                    setCurrentKeyIap(context, key = currentProductId)
                    queryIap.updatePurchase()
                } else {
                    isBought = false
                    isEnableAds = true
                }
            }.build()
        mBillingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
//                queryIap.queryFail()
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productList = ArrayList<QueryProductDetailsParams.Product>()
                    for (key in listKey) {
                        productList.add(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(key)
                                .setProductType(if (isInapp) BillingClient.ProductType.INAPP else BillingClient.ProductType.SUBS)
                                .build()
                        )
                    }
                    val params = QueryProductDetailsParams.newBuilder()
                    params.setProductList(productList)
                    mBillingClient?.queryProductDetailsAsync(params.build()) { billingResult1: BillingResult, productDetailsList: List<ProductDetails>? ->
                        if(!productDetailsList.isNullOrEmpty()){
                            val listNew = productDetailsList.sortedBy {
                                it.oneTimePurchaseOfferDetails?.priceAmountMicros
                            }
                            queryIap.querySussces(billingResult1, listNew)
                        }else{
                            queryIap.queryFail()
                        }

                    }
                } else {
                    queryIap.queryFail()
                }
            }
        })
    }

    private fun subscribePurchase(context: Context, productDetails: ProductDetails) {
        try {
            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
            val productDetailsParamsList =
                listOf(
                    offerToken?.let { it1 ->
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails).setOfferToken(it1)
                            .build()
                    }
                )
            currentProductId = productDetails.productId
            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
            val billingResult =
                mBillingClient?.launchBillingFlow(context as Activity, billingFlowParams)
            Log.d("===IAP", "lnMonthly: $offerToken|$billingResult")
        } catch (e: Exception) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun subscribePurchaseInapp(context: Context, productDetails: ProductDetails) {
        try {
            isLifetime = checkLifetime(productDetails.productId)
            Log.e("====IAP", "subscribePurchaseInapp: ${productDetails.productId}")
            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            currentProductId = productDetails.productId
            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
            val billingResult =
                mBillingClient?.launchBillingFlow(context as Activity, billingFlowParams)
            Log.d("===IAP", "lnMonthly: $billingResult")
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopConnect() {
        if (mBillingClient != null) {
            mBillingClient?.endConnection()
        }
    }


    interface InitIapListenner {
        fun initPurcahseSussces(billingResult: BillingResult, purchases: List<Purchase>)

    }

    interface QueryPurChaseListenner {
        fun updatePurchase()
        fun queryFail()
        fun querySussces(billingResult1: BillingResult, productDetailsList: List<ProductDetails>?)
    }

    fun checkLifetime(key: String): Boolean {
        return key.endsWith("20")
    }

    fun checkTimeIap(context: Context): Boolean {
        if (isIapTest) {
            return if (getCurrentKeyIap(context).endsWith("1")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toMillis(currentTime - timeBought) <= 1
            } else if (getCurrentKeyIap(context).endsWith("2")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                val check = TimeUnit.MILLISECONDS.toMillis(currentTime - timeBought) <= 2
                check
            } else if (getCurrentKeyIap(context).endsWith("5")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toMillis(currentTime - timeBought) <= 3
            } else if (getCurrentKeyIap(context).endsWith("10")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toMillis(currentTime - timeBought) <= 4
            } else if (getCurrentKeyIap(context).endsWith("20")) {
                true
            } else {
                false
            }
        } else {
            return if (getCurrentKeyIap(context).endsWith("1")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toDays(currentTime - timeBought) <= 3
            } else if (getCurrentKeyIap(context).endsWith("2")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                val check = TimeUnit.MILLISECONDS.toDays(currentTime - timeBought) <= 6
                check
            } else if (getCurrentKeyIap(context).endsWith("5")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toDays(currentTime - timeBought) <= 15
            } else if (getCurrentKeyIap(context).endsWith("10")) {
                val currentTime = System.currentTimeMillis()
                val timeBought = getCurrentTimeBoughtIap(context = context)
                TimeUnit.MILLISECONDS.toDays(currentTime - timeBought) <= 30
            } else if (getCurrentKeyIap(context).endsWith("20")) {
                true
            } else {
                false
            }
        }

    }

    private fun setCurrentTimeBoughtIap(context: Context) {
        val time = System.currentTimeMillis()
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        preferences.edit().putLong("time_bought_iap", time).apply()
    }


    private fun getCurrentTimeBoughtIap(context: Context): Long {
        val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return preferences.getLong(
            "time_bought_iap",
            0L
        )
    }

    private fun setCurrentKeyIap(context: Context, key: String) {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        preferences.edit().putString("current_key_iap", key).apply()
    }

    private fun getCurrentKeyIap(context: Context): String {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return preferences.getString("current_key_iap", "").toString()
    }

    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
    @JvmStatic
    fun formatPriceIap(p: String): String {
        return try {
            val str1 = p.trim().substring(0, 1)
            str1.toInt()
            val str = p.trim().substring(p.length - 1, p.length)
            val price = p.substring(0, p.length - 1)
            price + "" + str
        } catch (e: Exception) {
            val str = p.trim().substring(0, 1)
            val price = p.substring(1, p.length)
            price + "" + str
        }
    }
}

