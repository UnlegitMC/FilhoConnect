package today.getfdp.connect.translate.bedrock.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundChangeDifficultyPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket
import com.nukkitx.protocol.bedrock.packet.StartGamePacket
import com.nukkitx.protocol.bedrock.packet.TickSyncPacket
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase
import today.getfdp.connect.utils.game.DimensionUtils
import today.getfdp.connect.utils.game.GameUtils
import today.getfdp.connect.utils.protocol.PayloadEncoder


class BedrockStartGameTranslator : TranslatorBase<StartGamePacket> {

    override val intendedClass: Class<StartGamePacket>
        get() = StartGamePacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: StartGamePacket) {
        val dim = DimensionUtils.getById(packet.dimensionId)

        provider.client.thePlayer.dimension = dim

        // if client is not login, send login packet
        val loginPacket = ClientboundLoginPacket(packet.runtimeEntityId.toInt(), false,
            GameUtils.convertToGameMode(packet.playerGameType), GameUtils.convertToGameMode(packet.levelGameType),
            3, arrayOf("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"), // 3 dimensions
            DimensionUtils.dimensionCodec, dim.tag,
            "minecraft:overworld", packet.seed.toLong(), Int.MAX_VALUE, 32, 32 /** hmm? **/,
            false, true, false, false)
        provider.packetOut(loginPacket)
        provider.packetOut(ClientboundChangeDifficultyPacket(GameUtils.getDifficulty(packet.difficulty), false))

        // send client a position packet to close loading screen
        provider.client.thePlayer.runtimeId = packet.runtimeEntityId.toInt()
        provider.client.thePlayer.updatePosition(packet.playerPosition)
        provider.client.thePlayer.updateRotation(packet.rotation)
        provider.client.thePlayer.teleport()
        provider.client.thePlayer.movementMode = packet.playerMovementSettings.movementMode
        provider.client.thePlayer.blockPlaceAuth = packet.playerMovementSettings.isServerAuthoritativeBlockBreaking

        // send weather to client
        provider.client.theWorld.rain = packet.rainLevel
        provider.client.theWorld.thunder = packet.lightningLevel
        provider.client.theWorld.update()

        // tell bedrock server we are ready to play
        val tickSyncPacket = TickSyncPacket()
        tickSyncPacket.requestTimestamp = 0
        tickSyncPacket.responseTimestamp = 0
        provider.bedrockPacketOut(tickSyncPacket)

        provider.client.thePlayer.move()

        // send brand info
        PayloadEncoder.sendBrand(provider.client.session)

        provider.client.title("")
        provider.client.subtitle("")
    }
}