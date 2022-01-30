package today.getfdp.connect.network.translate

import com.github.steveice10.packetlib.packet.Packet
import today.getfdp.connect.network.provider.BedrockProxyProvider

interface JavaTranslator {
    fun translate(provider: BedrockProxyProvider, packet: Packet)
}