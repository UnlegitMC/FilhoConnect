package today.getfdp.connect.network.translate

import com.github.steveice10.packetlib.packet.Packet
import com.nukkitx.protocol.bedrock.BedrockPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider

interface TranslatorBase<T> {

    val intendedClass: Class<out T>

    fun translate(provider: BedrockProxyProvider, packet: T)
}

interface BedrockTranslator : TranslatorBase<BedrockPacket>

interface JavaTranslator : TranslatorBase<Packet>