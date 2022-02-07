package today.getfdp.connect.utils.game

import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.ListTag
import com.github.steveice10.opennbt.tag.builtin.StringTag

object DimensionUtils {

    val dimensionCodec = NBTUtils.readCompressedTag(this.javaClass.classLoader.getResourceAsStream("fc-data/dimension.nbt")) as CompoundTag

    enum class Dimension(val id: Int, val dimName: String) {
        OVERWORLD(0, "minecraft:overworld"),
        NETHER(1, "minecraft:the_nether"),
        END(2, "minecraft:the_end");

        val tag: CompoundTag

        init {
            val values = dimensionCodec
                .get<CompoundTag>("minecraft:dimension_type")
                .values().find { it.name == "value" } as ListTag
            tag = (values.find { (it as CompoundTag).get<StringTag>("name").value == dimName } as CompoundTag)
                    .get("element")
        }
    }
}