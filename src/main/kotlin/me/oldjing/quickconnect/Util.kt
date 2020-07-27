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
    return when (obj) {
      is String -> {
        obj.length == 0
      }
      is Collection<*> -> {
        obj.isEmpty()
      }
      is Array<*> -> {
        obj.isEmpty()
      }
      else -> false
    }
  }
}
