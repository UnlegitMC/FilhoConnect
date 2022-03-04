package today.getfdp.connect.utils.other

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.File
import java.util.*

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
        DO_ASYNC("proxy.do_async", true), // whether to do async
        ONLINE_MODE("play.online", "OFFLINE"), // ONLINE, CUSTOM, OFFLINE
        XBOX_AUTOLOGIN("play.xbox_auto_login", true), // this stores microsoft access token
        BEDROCK_CODEC("play.bedrock_codec", "v486"), // codec version that is used to encode and decode the bedrock packet
        BEDROCK_PROTOCOL("play.bedrock_protocol", 486), // protocol version that actually sends to the server
        LANGUAGE_CODE("play.language_code", Locale.getDefault().toString()), // language code that is used to send to the server, may affect the server's language
        DEVICE_OS("play.device_os", 7), // device os, 1 = Android, 7 = Windows 10
        SKIN_PATH("play.skin_path", "https://github.com/UnlegitMC/fc-data/raw/main/default_skin.png"), // custom skin path, but we only support default skin geometry
        AUTH_INPUT_MODE("play.auth_input_mode", "TOUCH"), // touch screen, disable hitbox check?
        SELF_SKIN("addition.self_skin", true), // send self skin to geyser bedrock skin utils mod, this have a bug to make first-person skin black
        BLOCK_PALETTE("mapping.block_palette", "https://github.com/CloudburstMC/Nukkit/raw/84be206437c40da83af4035e27b63cc99f375e9f/src/main/resources/runtime_block_states.dat"), // block palette url
        BIOME_MAPPING("mapping.biome_mapping", "https://github.com/GeyserMC/mappings/raw/64f338a2670bb8d300a66975389fc7887df4c4de/biomes.json"), // java <-> bedrock block name mapping
        BLOCK_MAPPING("mapping.block_mapping", "https://github.com/GeyserMC/mappings/raw/64f338a2670bb8d300a66975389fc7887df4c4de/blocks.json"), // java <-> bedrock block name mapping
    }
}