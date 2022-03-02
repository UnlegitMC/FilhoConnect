package today.getfdp.connect.translate.bedrock.world

import com.github.steveice10.mc.protocol.data.game.chunk.ChunkSection
import com.github.steveice10.mc.protocol.data.game.chunk.DataPalette
import com.github.steveice10.mc.protocol.data.game.chunk.palette.SingletonPalette
import com.github.steveice10.mc.protocol.data.game.level.LightUpdateData
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag
import com.github.steveice10.packetlib.io.stream.StreamNetOutput
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.resources.BedrockBlockPaletteHolder
import today.getfdp.connect.resources.BiomeMappingHolder
import today.getfdp.connect.resources.BlockMappingHolder
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.DimensionUtils
import today.getfdp.connect.utils.level.PalettedStorage
import java.io.ByteArrayOutputStream
import java.util.*


/**
 * some of the code is from TunnelMC https://github.com/THEREALWWEFAN231/TunnelMC/blob/432014f75aef85fa42436fd9d4756c8625b9aed0/src/main/java/me/THEREALWWEFAN231/tunnelmc/translator/packet/world/LevelChunkTranslator.java
 */
class BedrockLevelChunkTranslator : TranslatorBase<LevelChunkPacket> {

    private val emptyChunkSectionBytes: ByteArray
    private val emptyHeightMap: CompoundTag

    init {
        // pre-write the chunk section into a byte array can make processing faster
        val chunkSection = ChunkSection()
        val bos = ByteArrayOutputStream()
        val data = StreamNetOutput(bos)

        fillPalette(chunkSection.chunkData)
        fillPalette(chunkSection.biomeData)

        ChunkSection.write(data, chunkSection, DimensionUtils.biomeCount)

        emptyChunkSectionBytes = bos.toByteArray()

        bos.close()
        data.close()

        emptyHeightMap = CompoundTag("")
        val array = LongArray(37)
        // send empty height map with right size, or client will log warnings
        repeat(37) {
            array[it] = 0
        }
        emptyHeightMap.put(LongArrayTag("MOTION_BLOCKING", array))
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
            val chunkSections = mutableListOf<ChunkSection>()

            // read the blocks data
            repeat(packet.subChunksLength) {
                val version = byteBuf.readByte().toInt()
                val chunkSection = ChunkSection()
                if (version == 0) {
                    readZeroChunk(byteBuf, chunkSection)
                } else {
                    readChunk(byteBuf, chunkSection, version)
                }
                chunkSections.add(chunkSection)
            }

            // read the biome data
            var has0Ver = false
            repeat(packet.subChunksLength) { // bedrock has hardcoded biome count to 24, but we just want to read the biome data with blocks in it
                val header = byteBuf.readByte().toInt()
                val paletteVersion = if(has0Ver) 0 else header or 1 shr 1
                val biomeData = chunkSections[it].biomeData
                if(paletteVersion != 0) {
                    val storage = PalettedStorage(byteBuf, header)

                    var index = 0
                    for (x in 0..3) {
                        for (y in 0..3) {
                            for (z in 0..3) {
                                val bedrockId = storage.get(x * 4, y * 4, z * 4)
                                val javaId = BiomeMappingHolder.javaToRuntime[BiomeMappingHolder.bedrockToJava[bedrockId] ?: "minecraft:the_void"] ?: 0

                                biomeData.storage[index] = biomeData.palette.stateToId(javaId).coerceAtLeast(0)

                                index++
                            }
                        }
                    }
                } else {
                    has0Ver = true
                    fillPalette(biomeData) // todo: translate palette with version 0
                }
            }

            chunkSections.forEach {
                ChunkSection.write(data, it, DimensionUtils.biomeCount)
            }
        } finally {
            byteBuf.release() // make sure to release the buffer or the memory will leak
        }

        val sectionLast = provider.client.thePlayer.dimension.chunkHeightY - packet.subChunksLength
        if(sectionLast > 0) {
            repeat(provider.client.thePlayer.dimension.chunkHeightY) {
                data.write(emptyChunkSectionBytes)
            }
        }

        // just send an empty light data, client will calculate light itself
        val light = LightUpdateData(BitSet(), BitSet(), BitSet(), BitSet(), emptyList(), emptyList(), true)
        // todo: calculate height map to make client load chunk faster

        bos.close()
        data.close()
        provider.packetOut(ClientboundLevelChunkWithLightPacket(packet.chunkX, packet.chunkZ, bos.toByteArray(), emptyHeightMap, emptyArray(), light))
    }

    /**
     * chunk with version 1 or 8 is used in modern bedrock servers
     */
    private fun readChunk(byteBuf: ByteBuf, chunkSection: ChunkSection, version: Int) {
        val layers = if(version == 1) 1 else byteBuf.readByte().toInt()
        repeat(layers) {
            val storage = PalettedStorage(byteBuf)

            var index = 0
            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in 0..15) {
                        val bedrockId = storage.getByIndex(index)
                        val name = BedrockBlockPaletteHolder.runtimeToBlock[bedrockId]
                        if (name == null) {
                            BedrockBlockPaletteHolder.logUnknownBlock(bedrockId)
                        }
                        if (it == 0) {
                            val javaName = BlockMappingHolder.bedrockToJava[name]
                            if (javaName == null) {
                                BlockMappingHolder.logUnknownBlock(name ?: "null")
                            }
                            val block = BlockMappingHolder.javaBlockToState[BlockMappingHolder.bedrockToJava[name]] ?: 1
                            chunkSection.setBlock(x, y, z, block)
                        } else {
                            if(bedrockId == BedrockBlockPaletteHolder.airId) {
                                // waterlogged
                                if(BlockMappingHolder.bedrockToJava[name]?.startsWith("minecraft:water") == true) {
                                    val originName = BlockMappingHolder.javaStateToBlock[chunkSection.getBlock(x, y, z)] ?: continue
                                    chunkSection.setBlock(x, y, z,
                                        BlockMappingHolder.javaBlockToState[originName.replace("waterlogged=false", "waterlogged=true")] ?: continue)
                                }
                            }
                        }
                        index++
                    }
                }
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
                    val name = BedrockBlockPaletteHolder.legacyBlocks[id shl 6 or meta] ?: "minecraft:stone"
                    chunkSection.setBlock(x, y, z,
                        BlockMappingHolder.javaBlockToState[BlockMappingHolder.bedrockToJava[name]] ?: 1)
                }
            }
        }
    }

    /**
     * fill the palette with specified value
     */
    private fun fillPalette(dataPalette: DataPalette, state: Int = 0) {
        val bitStorage = dataPalette.storage
        dataPalette.palette = SingletonPalette(state)
        for(i in 0 until bitStorage.size) {
            bitStorage[i] = 0 // singleton palette, so all bits are 0
        }
    }

    override fun async(): Boolean {
        return true
    }
}