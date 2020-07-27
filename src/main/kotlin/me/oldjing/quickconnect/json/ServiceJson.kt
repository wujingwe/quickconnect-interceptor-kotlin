package me.oldjing.quickconnect.json

data class ServiceJson internal constructor(
  val port: Int,
  val ext_port: Int,
  val relay_ip: String?,
  val relay_ipv6: String?,
  val relay_port: Int?)

class ServiceBuilder {
  var port: Int = 0
  var extPort: Int = 0
  var relayIp: String? = null
  var relayIpv6: String? = null
  var relayPort: Int? = null

  internal fun build() = ServiceJson(port, extPort, relayIp, relayIpv6, relayPort)
}
