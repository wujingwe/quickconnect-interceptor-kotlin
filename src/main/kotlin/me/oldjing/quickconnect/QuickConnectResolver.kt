package me.oldjing.quickconnect

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.oldjing.quickconnect.store.RelayCookie
import me.oldjing.quickconnect.store.RelayHandler
import me.oldjing.quickconnect.store.RelayManager
import me.oldjing.quickconnect.json.PingPongJson
import me.oldjing.quickconnect.json.ServerInfoJson
import me.oldjing.quickconnect.json.ServerInfoJson.ServiceJson
import okhttp3.*
import okhttp3.OkHttpClient.Builder
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class QuickConnectResolver(private val requestUrl: HttpUrl) {
	private val builder: Builder = Builder()
	private val gson: Gson
	private val context = SSLContext.getInstance("TLS")

	init {

		try {
			val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
				@Throws(CertificateException::class)
				override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
				}

				@Throws(CertificateException::class)
				override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
				}

				override fun getAcceptedIssuers(): Array<X509Certificate> {
					return arrayOf()
				}
			})
			context.init(null, trustManagers, SecureRandom())
			builder.sslSocketFactory(context.socketFactory)
					.hostnameVerifier { _, _ ->
						// since most DSM doesn't have valid certificate, ignore verifying hostname
						true
					}
		} catch (ignored: NoSuchAlgorithmException) {
		} catch (ignored: KeyManagementException) {
		}
		gson = Gson()
	}

	@Throws(IOException::class)
	fun resolve(serverID: String, id: String): RelayCookie {
		if (!Util.isQuickConnectId(serverID)) {
			throw IllegalArgumentException("serverID isn't a Quick Connect ID")
		}

		val relayManager = RelayHandler.getDefault() as RelayManager
		var cookie = relayManager.get(serverID)
		if (cookie == null) {
			cookie = RelayCookie(serverID, id)
			relayManager.put(serverID, cookie)
		}

		val serverUrl = HttpUrl.parse("http://global.quickconnect.to/Serv.php")
		var infoJson: ServerInfoJson? = getServerInfo(serverUrl, serverID, id)

		// ping DSM directly
		var resolvedUrl = pingDSM(infoJson)
		if (resolvedUrl != null) {
			cookie.resolvedUrl = resolvedUrl
			return cookie
		}

		// ping DSM through tunnel
		val serviceJson = infoJson?.service
		if (serviceJson != null) {
			resolvedUrl = pingTunnel(serviceJson)
			if (resolvedUrl != null) {
				cookie.resolvedUrl = resolvedUrl
				return cookie
			}

			// request tunnel                         f
			infoJson = requestTunnel(infoJson, serverID, id)
			if (infoJson != null && serviceJson.relay_ip != null && serviceJson.relay_port != null) {
				resolvedUrl = requestUrl.newBuilder().host(serviceJson.relay_ip).port(serviceJson.relay_port).build()
				cookie.resolvedUrl = resolvedUrl
				return cookie
			}
		}

		throw IOException("No valid url resolved")
	}

	@Throws(IOException::class)
	fun getServerInfo(serverUrl: HttpUrl, serverID: String, id: String): ServerInfoJson {
		// set timeout to 30 seconds
		val client = builder.connectTimeout(30, SECONDS)
				.readTimeout(30, SECONDS)
				.build()

		val jObject = JsonObject()
		jObject.addProperty("version", 1)
		jObject.addProperty("command", "get_server_info")
		jObject.addProperty("stop_when_error", "false")
		jObject.addProperty("stop_when_success", "false")
		jObject.addProperty("id", id)
		jObject.addProperty("serverID", serverID)
		val requestBody = RequestBody.create(MediaType.parse("text/plain"), gson.toJson(jObject))
		val request = Request.Builder()
				.url(serverUrl)
				.post(requestBody)
				.build()
		val response = client.newCall(request).execute()
		val reader = response.body().charStream()
		reader.use {
			val serverInfoJson = gson.fromJson<ServerInfoJson>(it, ServerInfoJson::class.java)
			if (serverInfoJson != null) {
				val server = serverInfoJson.server
				if (server != null) {
					return serverInfoJson
				}
				val sites = serverInfoJson.sites
				if (sites != null && sites.isNotEmpty()) {
					val site = sites[0]
					val siteUrl = HttpUrl.Builder().scheme("http").host(site).addPathSegment("Serv.php").build()
					return getServerInfo(siteUrl, serverID, id)
				}
			}
		}

		throw IOException("No server info found!")
	}

	fun pingDSM(infoJson: ServerInfoJson?): HttpUrl? {
		// set timeout to 5 seconds
		val client = builder.connectTimeout(5, SECONDS)
				.readTimeout(5, SECONDS)
				.build()

		val serverJson = infoJson?.server ?: throw IllegalArgumentException("serverJson == null")
		val serviceJson = infoJson.service ?: throw IllegalArgumentException("serviceJson == null")
		val port = serviceJson.port
		val externalPort = serviceJson.ext_port

		// internal address(192.168.x.x/10.x.x.x)
		val executor = Executors.newFixedThreadPool(10)
		val internalService = ExecutorCompletionService<String>(executor)
		val ifaces = serverJson.interface_
		val internalCount = AtomicInteger(0)
		if (ifaces != null) {
			for (iface in ifaces) {
				internalService.submit(newPingCallable(client, iface.ip, port))
				internalCount.incrementAndGet()

				if (iface.ipv6 != null) {
					for (ipv6 in iface.ipv6) {
						val ipv6Address = "[${ipv6.address}]"
						internalService.submit(newPingCallable(client, ipv6Address, port))
						internalCount.incrementAndGet()
					}
				}
			}
		}

		// host address(ddns/fqdn)
		val hostService = ExecutorCompletionService<String>(executor)
		val hostCount = AtomicInteger(0)
		val ddns = serverJson.ddns
		if (!Util.isEmpty(ddns) && ddns != "NULL") {
			hostService.submit(newPingCallable(client, ddns, port))
			hostCount.incrementAndGet()
		}
		val fqdn = serverJson.fqdn
		if (!Util.isEmpty(fqdn) && fqdn != "NULL") {
			hostService.submit(newPingCallable(client, fqdn, port))
			hostCount.incrementAndGet()
		}

		// external address(public ip address)
		val externalService = ExecutorCompletionService<String>(executor)
		val externalCount = AtomicInteger(0)
		if (serverJson.external != null) {
			val ip = serverJson.external.ip
			if (!Util.isEmpty(ip)) {
				externalService.submit(newPingCallable(client, ip, if (externalPort != 0) externalPort else port))
				externalCount.incrementAndGet()
			}
			val ipv6 = serverJson.external.ipv6
			if (!Util.isEmpty(ipv6) && ipv6 != "::") {
				externalService.submit(newPingCallable(client, "[$ipv6]", if (externalPort != 0) externalPort else port))
				externalCount.incrementAndGet()
			}
		}

		while (internalCount.andDecrement > 0) {
			try {
				val future = internalService.take()
				if (future != null) {
					val host = future.get()
					if (!Util.isEmpty(host)) {
						return requestUrl.newBuilder().host(host).port(port).build()
					}
				}
			} catch (ignored: InterruptedException) {
			} catch (ignored: ExecutionException) {
			}
		}

		while (hostCount.andDecrement > 0) {
			try {
				val future = hostService.take()
				if (future != null) {
					val host = future.get()
					if (!Util.isEmpty(host)) {
						return requestUrl.newBuilder().host(host).port(port).build()
					}
				}
			} catch (ignored: InterruptedException) {
			} catch (ignored: ExecutionException) {
			}
		}

		while (externalCount.andDecrement > 0) {
			try {
				val future = externalService.take()
				if (future != null) {
					val host = future.get()
					if (!Util.isEmpty(host)) {
						return requestUrl.newBuilder().host(host).port(port).build()
					}
				}
			} catch (ignored: InterruptedException) {
			} catch (ignored: ExecutionException) {
			}
		}

		// shutdown executors
		executor.shutdownNow()

		return null
	}

	private fun pingTunnel(serviceJson: ServiceJson): HttpUrl? {
		if (Util.isEmpty(serviceJson.relay_ip) || serviceJson.relay_port == 0) {
			return null
		}

		// set timeout to 10 seconds
		val client = builder.connectTimeout(10, SECONDS)
				.readTimeout(10, SECONDS)
				.build()

		val relayIp = serviceJson.relay_ip
		val relayPort = serviceJson.relay_port

		// tunnel address
		val executor = Executors.newFixedThreadPool(10)
		val service = ExecutorCompletionService<String>(executor)
		service.submit(newPingCallable(client, relayIp, relayPort))

		try {
			val future = service.take()
			if (future != null) {
				val host = future.get()
				if (!Util.isEmpty(host) && relayPort != null) {
					return requestUrl.newBuilder().host(host).port(relayPort).build()
				}
			}
		} catch (ignored: InterruptedException) {
		} catch (ignored: ExecutionException) {
		}

		// shutdown executors
		executor.shutdownNow()

		return null
	}

	private fun newPingCallable(client: OkHttpClient, host: String?, port: Int?): Callable<String> {
		if (host == null) {
			throw IllegalArgumentException("host == null")
		}
		if (port == null) {
			throw IllegalArgumentException("port == null")
		}

		val pingPongUrl = HttpUrl.Builder()
				.scheme(requestUrl.scheme())
				.host(host)
				.port(port)
				.addPathSegment("webman")
				.addPathSegment("pingpong.cgi")
				.build()

		return Callable<String> {
			val request = Request.Builder()
					.url(pingPongUrl)
					.build()
			val response = client.newCall(request).execute()
			response.body().charStream().use {
				val pingPongJson = gson.fromJson<PingPongJson>(it, PingPongJson::class.java)
				if (pingPongJson != null && pingPongJson.success) {
					return@Callable host
				}
			}
			null
		}
	}

	fun requestTunnel(infoJson: ServerInfoJson?, serverID: String, id: String): ServerInfoJson? {
		if (infoJson?.env == null || Util.isEmpty(infoJson.env.control_host)) {
			return null
		}

		val client = builder.connectTimeout(30, SECONDS)
				.readTimeout(30, SECONDS)
				.build()

		val server = infoJson.env.control_host
		val jObject = JsonObject()
		jObject.addProperty("command", "request_tunnel")
		jObject.addProperty("version", 1)
		jObject.addProperty("serverID", serverID)
		jObject.addProperty("id", id)

		val requestBody = RequestBody.create(MediaType.parse("text/plain"), gson.toJson(jObject))
		val request = Request.Builder()
				.url(HttpUrl.parse("http://$server/Serv.php"))
				.post(requestBody)
				.build()
		val response = client.newCall(request).execute()
		response.body().charStream().use {
			return gson.fromJson<ServerInfoJson>(it, ServerInfoJson::class.java)
		}
	}
}
