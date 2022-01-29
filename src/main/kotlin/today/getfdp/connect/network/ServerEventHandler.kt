package today.getfdp.connect.network

import today.getfdp.connect.play.Client

class ServerEventHandler {

    /**
     * called when a client with PLAY protocol connects
     */
    fun onConnected(client: Client) {

    }

    /**
     * called when a client disconnects
     */
    fun onDisconnected(client: Client) {

    }

    /**
     * Called when a client requests the motd.
     */
    fun getMotd(): Motd {
        return Motd("Welcome to the server!", 114514, 1919810)
    }
}