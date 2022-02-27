package today.getfdp.connect.play

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket
import com.nukkitx.math.vector.Vector2f
import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode
import com.nukkitx.protocol.bedrock.data.ClientPlayMode
import com.nukkitx.protocol.bedrock.data.InputMode
import com.nukkitx.protocol.bedrock.data.PlayerAuthInputData
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket
import com.nukkitx.protocol.bedrock.packet.PlayerAuthInputPacket
import today.getfdp.connect.utils.game.DimensionUtils

class ThePlayer(private val client: Client) {
    var posX = 0.0
    var posY = 0.0
    var posZ = 0.0
    var onGround = false
    var rotationYaw = 0f
    var rotationPitch = 0f
    var runtimeId = 0
    var ridingId = 0
    var dimension = DimensionUtils.Dimension.OVERWORLD // 默认为主世界
    var shouldMoveRespawn = true // client will send move player packet with respawn flag when connected to server

    private var teleportId = 0
    private var tick = 0L

    var movementMode = AuthoritativeMovementMode.CLIENT

    fun updatePosition(v3f: Vector3f) {
        posX = v3f.x.toDouble()
        posY = v3f.y.toDouble() - EYE_HEIGHT
        posZ = v3f.z.toDouble()
    }

    fun updateRotation(v2f: Vector2f) {
        // keep attention: rotation.y is yaw, rotation.x is pitch
        rotationYaw = v2f.y
        rotationPitch = v2f.x
    }

    fun updateRotation(v3f: Vector3f) {
        // keep attention: rotation.y is yaw, rotation.x is pitch
        rotationYaw = v3f.y
        rotationPitch = v3f.x
    }

    fun teleport(dismountVehicle: Boolean = false) {
        client.send(ClientboundPlayerPositionPacket(posX, posY, posZ, rotationYaw, rotationPitch, teleportId, dismountVehicle))
        teleportId++
    }

    fun move(mode: MovePlayerPacket.Mode = MovePlayerPacket.Mode.NORMAL) {
        if(movementMode == AuthoritativeMovementMode.CLIENT) {
            val packet = MovePlayerPacket()

            packet.runtimeEntityId = runtimeId.toLong()
            packet.position = Vector3f.from(posX, posY + EYE_HEIGHT, posZ)
            packet.rotation = Vector3f.from(rotationPitch, rotationYaw, rotationYaw)
            packet.isOnGround = onGround
            packet.ridingRuntimeEntityId = ridingId.toLong()
            packet.mode = if(shouldMoveRespawn) {
                MovePlayerPacket.Mode.RESPAWN
            } else {
                mode
            }
            packet.tick = tick++

            if(mode == MovePlayerPacket.Mode.TELEPORT) {
                packet.teleportationCause = MovePlayerPacket.TeleportationCause.UNKNOWN
            }

            client.send(packet)
        } else {
            val packet = PlayerAuthInputPacket()

            packet.position = Vector3f.from(posX, posY + EYE_HEIGHT, posZ)
            packet.rotation = Vector3f.from(rotationPitch, rotationYaw, rotationYaw)
            packet.motion = Vector2f.ZERO
            packet.inputData.apply { clear() }.add(PlayerAuthInputData.PERSIST_SNEAK)
            packet.inputMode = InputMode.TOUCH
            packet.playMode = ClientPlayMode.NORMAL
            packet.tick = tick++

            client.send(packet)
        }
    }

    companion object {
        const val EYE_HEIGHT = 1.62
    }
}