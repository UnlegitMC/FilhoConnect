package today.getfdp.connect.utils.other

import java.io.InputStream
import kotlin.experimental.or

fun writeVarInt(value: Int): ByteArray {
    var value = value
    val data = ByteArray(getVarIntLength(value))
    var index = 0
    do {
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0) {
            temp = temp or 128.toByte()
        }
        data[index] = temp
        index++
    } while (value != 0)
    return data
}

fun getVarIntLength(number: Int): Int {
    if (number and -0x80 == 0) {
        return 1
    } else if (number and -0x4000 == 0) {
        return 2
    } else if (number and -0x200000 == 0) {
        return 3
    } else if (number and -0x10000000 == 0) {
        return 4
    }
    return 5
}

fun readInputStream(inputStream: InputStream): String {
    return inputStream.reader(Charsets.UTF_8).readText()
}