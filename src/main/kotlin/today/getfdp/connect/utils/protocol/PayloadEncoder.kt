package today.getfdp.connect.utils.protocol

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundCustomPayloadPacket
import com.github.steveice10.packetlib.Session
import net.kyori.adventure.text.Component
import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.other.getVarIntLength
import today.getfdp.connect.utils.other.writeVarInt
import java.nio.ByteBuffer

object PayloadEncoder {
    fun sendStringPayload(session: Session, channel: String, payload: String): String {
        val data = payload.toByteArray()
        val buf = ByteBuffer.allocate(data.size + getVarIntLength(data.size))
            .put(writeVarInt(data.size))
            .put(data)
        session.send(ClientboundCustomPayloadPacket(channel, buf.array()))
        return payload
    }

    fun sendBrand(session: Session) {
        sendStringPayload(session, "minecraft:brand", FConnect.PROGRAM_NAME)
        session.send(ClientboundChatPacket(Component.text("§8Proudly hosted by ${FConnect.PROGRAM_NAME} ver${FConnect.PROGRAM_VERSION}!")))
    }
}