# rateAndPurchase

### install libs 

// cài maven 
``` sh
    maven("https://jitpack.io")
```
// cài gradle
 ```sh
    implementation("com.github.phamdat23:rateAndPurchase:1.0.0-beta1")
    implementation("com.android.billingclient:billing:7.1.1")
 ```

### Init Purchase

add this to create of your first activity
isDebug
true: mua purchase với thời gian test
false: mua purchase với thời gian live

hàm initIap   để check xem là ứng dụng  đã có iap hay chưa

PurchaseManager.isBought = true : đã có purchase
PurchaseManager.isBought = false: không có purchase

``` shell
   PurchaseManager.initIap(context = this, isDebug = true, object :PurchaseManager.InitIapListenner{
            override fun initPurcahseSussces(
                billingResult: com.android.billingclient.api.BillingResult,
                purchases: List<com.android.billingclient.api.Purchase>
            ) {
                lifecycleScope.launch(Dispatchers.Main){
                      if (PurchaseManager.isBought) {
                           // iap đã có 
                        } else {
                          // iap chưa có
                        }
                }
            }
        })
       
```

### query Purchase
listKey : truyền listId vào để query
isInapp: là chọn type  để query (true: type inapp, false: type sub)

``` shell

 PurchaseManager.queryDetailPurchase(context = this, listKey = listOf(), isInapp = true, queryIap = object : PurchaseManager.QueryPurChaseListenner{
            override fun updatePurchase() {
                lifecycleScope.launch(Dispatchers.Main){
                    // khi mua các gói thì sẽ update ở đây
                   if (PurchaseManager.isBought) {
                            Toast.makeText(this@IapActivity, "Success", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@IapActivity, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@IapActivity, "Failed purchase", Toast.LENGTH_SHORT)
                                .show()
                        }
                    
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
// hàm mua thì truyền 1 productDetail 
``` shell
   PurchaseManager.subscribePurchaseInapp(context = this, productDetails = productDetail)
```


### restore Purchase
// hàm  để restore lại xem có gói iap nào đã mua hay chưa
PurchaseManager.isBought = true : đã có purchase
PurchaseManager.isBought = false: không có purchase
``` shell
  Purchase.reStorePurchase(context = this, object : Purchase.InitIapListenner {
                override fun initPurcahseSussces(
                    billingResult: BillingResult,
                    purchases: List<com.android.billingclient.api.Purchase>
                ) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (PurchaseManager.isBought) {
                            Toast.makeText(
                                this@IapActivity,
                                getString(R.string.subscription_has_been_purchased),
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@IapActivity, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@IapActivity,
                                getString(R.string.subscription_has_not_been_purchased),
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                }
            })
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
