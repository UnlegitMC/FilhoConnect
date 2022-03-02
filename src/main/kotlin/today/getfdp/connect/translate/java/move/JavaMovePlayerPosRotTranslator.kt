package today.getfdp.connect.translate.java.move

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaMovePlayerPosRotTranslator : TranslatorBase<ServerboundMovePlayerPosRotPacket> {

    override val intendedClass: Class<ServerboundMovePlayerPosRotPacket>
        get() = ServerboundMovePlayerPosRotPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundMovePlayerPosRotPacket) {
        provider.client.thePlayer.posX = packet.x
        provider.client.thePlayer.posY = packet.y
        provider.client.thePlayer.posZ = packet.z
        provider.client.thePlayer.rotationYaw = packet.yaw
        provider.client.thePlayer.rotationPitch = packet.pitch
        provider.client.thePlayer.move()
    }
}