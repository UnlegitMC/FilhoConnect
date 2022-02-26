package today.getfdp.connect.translate.java.move

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaMovePlayerPosPacketTranslator : TranslatorBase<ServerboundMovePlayerPosPacket> {

    override val intendedClass: Class<ServerboundMovePlayerPosPacket>
        get() = ServerboundMovePlayerPosPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundMovePlayerPosPacket) {
        provider.client.thePlayer.posX = packet.x
        provider.client.thePlayer.posY = packet.y
        provider.client.thePlayer.posZ = packet.z
        provider.client.thePlayer.move()
    }
}