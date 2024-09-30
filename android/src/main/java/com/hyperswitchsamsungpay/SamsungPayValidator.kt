package com.hyperswitchsamsungpay

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableArray
import com.hyperswitchsamsungpay.SPaySheetControlUtils.Companion.makeAmountControl
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AddressControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.SheetItemType
import org.json.JSONArray
import org.json.JSONObject


class SamsungPayValidator {

  companion object {
    lateinit var context: Context
    lateinit var samsungPay: SamsungPay
    lateinit var partnerInfo: PartnerInfo
    lateinit var bundle: Bundle
    lateinit var samsungPayDTO: SamsungPayDTO

    @JvmStatic
    fun setSamsungPayContext(context: Context) {
      this.context = context
    }

    @JvmStatic
    fun parseSamsungPayInfo(requestObj: String, callback: Callback) {

//      Log.i("SamsungPayJSON", requestObj)

      try {
        val jsonObject = JSONObject(requestObj)
        val serviceId = jsonObject.get("service_id").toString()
        val orderNo = jsonObject.get("order_number").toString()
        val protocol = jsonObject.get("protocol").toString()

        val merchantObj = JSONObject(jsonObject.get("merchant").toString())

        val merchantName = merchantObj.get("name").toString()
        val merchantUrl = merchantObj.get("url").toString()
        val merchantCountryCode = merchantObj.get("country_code").toString()
        val merchant = Merchant(merchantName, merchantUrl, merchantCountryCode)


        val amountObj = JSONObject(jsonObject.get("amount").toString())
        val amountOption = amountObj.get("option").toString()
        val currencyCode = amountObj.get("currency_code").toString()
        val total = amountObj.get("total").toString()

        val amount = Amount(amountOption, currencyCode, total)
        val allowedBrandsArray = JSONArray(jsonObject.get("allowed_brands").toString())

        this.samsungPayDTO =
          SamsungPayDTO(serviceId, orderNo, merchant, amount, protocol, allowedBrandsArray)
      } catch (exception: Exception) {
        val map = Arguments.createMap()
        map.putString("status", "Error")
        map.putString("message", exception.message.toString())
        callback.invoke(map)

        throw exception

      }

    }


    @JvmStatic
    fun checkSamsungPayStatus(callback: Callback) {
      val map = Arguments.createMap()
      val bundle = Bundle()
      bundle.putString(
        SamsungPay.PARTNER_SERVICE_TYPE,
        SpaySdk.ServiceType.INAPP_PAYMENT.toString()
      )
      this.bundle = bundle
      this.partnerInfo = PartnerInfo(this.samsungPayDTO.serviceId, bundle)
      samsungPay = SamsungPay(context, partnerInfo)

      samsungPay.getSamsungPayStatus(object : StatusListener {
        override fun onSuccess(status: Int, bundle: Bundle?) {
          when (status) {
            SamsungPay.SPAY_NOT_SUPPORTED -> {
              // Samsung Pay is not supported
//              Log.i("SPAY", "not supported")
              map.putString("status", "failure")
              map.putString("message", "Samsung Pay is not supported on this device")
              callback.invoke(map)
            }


            SamsungPay.SPAY_NOT_READY -> {
              // Activate Samsung Pay or update Samsung Pay, if needed
              val errorReason = bundle?.getInt(SamsungPay.EXTRA_ERROR_REASON)
              if (errorReason == SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED) {
                // Display an appropriate popup message to the user
                samsungPay.activateSamsungPay()
              } else if (errorReason == SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE) {
                // Display an appropriate popup message to the user
                samsungPay.goToUpdatePage()
              } else {
                Toast.makeText(context, "error reason: $errorReason", Toast.LENGTH_LONG).show()
              }

              map.putString("status", "failure")
              map.putString("message", "Samsung Pay is not ready")
              callback.invoke(map)
            }

            SamsungPay.SPAY_READY -> {
//              Log.i("SPAY", "ready")
              map.putString("status", "success")
              map.putString("message", "Samsung Pay is ready")
              callback.invoke(map)

            }


            else ->{
//              Log.i("SPAY", "ready")
              map.putString("status", "failure")
              map.putString("message", "Samsung Pay is not ready")
              callback.invoke(map)

            }
            // Not expected result
          }
        }

        override fun onFail(status: Int, bundle: Bundle?) {
          map.putString("status", "failure")
          map.putString("message", "Samsung Pay is not ready")
          callback.invoke(map)
//          Log.i("SPAY", "failed!!!!")
        }

      })
    }

    @JvmStatic
    fun activateSamsungPay(callback: Callback) {
      val map = Arguments.createMap()
      map.putString("status", "success")
      map.putString("message", "Samsung Pay SDK activated")
      samsungPay.activateSamsungPay()
      callback.invoke(map)

    }

    @JvmStatic
    fun requestCardInfo(callback: Callback) {
      val supportedCardBrandsMap = Arguments.createMap()
      val statusMap = Arguments.createMap()
      val bundle = Bundle()
      bundle.putString(
        SamsungPay.PARTNER_SERVICE_TYPE,
        SpaySdk.ServiceType.INAPP_PAYMENT.toString()
      )

      val partnerInfo = PartnerInfo(samsungPayDTO.serviceId, bundle)
      val paymentManager = PaymentManager(context, partnerInfo)
      val cardInfoListener = object : PaymentManager.CardInfoListener {
        override fun onResult(p0: MutableList<CardInfo>?) {
          var cardBrandsArray = Arguments.createArray()

          if (p0 != null) {
            for (item in p0) {
              cardBrandsArray.pushString(item.brand.toString())
//              Log.i("SPAY_CARDS", item.brand.toString())
            }
          }
//          Log.i("SPAY_CARDS", p0.toString())
          statusMap.putString("status", "success")
          statusMap.putString("message", "Samsung Pay SDK fetched card brands successfully")
          supportedCardBrandsMap.putArray("cardBrands", cardBrandsArray)
          callback.invoke(statusMap, supportedCardBrandsMap)
        }

        override fun onFailure(p0: Int, p1: Bundle?) {
          statusMap.putString("status", "failure")
          statusMap.putString("message", "Samsung Pay SDK could not fetch card brands")
          callback.invoke(statusMap)
        }

      }

      paymentManager.requestCardInfo(bundle, cardInfoListener)
    }

    fun presentSamsungPayPaymentSheet(callback: Callback) {
      val paymentManager = PaymentManager(context, partnerInfo)
      val map = Arguments.createMap()


      val customSheet = CustomSheet()
      val amountBoxControl = makeAmountControl(samsungPayDTO.amount)

      customSheet.addControl(amountBoxControl)
      val customSheetPaymentInfo = CustomSheetPaymentInfo.Builder()
        .setMerchantId("123456")
        .setMerchantName(samsungPayDTO.merchant.name)
        .setCardHolderNameEnabled(true)
        .setRecurringEnabled(false)
        .setOrderNumber(samsungPayDTO.orderNo)
        .setCustomSheet(customSheet)
        .build()

      val transactionListener = object : PaymentManager.CustomSheetTransactionInfoListener {
        // This callback is received when the user changes card on the custom payment sheet in Samsung Pay
        override fun onCardInfoUpdated(selectedCardInfo: CardInfo, customSheet: CustomSheet) {
          /*
           * Called when the user changes card in Samsung Pay.
           * Newly selected cardInfo is passed so merchant app can update transaction amount
           * based on different card (if needed),
           */
          val amountBoxControl = makeAmountControl(samsungPayDTO.amount)
          customSheet.updateControl(amountBoxControl)
          // Call updateSheet() with AmountBoxControl; mandatory.
          try {
            paymentManager.updateSheet(customSheet)
          } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
          } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
          }
        }

        /*
         * This callback is received when the payment is approved by the user and the transaction payload
         * is generated. Payload can be an encrypted cryptogram (network token mode) or the PG's token
         * reference ID (gateway token mode).
         */
        override fun onSuccess(
          response: CustomSheetPaymentInfo,
          paymentCredential: String,
          extraPaymentData: Bundle
        ) {
          /*
           * Called when Samsung Pay creates the transaction cryptogram, which merchant app then sends
           * to merchant server or PG to complete in-app payment.
           */try {
//            Log.i("SPAY_CREDENTIALS", paymentCredential)
//            Log.i("SPAY_CREDENTIALS", response.toString())
            map.putString("status", "success")
            map.putString("message", paymentCredential)
            callback.invoke(map)
          } catch (e: java.lang.NullPointerException) {

            map.putString("status", "failure")
            map.putString("response", "Samsung Pay transaction failure")
            callback.invoke(map)
            e.printStackTrace()
          }
        }

        override fun onFailure(errorCode: Int, errorData: Bundle) {
          // Called when an error occurs during cryptogram generation
//          Log.i("SPAY_MERA", errorCode.toString())
//          Log.i("SPAY_MERA", errorData.toString())
          map.putString("status", "failure")
          map.putString("response", "Samsung Pay transaction failure")
          callback.invoke(map)
        }
      }


      try {
        paymentManager.startInAppPayWithCustomSheet(customSheetPaymentInfo, transactionListener)
      } catch (err: Exception) {
        map.putString("status", "failure")
        map.putString("message", err.message.toString())
        callback.invoke(map)
      }
    }


  }
}
