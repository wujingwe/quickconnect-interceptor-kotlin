package me.oldjing.quickconnect.json

data class InterfaceJson(
  val ip: String,
  val ipv6: List<Ipv6Json>?,
  val mask: String,
  val name: String)
