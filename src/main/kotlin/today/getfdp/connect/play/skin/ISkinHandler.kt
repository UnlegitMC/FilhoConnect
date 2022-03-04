package today.getfdp.connect.play.skin

import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin
import java.util.UUID

interface ISkinHandler {

    fun handle(uuid: UUID, skin: SerializedSkin)
}