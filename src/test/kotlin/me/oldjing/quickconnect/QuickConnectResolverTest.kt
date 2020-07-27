package me.oldjing.quickconnect

import com.google.gson.Gson
import me.oldjing.quickconnect.json.pingPong
import me.oldjing.quickconnect.json.serverInfo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class QuickConnectResolverTest {

	@get:Rule
	var mockWebServer = MockWebServer()

	private val gson = Gson()

	@Test
	fun getServerInfo() {
		val serverInfoJson =
				serverInfo {
					server = server {
						serverID = "dsm"
						ddns = "ddns"
						fqdn = "fqdn"
						gateway = "gateway"
						ifaces = listOf(
								iface {
									ip = "localhost"
									ipv6 = null
									mask = "mask"
									name = "eth0"
								}
						)
						external = external {
							ip = "external"
							ipv6 = "external_ipv6"
						}
					}
					env = env {
						relayRegion = "tw"
						controlHost = "twc.quickconnect.to"
					}
					service = service {
						port = 5000
						extPort = 0
						relayIp = null
						relayIpv6 = null
						relayPort = null
					}
				}

		mockWebServer.enqueue(MockResponse().setBody(gson.toJson(serverInfoJson)))

		val resolver = QuickConnectResolver(mockWebServer.url("/"))
		val result = resolver.getServerInfo(mockWebServer.url("/"), "demo", "dsm_portal")

		assertNotNull(result)
		assertEquals(result, serverInfoJson)
	}

	@Test
	fun pingDSM() {
		val pingPongJson =
				pingPong {
					bootDone = true
					diskHibernation = false
					ezid = "ezid"
					success = true
				}
		mockWebServer.enqueue(MockResponse().setBody(gson.toJson(pingPongJson)))

		val serverInfoJson =
				serverInfo {
					sites = null
					server = server {
						serverID = "dsm"
						ddns = "NULL"
						fqdn = "NULL"
						gateway = "gateway"
						ifaces = listOf(
								iface {
									ip = "localhost"
									ipv6 = null
									mask = "mask"
									name = "eth0"
								}
						)
						external = null
					}
					env = env {
						relayRegion = "tw"
						controlHost = "twc.quickconnect.to"
					}
					service = service {
						port = mockWebServer.port
						extPort = 0
						relayIp = null
						relayIpv6 = null
						relayPort = null
					}
				}
		val resolver = QuickConnectResolver(mockWebServer.url("/"))
		val url = resolver.pingDSM(serverInfoJson)

		assertNotNull(url)
		assertEquals(url, mockWebServer.url("/"))
	}

	@Test
	fun requestTunnel() {
		val serverInfoJson =
				serverInfo {
					server = server {
						serverID = "dsm"
						ddns = "ddns"
						fqdn = "fqdn"
						gateway = "gateway"
						ifaces = listOf(
								iface {
									ip = "localhost"
									ipv6 = null
									mask = "mask"
									name = "eth0"
								}
						)
						external = external {
							ip = "external"
							ipv6 = "external_ipv6"
						}
					}
					env = env {
						relayRegion = "tw"
						controlHost = mockWebServer.url("/").host() + ":" + mockWebServer.port
					}
					service = service {
						port = 5000
						extPort = 0
						relayIpv6 = null
						relayPort = null
					}
				}
		mockWebServer.enqueue(MockResponse().setBody(gson.toJson(serverInfoJson)))

		val resolver = QuickConnectResolver(mockWebServer.url("/"))
		val result = resolver.requestTunnel(serverInfoJson, "demo", "dsm_portal")

		assertNotNull(result)
		assertEquals(result, serverInfoJson)
  }
}
