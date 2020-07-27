package me.oldjing.quickconnect.json

data class InterfaceJson internal constructor(
  val ip: String,
  val ipv6: List<Ipv6Json>?,
  val mask: String,
  val name: String)

class IfaceBuilder {
  var ip: String = ""
  var ipv6: MutableList<Ipv6Json>? = null
  var mask: String = ""
  var name: String = ""

  internal fun build() = InterfaceJson(ip, ipv6, mask, name)
}
