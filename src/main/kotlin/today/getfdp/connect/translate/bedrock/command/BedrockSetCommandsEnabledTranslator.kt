package today.getfdp.connect.translate.bedrock.command

import com.nukkitx.protocol.bedrock.packet.SetCommandsEnabledPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockSetCommandsEnabledTranslator : TranslatorBase<SetCommandsEnabledPacket> {

    override val intendedClass: Class<SetCommandsEnabledPacket>
        get() = SetCommandsEnabledPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: SetCommandsEnabledPacket) {
        provider.client.commandsEnabled = packet.isCommandsEnabled
    }
}