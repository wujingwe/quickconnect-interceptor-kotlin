package me.oldjing.quickconnect.json

import com.google.gson.annotations.SerializedName

data class ServerInfoJson(val sites: List<String>?,
                          val server: ServerJson?,
                          val env: EnvJson?,
                          val service: ServiceJson?) {

	data class ServerJson(val serverID: String,
	                      val ddns: String,
	                      val fqdn: String,
	                      val gateway: String,
	                      @SerializedName("interface") val interface_: List<InterfaceJson>?,
	                      val external: ExternalJson?) {

		data class InterfaceJson(val ip: String,
		                         val ipv6: List<Ipv6Json>?,
		                         val mask: String,
		                         val name: String) {

			data class Ipv6Json(val addr_type: Int,
			                    val address: String,
			                    val prefix_length: Int,
			                    val scope: String)
		}

		data class ExternalJson(val ip: String,
		                        val ipv6: String)
	}

	data class EnvJson(val relay_region: String,
	                   val control_host: String)

	data class ServiceJson(val port: Int,
	                       val ext_port: Int,
	                       val relay_ip: String?,
	                       val relay_ipv6: String?,
	                       val relay_port: Int?)
}
