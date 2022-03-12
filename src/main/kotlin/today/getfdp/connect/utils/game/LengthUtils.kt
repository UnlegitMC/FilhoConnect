package today.getfdp.connect.utils.game

object LengthUtils {

    fun forName(name: String): String {
        return name.substring(0, 16.coerceAtMost(name.length))
    }
}