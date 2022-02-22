package today.getfdp.connect.translate.bedrock.connect

import com.nukkitx.protocol.bedrock.packet.ClientToServerHandshakePacket
import com.nukkitx.protocol.bedrock.packet.ServerToClientHandshakePacket
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.network.JWTUtils
import java.util.*

class BedrockServerToClientHandshakePacketTranslator : TranslatorBase<ServerToClientHandshakePacket> {

    override val intendedClass: Class<ServerToClientHandshakePacket>
        get() = ServerToClientHandshakePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: ServerToClientHandshakePacket) {
        try {
            println(packet.jwt)
            val jwtSplit = packet.jwt.split(".")
            val headerObject = JWTUtils.parseJsonObj(String(Base64.getDecoder().decode(jwtSplit[0])))
            val payloadObject = JWTUtils.parseJsonObj(String(Base64.getDecoder().decode(jwtSplit[1])))
            val serverKey = EncryptionUtils.generateKey(headerObject.string("x5u"))
            val key = EncryptionUtils.getSecretKey(provider.client.loginHelper.keyPair.private, serverKey,
                Base64.getDecoder().decode(payloadObject.string("salt")))
            provider.bedrockClient.session.enableEncryption(key)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val clientToServerHandshake = ClientToServerHandshakePacket()
        provider.bedrockPacketOut(clientToServerHandshake)
    }
}