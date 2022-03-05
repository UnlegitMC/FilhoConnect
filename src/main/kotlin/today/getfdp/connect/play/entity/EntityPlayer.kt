package today.getfdp.connect.play.entity

import com.github.steveice10.mc.auth.data.GameProfile
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerInfoPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddPlayerPacket
import net.kyori.adventure.text.Component
import today.getfdp.connect.play.Client
import java.util.*

open class EntityPlayer(client: Client) : EntityHuman(client) {

    open lateinit var name: String
    open lateinit var uuid: UUID

    override fun spawn() {
        super.spawn()

        val hasPlayerList = client.theWorld.playerUuids.contains(this.uuid) // java edition needs player uuid in player list to spawn
        var entry: Array<PlayerListEntry>? = null
        if(!hasPlayerList) {
            entry = arrayOf(PlayerListEntry(GameProfile(this.uuid, this.name), GameMode.SURVIVAL, 0, Component.text(name)))
            client.send(ClientboundPlayerInfoPacket(PlayerListEntryAction.ADD_PLAYER, entry))
        }

        client.send(ClientboundAddPlayerPacket(runtimeId, uuid, posX, posY, posZ, rotationYaw, rotationPitch))

        if(!hasPlayerList) {
            client.send(ClientboundPlayerInfoPacket(PlayerListEntryAction.REMOVE_PLAYER, entry!!))
        }
    }
}