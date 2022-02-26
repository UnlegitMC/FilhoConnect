package today.getfdp.connect.resources

import com.nukkitx.nbt.NBTInputStream
import com.nukkitx.nbt.NbtList
import com.nukkitx.nbt.NbtMap
import today.getfdp.connect.utils.other.Configuration
import java.io.DataInputStream
import java.io.File

object BedrockBlockPaletteHolder : ResourceHolder() {
    override val resourceName: String
        get() = "BlockPalette"
    override val resourcePath: String
        get() = Configuration[Configuration.Key.BLOCK_PALETTE]

    val blocks = mutableMapOf<Int, String>()
    val legacyBlocks = mutableMapOf<Int, String>()

    override fun init(file: File) {
        val tag = NBTInputStream(DataInputStream(file.inputStream())).readTag() as NbtList<*>
        tag.forEach { nbt ->
            if(nbt is NbtMap) {
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
                        sb.append(", ")
                    }
                    sb.delete(sb.length - 2, sb.length)
                    sb.append("]")
                }
                val name = sb.toString()

                blocks[nbt.getInt("runtimeId")] = name
                if(nbt.containsKey("id") && nbt.containsKey("data")) {
                    legacyBlocks[nbt.getInt("id") shl 6 or nbt.getShort("data").toInt()] = name
                }
            }
        }
    }
}