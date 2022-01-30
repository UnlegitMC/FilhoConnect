package today.getfdp.connect.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
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
        SERVER_HOST("proxy.host", "0.0.0.0"),
        SERVER_PORT("proxy.port", 25565),
        TARGET_HOST("proxy.target_host", "127.0.0.1"),
        TARGET_PORT("proxy.target_port", 19132),
        ONLINE_MODE("play.online", false),
        BEDROCK_CODEC("play.bedrock_codec", "v475")
    }
}