package me.oldjing.quickconnect.store

import me.oldjing.quickconnect.store.InMemoryRelayStore

class RelayManager(val store: RelayStore? = null) : RelayHandler() {

	var relayJar: RelayStore? = null

	init {
		if (store == null) {
			relayJar = InMemoryRelayStore()
		} else {
			relayJar = store
		}
	}

	override fun get(serverID: String): RelayCookie? {
		// if there's no default ApiStore, no way for us to get any API
		return relayJar?.get(serverID)
	}

	override fun put(serverID: String, cookie: RelayCookie) {
		// if there's no default ApiStore, no need to remember any API
		if (relayJar == null) {
			return
		}
		(relayJar as RelayStore).add(serverID, cookie)
	}

	override fun remove(serverID: String) {
		(relayJar as RelayStore).remove(serverID)
	}

	override fun removeAll() {
		(relayJar as RelayStore).removeAll()
	}
}
