package today.getfdp.connect.translate.bedrock.play

import com.nukkitx.protocol.bedrock.packet.TransferPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockTransferTranslator : TranslatorBase<TransferPacket> {

    override val intendedClass: Class<TransferPacket>
        get() = TransferPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: TransferPacket) {
        // todo: transfer the player to another server
        provider.client.disconnect("Transfer to another server is not supported yet. (target=${packet.address}:${packet.port})")
    }
}