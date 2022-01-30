package today.getfdp.connect.network.provider

import com.github.steveice10.packetlib.packet.Packet
import today.getfdp.connect.play.Client

/**
 * Provides game play services, one [PlayProvider] instance can only be used by one [Client]
 */
abstract class PlayProvider {

    lateinit var client: Client

    /**
     * apply the provider to the client
     */
    open fun apply(client: Client) {
        this.client = client
    }

    /**
     * remove the provider from the client
     */
    abstract fun remove()

    /**
     * handle the packet
     */
    abstract fun packetIn(packet: Packet)

    /**
     * send the packet
     */
    open fun packetOut(packet: Packet) {
        client.session.send(packet)
    }
}