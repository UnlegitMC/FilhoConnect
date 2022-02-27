package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheRadiusPacket
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockChunkRadiusUpdatePacketTranslator : TranslatorBase<ChunkRadiusUpdatedPacket> {

    override val intendedClass: Class<ChunkRadiusUpdatedPacket>
        get() = ChunkRadiusUpdatedPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ChunkRadiusUpdatedPacket) {
        provider.packetOut(ClientboundSetChunkCacheRadiusPacket(packet.radius))
    }
}