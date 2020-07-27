package me.oldjing.quickconnect.json

data class EnvJson internal constructor(val relay_region: String, val control_host: String)

class EnvBuilder {
  var relayRegion: String = ""
  var controlHost: String = ""

  internal fun build() = EnvJson(relayRegion, controlHost)
}
