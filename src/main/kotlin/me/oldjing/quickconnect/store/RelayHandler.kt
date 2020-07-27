package me.oldjing.quickconnect.store

abstract class RelayHandler {

  companion object {
    private var instance: RelayHandler? = null

    fun getDefault(): RelayHandler? {
      return instance
    }

    fun setDefault(handler: RelayHandler) {
      instance = handler
    }
  }

  abstract fun get(serverID: String): RelayCookie?

  abstract fun put(serverID: String, cookie: RelayCookie)

  abstract fun remove(serverID: String)

  abstract fun removeAll()
}