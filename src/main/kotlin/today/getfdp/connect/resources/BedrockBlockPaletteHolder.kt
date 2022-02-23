package today.getfdp.connect.resources

import com.github.steveice10.opennbt.NBTIO
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

    override fun init(file: File) {
        val tag = NBTInputStream(DataInputStream(file.inputStream())).readTag() as NbtList<*>
        tag.forEach { nbt ->
            if(nbt is NbtMap) {
//                println(nbt)
            }
        }
    }
}