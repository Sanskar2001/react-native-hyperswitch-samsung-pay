package com.hyperswitchsamsungpay

import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants

class SPaySheetControlUtils {

  companion object{
    @JvmStatic
    fun makeAmountControl(amount: Amount): AmountBoxControl {
      val amountBoxControl = AmountBoxControl(amount.option, amount.currencyCode)
      amountBoxControl.setAmountTotal(amount.total.toDouble(), AmountConstants.FORMAT_TOTAL_PRICE_ONLY)
      return amountBoxControl
    }
  }
}
