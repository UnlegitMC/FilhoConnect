package today.getfdp.connect.resources

import com.google.gson.JsonParser
import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.network.JWTUtils
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logError
import java.io.File

object BlockMappingHolder : ResourceHolder() {
    override val resourceName: String
        get() = "BlockMapping"
    override val resourcePath: String
        get() = Configuration[Configuration.Key.BLOCK_MAPPING]

    val bedrockToJava = mutableMapOf<String, String>()
    val javaBlockToState = mutableMapOf<String, Int>()
    val javaStateToBlock = mutableMapOf<Int, String>()

    private val loggedUnknownBlocks = mutableSetOf<String>()

    override fun init(file: File) {
        val json = JsonParser().parse(file.reader(Charsets.UTF_8)).asJsonObject
        var index = 0
        json.entrySet().forEach { (key, value) ->
            val obj = value.asJsonObject
            val sortedMap = sortedMapOf<String, String>()

            obj.getAsJsonObject("bedrock_states")?.entrySet()?.forEach { (key, value) ->
                val jsonPrimitive = value.asJsonPrimitive
                sortedMap[key] = if(jsonPrimitive.isBoolean) {
                    if(jsonPrimitive.asBoolean) "1" else "0" // bedrock block palette dont have a boolean value
                } else {
                    JWTUtils.getValuePrimitive(jsonPrimitive).toString()
                }
            }

            // generate bedrock block name
            val sb = StringBuilder()
            sb.append(obj.get("bedrock_identifier").asString)
            if(sortedMap.isNotEmpty()) {
                sb.append("[")
                sortedMap.forEach { (key, value) ->
                    sb.append(key)
                    sb.append("=")
                    sb.append(value)
                    sb.append(",")
                }
                sb.delete(sb.length - 1, sb.length)
                sb.append("]")
            }
            val name = sb.toString()

            if(!key.contains("waterlogged=true")) { // waterlogged blocks in bedrock is not in this format
                bedrockToJava[name] = key
            }
            javaBlockToState[key] = index
            javaStateToBlock[index] = key

            index++
        }

        addPatches()
    }

    /**
     * add patches not in geyser block mapping
     */
    private fun addPatches() {
        val json = FConnect.parser.parse(this.javaClass.getResourceAsStream("/fc-data/addition_blocks.json")) as com.beust.klaxon.JsonObject
        json.forEach { (key, value) ->
            bedrockToJava[key] = value.toString()
        }
    }

    fun logUnknownBlock(name: String) {
        if(!loggedUnknownBlocks.contains(name)) {
            logError("Unable to find mapping for block $name")
            loggedUnknownBlocks.add(name)
        }
    }
}