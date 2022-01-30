package today.getfdp.connect.network

data class Motd(val message: String, val nowPlayers: Int, val maxPlayers: Int, val icon: ByteArray? = null)