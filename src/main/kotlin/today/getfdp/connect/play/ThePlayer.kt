package today.getfdp.connect.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket
import com.nukkitx.math.vector.Vector2f
import com.nukkitx.math.vector.Vector3f

class ThePlayer(private val client: Client) {
    var posX = 0.0
    var posY = 0.0
    var posZ = 0.0
    var rotationYaw = 0f
    var rotationPitch = 0f

    private var teleportId = 0

    fun updatePosition(v3f: Vector3f) {
        posX = v3f.x.toDouble()
        posY = v3f.y.toDouble()
        posZ = v3f.z.toDouble()
    }

    fun updateRotation(v2f: Vector2f) {
        // keep attention: rotation.y is yaw, rotation.x is pitch
        rotationYaw = v2f.y
        rotationPitch = v2f.x
    }

    fun updatePositionRotation(v3f: Vector3f, v2f: Vector2f) {
        updatePosition(v3f)
        updateRotation(v2f)
    }

    fun teleport(dismountVehicle: Boolean = false) {
        client.send(ClientboundPlayerPositionPacket(posX, posY, posZ, rotationYaw, rotationPitch, teleportId, dismountVehicle))
        teleportId++
    }
}