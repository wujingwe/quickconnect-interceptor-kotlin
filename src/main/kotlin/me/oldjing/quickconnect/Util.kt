package me.oldjing.quickconnect

fun String?.isQuickConnectId(): Boolean {
  return this != null
      && indexOf('.') < 0
      && indexOf(':') < 0
}

fun String?.isEmptyOrEscaped(escaped: String): Boolean {
  return isNullOrEmpty() || this == escaped
}
