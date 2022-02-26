package today.getfdp.connect.translate.java.move

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaMovePlayerRotPacket : TranslatorBase<ServerboundMovePlayerRotPacket> {

    override val intendedClass: Class<ServerboundMovePlayerRotPacket>
        get() = ServerboundMovePlayerRotPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundMovePlayerRotPacket) {
        provider.client.thePlayer.rotationYaw = packet.yaw
        provider.client.thePlayer.rotationPitch = packet.pitch
        provider.client.thePlayer.move(MovePlayerPacket.Mode.HEAD_ROTATION)
    }
}