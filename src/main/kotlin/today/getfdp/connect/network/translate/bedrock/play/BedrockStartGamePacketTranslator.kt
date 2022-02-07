package today.getfdp.connect.network.translate.bedrock.play

import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket
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

        // tell bedrock server we are ready to play
        val setLocalPlayerAsInitializedPacket = SetLocalPlayerAsInitializedPacket()
        setLocalPlayerAsInitializedPacket.runtimeEntityId = packet.runtimeEntityId
        provider.bedrockPacketOut(setLocalPlayerAsInitializedPacket)
    }
}