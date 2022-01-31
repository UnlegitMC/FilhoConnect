package today.getfdp.connect.console.commands

import com.github.steveice10.mc.protocol.codec.MinecraftCodec
import today.getfdp.connect.FConnect
import today.getfdp.connect.console.Command
import today.getfdp.connect.utils.logInfo

class VersionCommand : Command("version", "Print the version of the application") {
    override fun execute(args: Array<String>) {
        logInfo("Current on §l${FConnect.PROGRAM_NAME} ver ${FConnect.PROGRAM_VERSION}§r" +
                " for Minecraft: Java Edition §l${MinecraftCodec.CODEC.minecraftVersion}§r, Minecraft: Bedrock Edition §l${FConnect.bedrockCodec.minecraftVersion}§r")
    }
}