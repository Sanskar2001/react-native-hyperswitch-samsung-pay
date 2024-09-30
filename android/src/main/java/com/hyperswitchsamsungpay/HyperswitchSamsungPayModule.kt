package com.hyperswitchsamsungpay

import android.app.Activity
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.samsung.android.sdk.samsungpay.v2.SamsungPay


class HyperswitchSamsungPayModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return "HyperswitchSamsungPay"
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android


//  @ReactMethod
//  fun samsungPayInit(serviceId: String,requestObj:String, callback: Callback) {
//    SamsungPayValidator.setSamsungPayContext(reactApplicationContext.currentActivity!! as Activity)
//    // SamsungPayValidator.samsungPayInit(serviceId,requestObj, callback)
//  }


  @ReactMethod
  fun checkSamsungPayValidity(requestObj: String, callback: Callback) {
    try {
      SamsungPayValidator.parseSamsungPayInfo(requestObj, callback)
      SamsungPayValidator.setSamsungPayContext(reactApplicationContext.currentActivity!! as Activity)
      SamsungPayValidator.checkSamsungPayStatus(callback)
    } catch (err: Exception) {
      Log.i("SPAYValidityCheckFail", err.message.toString())
    }
  }


  @ReactMethod
  fun activateSamsungPay(callback: Callback) {
    SamsungPayValidator.activateSamsungPay(callback)
  }

  @ReactMethod
  fun requestCardInfo(callback: Callback) {
    SamsungPayValidator.requestCardInfo(callback)
  }

  @ReactMethod
  fun presentSamsungPayPaymentSheet(callback: Callback) {
    SamsungPayValidator.presentSamsungPayPaymentSheet(callback)
  }


}
