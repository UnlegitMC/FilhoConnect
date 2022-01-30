package today.getfdp.connect.play

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.Configuration
import java.io.File

object AutoLoginManager {

    private val file = File("xbox_tokens.json")
    private val data = mutableMapOf<String, String>() // name, token

    fun load() {
        if(!file.exists()) {
            return
        }
        val json = FConnect.parser.parse(StringBuilder(file.readText(Charsets.UTF_8))) as JsonObject
        data.clear()
        json.obj("tokens")?.forEach {
            data[it.key] = it.value as String
        }
    }

    fun save() {
        val json = JsonObject()
        data.forEach {
            json[it.key] = it.value
        }
        file.writeText(JsonObject().apply { set("tokens", json) }.toJsonString())
    }

    operator fun get(name: String): String? {
        return data[name]
    }

    operator fun set(name: String, token: String?) {
        if(token == null) {
            if(data.containsKey(name)) {
                data.remove(name)
            }
        } else {
            data[name] = token
        }
        save()
    }
}