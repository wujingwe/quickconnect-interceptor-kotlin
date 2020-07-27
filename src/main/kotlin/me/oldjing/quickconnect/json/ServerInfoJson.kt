package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class ServerInfoJson(
  val sites: List<String>?,
  val server: ServerJson?,
  val env: EnvJson?,
  val service: ServiceJson?)
