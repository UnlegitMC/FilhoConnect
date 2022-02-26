package today.getfdp.connect.utils.game

import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.IntTag
import com.github.steveice10.opennbt.tag.builtin.ListTag
import com.github.steveice10.opennbt.tag.builtin.StringTag

object DimensionUtils {

    val dimensionCodec = NBTUtils.readCompressedTag(this.javaClass.classLoader.getResourceAsStream("fc-data/dimension.nbt")) as CompoundTag
    val biomes = dimensionCodec.get<CompoundTag>("minecraft:worldgen/biome").get<ListTag>("value")
    val biomeCount = biomes.size()

    fun getById(id: Int): Dimension {
        return Dimension.values().find { it.id == id } ?: Dimension.OVERWORLD
    }

    enum class Dimension(val id: Int, val dimName: String) {
        OVERWORLD(0, "minecraft:overworld"),
        NETHER(1, "minecraft:the_nether"),
        END(2, "minecraft:the_end");

        val tag: CompoundTag
        val minY: Int
        val heightY: Int
        val chunkMinY: Int
        val chunkHeightY: Int

        init {
            val values = dimensionCodec
                .get<CompoundTag>("minecraft:dimension_type")
                .values().find { it.name == "value" } as ListTag
            tag = (values.find { (it as CompoundTag).get<StringTag>("name").value == dimName } as CompoundTag)
                    .get("element")

            minY = tag.get<IntTag>("min_y").value
            heightY = tag.get<IntTag>("height").value
            chunkMinY = minY shr 4
            chunkHeightY = heightY shr 4
        }
    }
}