package me.oldjing.quickconnect.store

import java.util.*

class InMemoryRelayStore : RelayStore {

	// In memory
	val allCookies: HashMap<String, RelayCookie>

	init {
		allCookies = HashMap<String, RelayCookie>()
	}

	override fun add(serverID: String, relayCookie: RelayCookie) {
		var targetCookie = allCookies[serverID]
		if (targetCookie != null) {
			allCookies.remove(serverID)
		}
		allCookies.put(serverID, relayCookie);
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