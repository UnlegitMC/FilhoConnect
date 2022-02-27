package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket
import com.nukkitx.protocol.bedrock.packet.NetworkChunkPublisherUpdatePacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockNetworkChunkPublisherUpdatePacketTranslator : TranslatorBase<NetworkChunkPublisherUpdatePacket> {

    override val intendedClass: Class<NetworkChunkPublisherUpdatePacket>
        get() = NetworkChunkPublisherUpdatePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: NetworkChunkPublisherUpdatePacket) {
        provider.packetOut(ClientboundSetChunkCacheCenterPacket(packet.position.x shr 4, packet.position.z shr 4))
    }
}