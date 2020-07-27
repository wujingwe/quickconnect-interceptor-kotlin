package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class ServerJson internal constructor(
  val serverID: String,
  val ddns: String,
  val fqdn: String,
  val gateway: String,
  @SerializedName("interface") val interface_: List<InterfaceJson>?,
  val external: ExternalJson?)

class ServerBuilder {
  var serverID: String = ""
  var ddns: String = ""
  var fqdn: String = ""
  var gateway: String = ""
  @SerializedName("interface") var ifaces: List<InterfaceJson>? = null
  var external: ExternalJson? = null

  fun iface(init: IfaceBuilder.() -> Unit): InterfaceJson {
    return IfaceBuilder().apply(init).build()
  }

  fun external(init: ExternalBuilder.() -> Unit): ExternalJson {
    return ExternalBuilder().apply(init).build()
  }

  internal fun build(): ServerJson {
    return ServerJson(serverID, ddns, fqdn, gateway, ifaces, external)
  }
}
