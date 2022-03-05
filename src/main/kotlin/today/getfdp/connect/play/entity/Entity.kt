package today.getfdp.connect.play.entity

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundTeleportEntityPacket
import com.nukkitx.math.vector.Vector2f
import com.nukkitx.math.vector.Vector3f
import today.getfdp.connect.play.Client

open class Entity(protected val client: Client) {

    open var posX = 0.0
    open var posY = 0.0
    open var posZ = 0.0
    open var onGround = false
    open var rotationYaw = 0f
    open var rotationPitch = 0f
    open var runtimeId = 0
    open var ridingId = 0

    open var spawned = false
        protected set

//    open val type = "entity"

    open fun updatePosition(v3f: Vector3f) {
        posX = v3f.x.toDouble()
        posY = v3f.y.toDouble()
        posZ = v3f.z.toDouble()
    }

    open fun updateRotation(v2f: Vector2f) {
        // keep attention: rotation.y is yaw, rotation.x is pitch
        rotationYaw = v2f.y
        rotationPitch = v2f.x
    }

    open fun updateRotation(v3f: Vector3f) {
        // keep attention: rotation.y is yaw, rotation.x is pitch
        rotationYaw = v3f.y
        rotationPitch = v3f.x
    }

    open fun spawn() {
        spawned = true
    }

    open fun move() {
        client.send(ClientboundTeleportEntityPacket(runtimeId, posX, posY, posZ, rotationYaw, rotationPitch, onGround)) // move entity absolute
    }

    open fun despawn() {
        spawned = false
        client.send(ClientboundRemoveEntitiesPacket(intArrayOf(runtimeId)))
    }
}