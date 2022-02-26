package today.getfdp.connect.resources

import com.nukkitx.nbt.NBTInputStream
import com.nukkitx.nbt.NbtList
import com.nukkitx.nbt.NbtMap
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logError
import java.io.DataInputStream
import java.io.File

object BedrockBlockPaletteHolder : ResourceHolder() {
    override val resourceName: String
        get() = "BlockPalette"
    override val resourcePath: String
        get() = Configuration[Configuration.Key.BLOCK_PALETTE]

    val runtimeToBlock = mutableMapOf<Int, String>()
    val blockToRuntime = mutableMapOf<String, Int>()
    val legacyBlocks = mutableMapOf<Int, String>()
    var airId = 0
        private set

    private val loggedUnknownBlocks = mutableSetOf<Int>()

    override fun init(file: File) {
        val tag = NBTInputStream(DataInputStream(file.inputStream())).readTag() as NbtList<*>
        tag.forEach { nbt ->
            if(nbt is NbtMap) {
                val name = getBlockNameFromNbtMap(nbt)
                val runtimeId = nbt.getInt("runtimeId")

                runtimeToBlock[runtimeId] = name
                blockToRuntime[name] = runtimeId

                if(nbt.containsKey("id") && nbt.containsKey("data")) {
                    legacyBlocks[nbt.getInt("id") shl 6 or nbt.getShort("data").toInt()] = name
                }

                if(name == "minecraft:air") {
                    airId = runtimeId
                }
            }
        }
    }

    fun getBlockNameFromNbtMap(nbt: NbtMap): String {
        val sortedMap = sortedMapOf<String, String>()
        val stateMap = nbt.getCompound("states") ?: NbtMap.builder().build()
        stateMap.forEach { (key, value) ->
            sortedMap[key] = value.toString()
        }
        val sb = StringBuilder()
        sb.append(nbt.getString("name"))
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
        return sb.toString()
    }

    fun logUnknownBlock(id: Int) {
        if(!loggedUnknownBlocks.contains(id)) {
            loggedUnknownBlocks.add(id)
            logError("Unable to find block with runtime id $id")
        }
    }
}