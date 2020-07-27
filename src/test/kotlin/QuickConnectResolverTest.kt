import com.google.gson.Gson
import me.oldjing.quickconnect.QuickConnectResolver
import me.oldjing.quickconnect.json.PingPongJson
import me.oldjing.quickconnect.json.ServerInfoJson
import me.oldjing.quickconnect.json.ServerInfoJson.EnvJson
import me.oldjing.quickconnect.json.ServerInfoJson.ServerJson
import me.oldjing.quickconnect.json.ServerInfoJson.ServiceJson
import me.oldjing.quickconnect.json.ServerInfoJson.ServerJson.ExternalJson
import me.oldjing.quickconnect.json.ServerInfoJson.ServerJson.InterfaceJson
import me.oldjing.quickconnect.store.RelayHandler
import me.oldjing.quickconnect.store.RelayManager
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class QuickConnectResolverTest {

	@get:Rule
	var server = MockWebServer()

	private val gson = Gson()

	@Test
	fun serverInfo() {
		val interfaceJson = InterfaceJson("localhost", null, "mask", "eth0")
		val externalJson = ExternalJson("external", "external_ipv6")
		val serverJson =
			ServerJson("dsm", "ddns", "fqdn", "gateway", listOf(interfaceJson), externalJson)
		val envJson = EnvJson("tw", "twc.quickconnect.to")
		val serviceJson = ServiceJson(5000, 0, null, null, null)
		val serverInfoJson = ServerInfoJson(null, serverJson, envJson, serviceJson)

		server.enqueue(MockResponse().setBody(gson.toJson(serverInfoJson)))

		val resolver = QuickConnectResolver(server.url("/"))
		val result = resolver.getServerInfo(server.url("/"), "demo", "dsm_portal")

		assertNotNull(result)
		assertEquals(result, serverInfoJson)
	}

	@Test
	fun pingDSM() {
		val pingPongJson = PingPongJson(true, false, "ezid", true)
		server.enqueue(MockResponse().setBody(gson.toJson(pingPongJson)))

		val interfaceJson = InterfaceJson("localhost", null, "mask", "eth0")
		val serverJson =
			ServerJson("dsm", "NULL", "NULL", "gateway", listOf(interfaceJson), null)
		val envJson = EnvJson("tw", "twc.quickconnect.to")
		val serviceJson = ServiceJson(server.port, 0, null, null, null)
		val serverInfoJson = ServerInfoJson(null, serverJson, envJson, serviceJson)
		val resolver = QuickConnectResolver(server.url("/"))
		val url = resolver.pingDSM(serverInfoJson)

		assertNotNull(url)
		assertEquals(url, server.url("/"))
	}

	@Test
	fun requestTunnel() {
		val interfaceJson = InterfaceJson("localhost", null, "mask", "eth0")
		val externalJson = ExternalJson("external", "external_ipv6")
		val serverJson =
			ServerJson("dsm", "ddns", "fqdn", "gateway", listOf(interfaceJson), externalJson)
		val envJson = EnvJson("tw", server.url("/").host() + ":" + server.port)
		val serviceJson = ServiceJson(5000, 0, null, null, null)
		val serverInfoJson = ServerInfoJson(null, serverJson, envJson, serviceJson)

		server.enqueue(MockResponse().setBody(gson.toJson(serverInfoJson)))

		val resolver = QuickConnectResolver(server.url("/"))
		val result = resolver.requestTunnel(serverInfoJson, "demo", "dsm_portal")

		assertNotNull(result)
		assertEquals(result, serverInfoJson)
	}

	@Test
	fun addRelayCookie() {
		val relayManager = RelayManager()
		RelayHandler.setDefault(relayManager)
	}
}
