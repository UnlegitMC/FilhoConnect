package today.getfdp.connect.network

import java.awt.image.BufferedImage

data class Motd(val message: String, val nowPlayers: Int, val maxPlayers: Int, val icon: ByteArray? = null)