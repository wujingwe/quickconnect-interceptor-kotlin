package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class ServerJson(
  val serverID: String,
  val ddns: String,
  val fqdn: String,
  val gateway: String,
  @SerializedName("interface") val interface_: List<InterfaceJson>?,
  val external: ExternalJson?)
