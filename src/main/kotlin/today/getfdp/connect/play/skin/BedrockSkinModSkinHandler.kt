package today.getfdp.connect.play.skin

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundCustomPayloadPacket
import com.github.steveice10.packetlib.io.NetOutput
import com.github.steveice10.packetlib.io.stream.StreamNetOutput
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.other.Configuration
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.ceil

class BedrockSkinModSkinHandler(val client: Client) : ISkinHandler {

    /**
     * Minecraft 1.16's maximum
     */
    private val customPayloadMaxBytes = 1048576

    /**
     * channel for bedrock skin mod
     */
    private val channel = "bedrockskin:data"

    override fun handle(uuid: UUID, skin: SerializedSkin) {
        if(uuid == client.uuid && !Configuration.get<Boolean>(Configuration.Key.SELF_SKIN)) return

        val chunks = ceil(skin.skinData.image.size / (customPayloadMaxBytes - 24/* header size */).toDouble()).toInt()
        client.send(ClientboundCustomPayloadPacket(channel, getSkinInfo(uuid, skin, chunks)))
        getSkinData(uuid, skin, chunks).forEach {
            client.send(ClientboundCustomPayloadPacket(channel, it))
        }
        getCape(uuid, skin)?.let { client.send(ClientboundCustomPayloadPacket(channel, it)) }
    }

    private fun getCape(uuid: UUID, skin: SerializedSkin): ByteArray? {
        if(skin.capeData.width == 0 || skin.capeData.height == 0 || skin.capeData.image.isEmpty()) return null

        val bos = ByteArrayOutputStream()
        val out = StreamNetOutput(bos)

        out.writeInt(MessageType.CAPE.ordinal)
        out.writeInt(1) // version

        out.writeLong(uuid.mostSignificantBits)
        out.writeLong(uuid.leastSignificantBits)

        out.writeInt(skin.capeData.width)
        out.writeInt(skin.capeData.height)

        out.writeGString(skin.capeId)

        out.writeInt(skin.capeData.image.size)
        out.writeBytes(skin.capeData.image)

        out.close()
        bos.close()

        return bos.toByteArray()
    }

    private fun getSkinData(uuid: UUID, skin: SerializedSkin, chunks: Int): List<ByteArray> {
        val list = mutableListOf<ByteArray>()

        val headerBytes = ByteArrayOutputStream().let {
            val out = StreamNetOutput(it)

            out.writeInt(MessageType.SKIN_DATA.ordinal)

            out.writeLong(uuid.mostSignificantBits)
            out.writeLong(uuid.leastSignificantBits)

            out.close()
            it.close()

            it.toByteArray()
        }

        for(i in 0 until chunks) {
            val bos = ByteArrayOutputStream()
            val out = StreamNetOutput(bos)

            out.writeBytes(headerBytes)
            out.writeInt(i) // index

            val offset = i * (customPayloadMaxBytes - 20 - 4)
            skin.skinData.image.copyOfRange(offset, (offset + (customPayloadMaxBytes - 20 - 4)).coerceAtMost(skin.skinData.image.size)).let {
                out.writeBytes(it)
            }

            out.close()
            bos.close()
            list.add(bos.toByteArray())
        }

        return list
    }

    private fun getSkinInfo(uuid: UUID, skin: SerializedSkin, chunks: Int): ByteArray {
        val bos = ByteArrayOutputStream()
        val out = StreamNetOutput(bos)

        out.writeInt(MessageType.SKIN_INFORMATION.ordinal)
        out.writeInt(1) // version

        out.writeLong(uuid.mostSignificantBits)
        out.writeLong(uuid.leastSignificantBits)

        out.writeInt(skin.skinData.width)
        out.writeInt(skin.skinData.height)

        val hasGeometry = skin.geometryData != null && skin.geometryData.trim() != "null" && skin.geometryName != null
        out.writeBoolean(hasGeometry)
        if(hasGeometry) {
            out.writeGString(skin.geometryData)
            out.writeGString(skin.skinResourcePatch)
        }

        // skin data chunk size
        out.writeInt(chunks)

        out.close()
        bos.close()

        return bos.toByteArray()
    }

    private fun NetOutput.writeGString(str: String) {
        this.writeInt(str.length)
        this.writeBytes(str.toByteArray(Charsets.UTF_8))
    }

    enum class MessageType {
        CAPE, SKIN_INFORMATION, SKIN_DATA
    }
}