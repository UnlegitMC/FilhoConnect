package today.getfdp.connect.network.provider

import com.github.steveice10.packetlib.packet.Packet
import today.getfdp.connect.play.Client

abstract class AbstractPlayProvider(val client: Client) {

    open fun packetIn(packet: Packet) {

    }

    open fun packetOut(packet: Packet) {
        client.session.send(packet)
    }
}