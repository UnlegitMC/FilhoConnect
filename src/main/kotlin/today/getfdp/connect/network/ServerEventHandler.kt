package today.getfdp.connect.network

import today.getfdp.connect.FConnect
import today.getfdp.connect.network.provider.AuthenticateProvider
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.utility.BedrockConnections
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logInfo

class ServerEventHandler {

    private val timeoutMotd = Motd("§4timed out", -1, -1)

    /**
     * called when a client with PLAY protocol connects
     */
    fun onConnected(client: Client) {
        logInfo("${client.name}[${client.session.host}:${client.session.port}] connected")
        client.provider = if(Configuration[Configuration.Key.ONLINE_MODE]) {
            AuthenticateProvider()
        } else {
            BedrockProxyProvider()
        }
    }

    /**
     * called when a client disconnects
     */
    fun onDisconnected(client: Client) {
        client.provider = null
        logInfo("${client.name}[${client.session.host}:${client.session.port}] disconnected")
    }

    /**
     * Called when a client requests the motd.
     */
    fun getMotd(): Motd {
        // ping the remote server to get the motd
        try {
            var wait = true
            val time = System.currentTimeMillis()
            var motd = timeoutMotd
            BedrockConnections.getClient().ping(BedrockConnections.targetAddress).whenComplete { pong, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                    motd = Motd("§c$throwable", -1, -1, "§cFrom server")
                    wait = false
                    return@whenComplete
                }
                motd = Motd("${pong.motd}\n§r${pong.subMotd}", pong.playerCount, pong.maximumPlayerCount,
                    "Edition: ${pong.edition}\n" +
                            "GameType: ${pong.gameType}\n" +
                            "Version: ${pong.version}\n" +
                            "NintendoLimited: ${pong.isNintendoLimited}\n" +
                            "§8Proudly hosted by ${FConnect.PROGRAM_NAME} ver${FConnect.PROGRAM_VERSION}!")
                wait = false
            }
            while (wait) {
                Thread.sleep(10)
                if (System.currentTimeMillis() - time > 5000) {
                    wait = false
                }
            }
            return motd
        } catch (t: Throwable) {
            t.printStackTrace()
            return Motd("§c$t", -1, -1, "§cFrom client")
        }
    }
}