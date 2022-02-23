package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.data.game.chunk.ChunkSection
import com.github.steveice10.mc.protocol.data.game.chunk.palette.GlobalPalette
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket
import com.github.steveice10.packetlib.io.NetInput
import com.github.steveice10.packetlib.io.stream.StreamNetInput
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket
import io.netty.buffer.Unpooled
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import java.io.ByteArrayInputStream


class BedrockLevelChunkPacketTranslator : TranslatorBase<LevelChunkPacket> {

    override val intendedClass: Class<LevelChunkPacket>
        get() = LevelChunkPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: LevelChunkPacket) {
        // use NetInput to make reading easier
//        val nin = StreamNetInput(ByteArrayInputStream(packet.data))
//
//        repeat(packet.subChunksLength) {
//            val version = nin.readByte().toInt()
//            if (version != STREAM_STORAGE_VERSION) { // TODO: add 0 version support to make PocketMine compatible
//                throw IllegalStateException("Unsupported chunk version: $version")
//            }
//            val layers = nin.readByte().toInt()
//            repeat(layers) {
//                val paletteHeader = nin.readByte().toInt()
//                val isRuntime = (paletteHeader and 1) == 1
//                val paletteVersion = paletteHeader or 1 shr 1
//            }
//            return
//        }

//        val levelPacket = ClientboundLevelChunkWithLightPacket()
//
//        val cs = ChunkSection()
//        cs.setBlock(0, 0, 0, 1)
//        cs.biomeData.set(0, 0, 0, 1)

        // we don't need to release NetInput, it's done automatically. That's why we don't use ByteBuf
    }

    override fun async(): Boolean {
        return true
    }

    companion object {
        const val STREAM_STORAGE_VERSION = 8
    }
}