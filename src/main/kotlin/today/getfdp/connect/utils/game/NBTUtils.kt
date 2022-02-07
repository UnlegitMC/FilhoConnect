package today.getfdp.connect.utils.game

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.github.steveice10.opennbt.NBTIO
import com.github.steveice10.opennbt.tag.builtin.*
import com.nukkitx.nbt.NbtList
import com.nukkitx.nbt.NbtMap
import com.nukkitx.nbt.NbtType
import java.io.InputStream
import java.util.zip.GZIPInputStream

object NBTUtils {
    fun toNbtMap(json: JsonObject): NbtMap {
        val builder = NbtMap.builder()
        json.forEach { (key, value) ->
            when (value) {
                is JsonObject -> builder[key] = toNbtMap(value)
                is JsonArray<*> -> builder[key] = toNbtList(value)
                else -> {
                    if(value != null) {
                        builder[key] = value
                    }
                }
            }
        }
        return builder.build()
    }

    fun toNbtList(list: List<*>): NbtList<*> {
        if(list.isEmpty()) {
            return NbtList.EMPTY
        }
        return when(list.first()) {
            is Int -> toNbtListTyped(list as List<Int>, Int::class.java)
            is Long -> toNbtListTyped(list as List<Long>, Long::class.java)
            is Double -> toNbtListTyped(list as List<Double>, Double::class.java)
            is String -> toNbtListTyped(list as List<String>, String::class.java)
            is Boolean -> toNbtListTyped(list as List<Boolean>, Boolean::class.java)
            is Float -> toNbtListTyped(list as List<Float>, Float::class.java)
            is Byte -> toNbtListTyped(list as List<Byte>, Byte::class.java)
            is Short -> toNbtListTyped(list as List<Short>, Short::class.java)
            is ByteArray -> toNbtListTyped(list as List<ByteArray>, ByteArray::class.java)
            is JsonObject -> toNbtListTyped(list.map { toNbtMap(it!! as JsonObject) }, NbtMap::class.java)
            else -> throw IllegalArgumentException("Unsupported type: ${list.first()}")
        }
    }

    private fun <T> toNbtListTyped(list: List<T>, klass: Class<T>): NbtList<T> {
        return NbtList(NbtType.byClass(klass), list.toCollection(ArrayList()))
    }

    fun toCompound(json: JsonObject, name: String = ""): CompoundTag {
        val tag = CompoundTag(name)
        json.forEach { (key, value) ->
        }
        return tag
    }

    fun toListTag(jsonArray: JsonArray<*>, name: String): ListTag {
        val listTag = ListTag(name)
        jsonArray.forEach { listTag.add(toTag(it!!, "")) }
        return listTag
    }

    fun toTag(value: Any, name: String): Tag {
        return when (value) {
            is JsonObject -> toCompound(value, name)
            is Int -> IntTag(name, value)
            is Long -> LongTag(name, value)
            is Double -> DoubleTag(name, value)
            is String -> StringTag(name, value)
            is Boolean -> ByteTag(name, if (value) 1 else 0)
            is Float -> FloatTag(name, value)
            is Byte -> ByteTag(name, value)
            is Short -> ShortTag(name, value)
            is ByteArray -> ByteArrayTag(name, value)
            is JsonArray<*> -> toTag(value, name)
            else -> throw IllegalArgumentException("Unsupported type: $value")
        }
    }

    fun toJsonObject(map: NbtMap): JsonObject {
        val json = JsonObject()
        map.forEach { (key, value) ->
            when (value) {
                is NbtMap -> json[key] = toJsonObject(value)
                is NbtList<*> -> json[key] = toJsonArray(value)
                else -> json[key] = value
            }
        }
        return json
    }

    fun <T> toJsonArray(list: NbtList<T>): JsonArray<T> {
        val array = JsonArray<T>()
        list.forEach { array.add(it) }
        return array
    }

    fun readCompressedTag(input: InputStream, littleEndian: Boolean = false): Tag {
        return NBTIO.readTag(GZIPInputStream(input), littleEndian)
    }
}