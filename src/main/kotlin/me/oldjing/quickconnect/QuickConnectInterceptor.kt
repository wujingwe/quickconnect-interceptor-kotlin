package me.oldjing.quickconnect

import me.oldjing.quickconnect.store.RelayCookie
import me.oldjing.quickconnect.store.RelayHandler
import me.oldjing.quickconnect.store.RelayManager
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class QuickConnectInterceptor : Interceptor {

	val ID_DSM_PORTAL = "dsm_portal";
	val ID_DSM_PORTAL_HTTPS = "dsm_portal_https"

	private val relayManager: RelayManager = RelayManager()

	init {
		RelayHandler.setDefault(relayManager)
	}

	override fun intercept(chain: Chain): Response? {
		var request: Request = chain.request()
		val requestUrl: HttpUrl = request.url()
		val isHttps: Boolean = requestUrl.isHttps
		var host: String = requestUrl.host()

		if (Util.isQuickConnectId(host)) {
			val serverID = host
			val id = if (isHttps) ID_DSM_PORTAL_HTTPS else ID_DSM_PORTAL;

			var cookie = relayManager.get(serverID)
			if (cookie == null) {
				// no quick connect information yet!
				cookie = RelayCookie(serverID, id)
				relayManager.put(serverID, cookie)
			}
			if (cookie.resolvedUrl == null) {
				// no resolved yet!
				val resolver: QuickConnectResolver = QuickConnectResolver(requestUrl)
				cookie = resolver.resolve(serverID, id)

				// update cache
				relayManager.put(serverID, cookie)
			}
			val resolvedUrl = cookie.resolvedUrl ?: throw IOException("resolvedUrl == null")
			host = resolvedUrl.host()
			if (host.indexOf(':') != -1) {
				host = "[$host]" // add brackets for IPv6
			}
			val url = requestUrl.newBuilder()
					.host(host)
					.port(resolvedUrl.port())
					.build()
			request = request.newBuilder()
					.url(url)
					.build();
			println("Resolved url: $url")
		}
		return chain.proceed(request)
	}
}