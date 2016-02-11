package me.oldjing.quickconnect.store

import okhttp3.HttpUrl

data class RelayCookie(val serverID: String,
                       val id: String = "",
                       var resolvedUrl: HttpUrl? = null) {
}
