package today.getfdp.connect.resources

import com.beust.klaxon.JsonObject
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.StringTag
import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.game.DimensionUtils
import today.getfdp.connect.utils.other.Configuration
import java.io.File

object BiomeMappingHolder : ResourceHolder() {

    override val resourceName: String
        get() = "BiomeMapping"
    override val resourcePath: String
        get() = Configuration[Configuration.Key.BIOME_MAPPING]
    
    val bedrockToJava = hashMapOf<Int, String>()
    val javaToRuntime = hashMapOf<String, Int>()

    override fun init(file: File) {
        val json = FConnect.parser.parse(file.reader(Charsets.UTF_8)) as JsonObject

        json.forEach { (name, obj) ->
            val bedrockId = (obj as JsonObject).int("bedrock_id")!!
            bedrockToJava[bedrockId] = name
        }

        DimensionUtils.biomes.forEachIndexed { index, tag ->
            val compound = tag as CompoundTag
            javaToRuntime[compound.get<StringTag>("name").value] = index
        }
    }
}