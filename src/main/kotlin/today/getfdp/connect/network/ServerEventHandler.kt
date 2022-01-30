package today.getfdp.connect.network

import today.getfdp.connect.FConnect
import today.getfdp.connect.network.provider.AuthenticateProvider
import today.getfdp.connect.play.Client

class ServerEventHandler {

    /**
     * called when a client with PLAY protocol connects
     */
    fun onConnected(client: Client) {
        FConnect.logInfo("${client.name}[${client.session.host}:${client.session.port}] connected")
        client.provider = AuthenticateProvider()
    }

    /**
     * called when a client disconnects
     */
    fun onDisconnected(client: Client) {
        FConnect.logInfo("${client.name}[${client.session.host}:${client.session.port}] disconnected")
    }

    /**
     * Called when a client requests the motd.
     */
    fun getMotd(): Motd {
        return Motd("Welcome to the server!", 114514, 1919810)
    }
}