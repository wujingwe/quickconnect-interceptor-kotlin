package me.oldjing.quickconnect.store

interface RelayStore {
  fun add(serverID: String, relayCookie: RelayCookie)

  fun get(serverID: String): RelayCookie

  fun remove(serverID: String)

  fun removeAll()
}
