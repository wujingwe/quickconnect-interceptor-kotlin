package me.oldjing.quickconnect.store

class InMemoryRelayStore : RelayStore {

	// In memory
	private val allCookies= mutableMapOf<String, RelayCookie>()

	override fun add(serverID: String, relayCookie: RelayCookie) {
		val targetCookie = allCookies[serverID]
		if (targetCookie != null) {
			allCookies.remove(serverID)
		}
		allCookies[serverID] = relayCookie;
	}

	override fun get(serverID: String): RelayCookie {
		return allCookies[serverID] ?: RelayCookie(serverID)
	}

	override fun remove(serverID: String) {
		allCookies.remove(serverID)
	}

	override fun removeAll() {
		allCookies.clear()
	}
}
