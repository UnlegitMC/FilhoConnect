package today.getfdp.connect.network.translate.bedrock.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundRespawnPacket
import com.nukkitx.protocol.bedrock.packet.StartGamePacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.network.translate.TranslatorBase

class BedrockStartGamePacketTranslator : TranslatorBase<StartGamePacket> {

    override val intendedClass: Class<StartGamePacket>
        get() = StartGamePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: StartGamePacket) {
//        if(provider.client.isLogin) {
//            // if client is login, send respawn packet
//            val respawnPacket = ClientboundRespawnPacket()
//        } else {
//            // if client is not login, send login packet
//            val loginPacket = ClientboundLoginPacket()
//        }
    }
}