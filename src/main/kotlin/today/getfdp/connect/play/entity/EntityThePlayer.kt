package today.getfdp.connect.play.entity

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket
import com.nukkitx.math.vector.Vector2f
import com.nukkitx.math.vector.Vector3f
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode
import com.nukkitx.protocol.bedrock.data.ClientPlayMode
import com.nukkitx.protocol.bedrock.data.InputMode
import com.nukkitx.protocol.bedrock.data.PlayerAuthInputData
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket
import com.nukkitx.protocol.bedrock.packet.PlayerAuthInputPacket
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.game.DimensionUtils
import today.getfdp.connect.utils.other.Configuration

class EntityThePlayer(client: Client) : EntityHuman(client) {

    var dimension = DimensionUtils.Dimension.OVERWORLD // 默认为主世界
    var shouldMoveRespawn = true // client will send move player packet with respawn flag when connected to server

    private var teleportId = 0
    private var tick = 0L

    private var preX = 0.0
    private var preY = 0.0
    private var preZ = 0.0

    var movementMode = AuthoritativeMovementMode.CLIENT
    var blockPlaceAuth = false
    val authInputMode = InputMode.valueOf(Configuration[Configuration.Key.AUTH_INPUT_MODE])

    init {
        spawned = true
    }

    override fun spawn() { }

    override fun despawn() { }

    fun teleport(dismountVehicle: Boolean = false) {
        client.send(ClientboundPlayerPositionPacket(posX, posY, posZ, rotationYaw, rotationPitch, teleportId, dismountVehicle))
        teleportId++
    }

    override fun move() {
        this.move(MovePlayerPacket.Mode.NORMAL)
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
            packet.inputMode = authInputMode
            packet.playMode = ClientPlayMode.NORMAL
            packet.tick = tick++
            packet.delta = Vector3f.from(posX - preX, posY - preY, posZ - preZ)

            client.send(packet)
        }

        preX = posX
        preY = posY
        preZ = posZ

        client.theWorld.update() // update world things when player tick
    }
}