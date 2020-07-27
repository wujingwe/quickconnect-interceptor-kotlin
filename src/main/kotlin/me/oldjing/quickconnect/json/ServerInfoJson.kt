package me.oldjing.quickconnect.json

data class ServerInfoJson internal constructor(
  val sites: List<String>?,
  val server: ServerJson?,
  val env: EnvJson?,
  val service: ServiceJson?)

class ServerInfoBuilder {
  var sites: MutableList<String>? = null
  var server: ServerJson? = null
  var env: EnvJson? = null
  var service: ServiceJson? = null

  fun server(init: ServerBuilder.() -> Unit): ServerJson {
    return ServerBuilder().apply(init).build()
  }

  fun env(init: EnvBuilder.() -> Unit): EnvJson {
    return EnvBuilder().apply(init).build()
  }

  fun service(init: ServiceBuilder.() -> Unit): ServiceJson {
    return ServiceBuilder().apply(init).build()
  }

  internal fun build() = ServerInfoJson(sites, server, env, service)
}

fun serverInfo(init: ServerInfoBuilder.() -> Unit): ServerInfoJson {
  return ServerInfoBuilder().apply(init).build()
}
