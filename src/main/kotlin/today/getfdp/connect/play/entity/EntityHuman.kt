package today.getfdp.connect.play.entity

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerLookAtPacket
import com.nukkitx.math.vector.Vector3f
import today.getfdp.connect.play.Client
import java.util.UUID

open class EntityHuman(client: Client) : Entity(client) {

    override fun updatePosition(v3f: Vector3f) {
        super.updatePosition(v3f)
        posY -= EYE_HEIGHT
    }

    open fun updatePositionAbsulute(v3f: Vector3f) {
        super.updatePosition(v3f)
    }

    override fun move() {
        super.move()
        client.send(ClientboundRotateHeadPacket(runtimeId, rotationYaw))
    }

    companion object {
        const val EYE_HEIGHT = 1.62
    }
}