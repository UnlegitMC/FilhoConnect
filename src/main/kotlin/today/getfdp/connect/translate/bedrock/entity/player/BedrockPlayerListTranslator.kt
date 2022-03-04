package today.getfdp.connect.translate.bedrock.entity.player

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerInfoPacket
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.translate.TranslatorBase

class BedrockPlayerListTranslator : TranslatorBase<PlayerListPacket> {

    override val intendedClass: Class<PlayerListPacket>
        get() = PlayerListPacket::class.java

    override fun translate(provider: BedrockProxyProvider, packet: PlayerListPacket) {
        val entries = packet.entries.map { entry ->
            val uuid = provider.client.fixUUID(entry.uuid)
            provider.client.modManager.skinHandler?.handle(uuid, entry.skin)
            PlayerListEntry(GameProfile(uuid, entry.name), GameMode.SURVIVAL, 1, Component.text(entry.name))
        }
        provider.packetOut(ClientboundPlayerInfoPacket(when(packet.action) {
            PlayerListPacket.Action.ADD -> {
                entries.forEach { provider.client.theWorld.playerUuids.add(it.profile.id) }
                PlayerListEntryAction.ADD_PLAYER
            }
            PlayerListPacket.Action.REMOVE -> {
                entries.forEach { provider.client.theWorld.playerUuids.remove(it.profile.id) }
                PlayerListEntryAction.REMOVE_PLAYER
            }
        }, entries.toTypedArray()))
    }
}