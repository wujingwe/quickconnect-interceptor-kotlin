package me.oldjing.quickconnect.store

class RelayManager(store: RelayStore? = null) : RelayHandler() {

	private var relayJar = store ?: InMemoryRelayStore()

	override fun get(serverID: String): RelayCookie? {
		return relayJar.get(serverID)
	}

	override fun put(serverID: String, cookie: RelayCookie) {
		relayJar.add(serverID, cookie)
	}

	override fun remove(serverID: String) {
		relayJar.remove(serverID)
	}

	override fun removeAll() {
		relayJar.removeAll()
	}
}
