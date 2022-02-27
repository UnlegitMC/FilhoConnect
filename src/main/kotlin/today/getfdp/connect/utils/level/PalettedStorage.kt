package today.getfdp.connect.utils.level

import com.nukkitx.nbt.NBTInputStream
import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.util.stream.NetworkDataInputStream
import com.nukkitx.network.VarInts
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import today.getfdp.connect.resources.BedrockBlockPaletteHolder

class PalettedStorage(byteBuf: ByteBuf, header: Int? = null) {

    val bitArray: BitArray
    val palette: IntArray

    init {
        val paletteHeader = header ?: byteBuf.readByte().toInt()
        val isRuntime = (paletteHeader and 1) == 1
        val paletteVersion = paletteHeader or 1 shr 1

        val bitArrayVersion = BitArrayVersion.get(paletteVersion, true)

        val maxBlocksInSection = 4096 // 16*16*16

        bitArray = bitArrayVersion.createPalette(maxBlocksInSection)
        val wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection)

        for (wordIterationIndex in 0 until wordsSize) {
            val word = byteBuf.readIntLE()
            bitArray.words[wordIterationIndex] = word
        }

        val paletteSize = VarInts.readInt(byteBuf)
        palette = IntArray(paletteSize)
        val nbtStream = if (isRuntime) null else NBTInputStream(NetworkDataInputStream(ByteBufInputStream(byteBuf)))
        for (i in 0 until paletteSize) {
            if (isRuntime) {
                palette[i] = VarInts.readInt(byteBuf)
            } else {
                val map = (nbtStream!!.readTag() as NbtMap).toBuilder()
                val name = map["name"].toString()
                map.replace("name", if(!name.startsWith("minecraft:")) {
                    // For some reason, persistent chunks don't include the "minecraft:" that should be used in state names.
                    "minecraft:$name"
                } else {
                    name
                })
                palette[i] = BedrockBlockPaletteHolder.blockToRuntime[BedrockBlockPaletteHolder.getBlockNameFromNbtMap(map.build())] ?: BedrockBlockPaletteHolder.airId
            }
        }
    }

    fun getByIndex(index: Int): Int {
        return palette[bitArray[index]]
    }

    fun getIndex(x: Int, y: Int, z: Int): Int {
        return x shl 8 or (z shl 4) or y
    }

    fun get(x: Int, y: Int, z: Int): Int {
        return getByIndex(getIndex(x, y, z))
    }
}