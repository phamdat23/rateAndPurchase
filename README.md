# rateAndPurchase

### install libs 

// add maven 
``` sh
  maven("https://jitpack.io")
```
// add gradle
 ```sh
    
    implementation("com.github.phamdat23:rateAndPurchase:1.0.0")
 ```

### Init Purchase

add this to create of your first activity
isDebug
true: mua purchase với thời gian test
false: mua purchase với thời gian live


PurchaseManager.isBought = true : đã có purchase
PurchaseManager.isBought = false: không có purchase
``` shell


   PurchaseManager.initIap(context = this, isDebug = true, object :PurchaseManager.InitIapListenner{
            override fun initPurcahseSussces(
                billingResult: com.android.billingclient.api.BillingResult,
                purchases: List<com.android.billingclient.api.Purchase>
            ) {
                lifecycleScope.launch(Dispatchers.Main){
                    //
                }
            }
        })
       
```

### query Purchase

``` shell

 PurchaseManager.queryDetailPurchase(context = this, listKey = listOf(), isInapp = true, queryIap = object : PurchaseManager.QueryPurChaseListenner{
            override fun updatePurchase() {
                lifecycleScope.launch(Dispatchers.Main){
                    // khi mua các gói thì sẽ update ở đây
                    
                    
                }
            }

            override fun queryFail() {
                lifecycleScope.launch(Dispatchers.Main){
                    // 
                }
            }

            override fun querySussces(
                billingResult1: com.android.billingclient.api.BillingResult,
                productDetailsList: List<com.android.billingclient.api.ProductDetails>?
            ) {
                lifecycleScope.launch(Dispatchers.Main){
                    //
                }
            }
        })

```

### subscribe Purchase

``` shell
   PurchaseManager.subscribePurchaseInapp(context = this, productDetails = productDetail)
```

### create dialog rate

```shell
        val ratingDialog: RatingDialog = RatingDialog.Builder(this)
            .session(1)
            .date(1)
            .setNameApp(getString(R.string.app_name))
            .setIcon(R.mipmap.ic_launcher)
            .setEmail("vapp.helpcenter@gmail.com")
            .isShowButtonLater(true)
            .isClickLaterDismiss(true)
            .setOnlickRate { rate ->
                Toast.makeText(
                    this@MainActivity,
                    "Rate$rate",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setTextButtonLater("Maybe Later")
            .setOnlickMaybeLate {
                Toast.makeText(
                    this@MainActivity,
                    "Feedback cannot be left blank",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .ratingButtonColor(R.color.black)
            .build()

        //Cancel On Touch Outside
        ratingDialog.setCanceledOnTouchOutside(false)
        //show
        ratingDialog.show()

```
