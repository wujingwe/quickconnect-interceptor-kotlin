package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class PingPongJson internal constructor(
  @SerializedName("boot_done") val bootDone: Boolean,
  @SerializedName("disk_hibernation") val diskHibernation: Boolean,
  val ezid: String,
  val success: Boolean)

class PingPongBuilder {
  var bootDone: Boolean = false
  var diskHibernation: Boolean = false
  var ezid: String = ""
  var success: Boolean = false

  internal fun build() = PingPongJson(bootDone, diskHibernation, ezid, success)
}

fun pingPong(init: PingPongBuilder.() -> Unit): PingPongJson {
  return PingPongBuilder().apply { init() }.build()
}
