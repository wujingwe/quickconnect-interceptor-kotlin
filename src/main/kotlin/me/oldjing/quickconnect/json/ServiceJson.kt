package me.oldjing.quickconnect.json

data class ServiceJson(
  val port: Int,
  val ext_port: Int,
  val relay_ip: String?,
  val relay_ipv6: String?,
  val relay_port: Int?)
