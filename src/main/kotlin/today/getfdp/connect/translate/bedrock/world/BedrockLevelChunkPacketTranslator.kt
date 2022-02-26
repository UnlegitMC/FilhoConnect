package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.data.game.chunk.ChunkSection
import com.github.steveice10.mc.protocol.data.game.level.LightUpdateData
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag
import com.github.steveice10.packetlib.io.stream.StreamNetOutput
import com.nukkitx.network.VarInts
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.resources.BedrockBlockPaletteHolder
import today.getfdp.connect.resources.BlockMappingHolder
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.DimensionUtils
import today.getfdp.connect.utils.level.BitArrayVersion
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * some of the code is from TunnelMC https://github.com/THEREALWWEFAN231/TunnelMC/blob/432014f75aef85fa42436fd9d4756c8625b9aed0/src/main/java/me/THEREALWWEFAN231/tunnelmc/translator/packet/world/LevelChunkTranslator.java
 */
class BedrockLevelChunkPacketTranslator : TranslatorBase<LevelChunkPacket> {

    private val emptyChunkSection: ChunkSection = ChunkSection()

    init {
        for(x in 0..15) {
            for(y in 0..15) {
                for (z in 0..15) {
                    emptyChunkSection.setBlock(x, y, z, 0)
                }
            }
        }
        fillBiome(emptyChunkSection)
    }

    override val intendedClass: Class<LevelChunkPacket>
        get() = LevelChunkPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: LevelChunkPacket) {
        val bos = ByteArrayOutputStream()
        val data = StreamNetOutput(bos)

        // use NetInput to make reading easier
        val byteBuf = Unpooled.buffer()
        byteBuf.writeBytes(packet.data)

        try {
            repeat(packet.subChunksLength) {
                val version = byteBuf.readByte().toInt()
                val chunkSection = ChunkSection()
                fillBiome(chunkSection) // todo: translate biome
                if (version == 0) {
                    readZeroChunk(byteBuf, chunkSection)
                } else {
                    readChunk(byteBuf, chunkSection, version)
                }
                ChunkSection.write(data, chunkSection, DimensionUtils.biomeCount)
            }
        } finally {
            byteBuf.release() // make sure to release the buffer or the memory will leak
        }

        val sectionLast = provider.client.thePlayer.dimension.chunkHeightY - packet.subChunksLength
        if(sectionLast > 0) {
            repeat(provider.client.thePlayer.dimension.chunkHeightY) {
                ChunkSection.write(data, emptyChunkSection, DimensionUtils.biomeCount)
            }
        }

        // just send an empty light data, client will calculate light itself
        val light = LightUpdateData(BitSet(), BitSet(), BitSet(), BitSet(), emptyList(), emptyList(), true)
        val height = CompoundTag("")
        val array = LongArray(37)
        // send empty height map with right size, or client will log warnings
        // todo: calculate height map to make client load chunk faster
        repeat(37) {
            array[it] = 0
        }
        height.put(LongArrayTag("MOTION_BLOCKING", array))

        bos.close()
        data.close()
        provider.packetOut(ClientboundLevelChunkWithLightPacket(packet.chunkX, packet.chunkZ, bos.toByteArray(), height, emptyArray(), light))
    }

    /**
     * chunk with version 1 or 8 is used in modern bedrock servers
     */
    private fun readChunk(byteBuf: ByteBuf, chunkSection: ChunkSection, version: Int) {
        val layers = if(version == 1) 1 else byteBuf.readByte().toInt()
        repeat(layers) {
            val paletteHeader = byteBuf.readByte().toInt()
            val isRuntime = (paletteHeader and 1) == 1
            val paletteVersion = paletteHeader or 1 shr 1

            val bitArrayVersion = BitArrayVersion.get(paletteVersion, true)

            val maxBlocksInSection = 4096 // 16*16*16

            val bitArray = bitArrayVersion.createPalette(maxBlocksInSection)
            val wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection)

            for (wordIterationIndex in 0 until wordsSize) {
                val word = byteBuf.readIntLE()
                bitArray.words[wordIterationIndex] = word
            }

            val paletteSize = VarInts.readInt(byteBuf)
            val sectionPalette = IntArray(paletteSize)
//            val nbtStream = if (isRuntime) null else NBTInputStream(NetworkDataInputStream(ByteBufInputStream(byteBuf)))
            for (i in 0 until paletteSize) {
                if (isRuntime) {
                    sectionPalette[i] = VarInts.readInt(byteBuf)
                } else {
                    TODO("not implemented")
                }
            }

            if (it == 0) {
                var index = 0
                for (x in 0..15) {
                    for (z in 0..15) {
                        for (y in 0..15) {
                            val paletteIndex = bitArray[index]
                            val mcbeBlockId = sectionPalette[paletteIndex]
                            val name = BedrockBlockPaletteHolder.blocks[mcbeBlockId] ?: continue
                            val block = BlockMappingHolder.javaBlockToState[BlockMappingHolder.bedrockToJava[name]] ?: continue
                            chunkSection.setBlock(x, y, z, block)
                            index++
                        }
                    }
                }
            } else {
                // todo: waterlogged
            }
        }
    }

    /**
     * chunk with version 0 is used in PocketMine-MP
     */
    private fun readZeroChunk(byteBuf: ByteBuf, chunkSection: ChunkSection) {
        val blockIds = ByteArray(4096)
        byteBuf.readBytes(blockIds)

        val metaIds = ByteArray(2048)
        byteBuf.readBytes(metaIds)

        for (x in 0..15) {
            for (y in 0..15) {
                for (z in 0..15) {
                    val idx = (x shl 8) + (z shl 4) + y
                    val id = blockIds[idx].toInt()
                    val meta = metaIds[idx shr 1].toInt() shr (idx and 1) * 4 and 15
                    val name = BedrockBlockPaletteHolder.legacyBlocks[id shl 6 or meta] ?: continue
                    chunkSection.setBlock(x, y, z,
                        BlockMappingHolder.javaBlockToState[BlockMappingHolder.bedrockToJava[name] ?: continue] ?: 1)
                }
            }
        }
    }

    /**
     * fill the biomes in the chunk section with plain
     */
    private fun fillBiome(chunkSection: ChunkSection) {
        val bitStorage = chunkSection.biomeData.storage
        val id = chunkSection.biomeData.palette.stateToId(0)
        for(i in 0 until bitStorage.size) {
            bitStorage[i] = id
        }
    }

//    override fun async(): Boolean {
//        return true
//    }
}