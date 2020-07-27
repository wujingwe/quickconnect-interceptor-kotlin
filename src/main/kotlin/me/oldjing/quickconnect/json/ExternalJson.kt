package me.oldjing.quickconnect.json

data class ExternalJson internal constructor(val ip: String, val ipv6: String)

class ExternalBuilder {
  var ip: String = ""
  var ipv6: String = ""

  fun build() = ExternalJson(ip, ipv6)
}
