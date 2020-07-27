package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class PingPongJson(
  @SerializedName("boot_done") val bootDone: Boolean,
  @SerializedName("disk_hibernation") val diskHibernation: Boolean,
  val ezid: String,
  val success: Boolean)
