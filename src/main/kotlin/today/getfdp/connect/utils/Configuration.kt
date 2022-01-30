package today.getfdp.connect.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.File

object Configuration {

    private val file = File("config.json")
    private var configJson = JsonObject()

    fun loadJson() {
        configJson = if(file.exists()) {
            Parser.default().parse(StringBuilder(file.readText(Charsets.UTF_8))) as JsonObject
        } else {
            JsonObject()
        }
    }

    fun saveJson() {
        file.writeText(configJson.toJsonString(true))
    }

    fun syncToJson() {
        configJson.clear()
        Key.values().forEach { key ->
            val parent = parentJson(key.path)
            val jkey = key.path.substring(key.path.lastIndexOf(".") + 1)
            parent[jkey] = key.value
        }
        saveJson()
    }

    fun load() {
        loadJson()
        Key.values().forEach { key ->
            val parent = parentJson(key.path)
            val jkey = key.path.substring(key.path.lastIndexOf(".") + 1)
            if(parent.containsKey(jkey)) {
                key.value = parent[jkey]!!
            } else {
                parent[jkey] = key.value
            }
        }
        saveJson()
    }

    fun save() {
        syncToJson()
        saveJson()
    }

    private fun parentJson(path: String, lastJson: JsonObject = configJson): JsonObject {
        return if(path.contains(".")) {
            val index = path.indexOf(".")
            val parentPath = path.substring(0, index)
            parentJson(path.substring(index + 1), (lastJson.obj(parentPath) ?: JsonObject().also { lastJson[parentPath] = it }))
        } else {
            lastJson
        }
    }

    operator fun <T> get(key: Key): T {
        return key.value as T
    }

    enum class Key(val path: String, var value: Any) {
        SERVER_HOST("proxy.host", "0.0.0.0"), // proxy server host (Java Edition)
        SERVER_PORT("proxy.port", 25565), // proxy server port
        TARGET_HOST("proxy.target_host", "127.0.0.1"), // target server host (Bedrock Edition)
        TARGET_PORT("proxy.target_port", 19132), // target server port
        ONLINE_MODE("play.online", false), // use xbox live login
        XBOX_AUTOLOGIN("play.xbox_auto_login", true), // this stores microsoft access token
        BEDROCK_CODEC("play.bedrock_codec", "v475"), // codec version that is used to encode and decode the bedrock packet
        BEDROCK_PROTOCOL("play.bedrock_protocol", 475), // protocol version that actually sends to the server
        DEVICE_OS("play.device_os", 7), // device os, 1 = Android, 7 = Windows 10
    }
}