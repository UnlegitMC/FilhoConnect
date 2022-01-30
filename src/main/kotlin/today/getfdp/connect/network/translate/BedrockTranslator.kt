package today.getfdp.connect.network.translate

import com.nukkitx.protocol.bedrock.BedrockPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider

interface BedrockTranslator {
    fun translate(provider: BedrockProxyProvider, packet: BedrockPacket)
}