package me.oldjing.quickconnect.json

data class Ipv6Json(
  val addr_type: Int,
  val address: String,
  val prefix_length: Int,
  val scope: String)
