package today.getfdp.connect.translate.java.move

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerStatusOnlyPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class JavaMovePlayerStatusOnlyTranslator : TranslatorBase<ServerboundMovePlayerStatusOnlyPacket> {

    override val intendedClass: Class<ServerboundMovePlayerStatusOnlyPacket>
        get() = ServerboundMovePlayerStatusOnlyPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerboundMovePlayerStatusOnlyPacket) {
        provider.client.thePlayer.onGround = packet.isOnGround
        provider.client.thePlayer.move()
    }
}