package com.hyperswitchsamsungpay

import org.json.JSONArray

data class Amount(
  val option: String,
  val currencyCode: String,
  val total: String
)

data class Merchant(
  val name: String,
  val url: String,
  val currencyCode: String
)

data class SamsungPayDTO(
  val serviceId: String,
  val orderNo: String,
  val merchant: Merchant,
  val amount: Amount,
  val protocol: String,
  val allowedCardBrands: JSONArray
)


