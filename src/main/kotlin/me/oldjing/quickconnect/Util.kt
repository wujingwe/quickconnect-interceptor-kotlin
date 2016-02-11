package me.oldjing.quickconnect

object Util {
	fun isQuickConnectId(host: String?): Boolean {
		return host != null
		       && host.indexOf('.') < 0
		       && host.indexOf(':') < 0
	}

	fun isEmpty(obj: Any?): Boolean {
		if (obj == null) {
			return true
		}
		if (obj is String) {
			return obj.length == 0
		} else if (obj is Collection<*>) {
			return obj.isEmpty()
		} else if (obj is Array<*>) {
			return obj.isEmpty()
		}
		return false
	}
}