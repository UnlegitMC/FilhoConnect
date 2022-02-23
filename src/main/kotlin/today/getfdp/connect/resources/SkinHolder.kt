package today.getfdp.connect.resources

import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.protocol.BedrockLoginHelper
import java.io.File
import javax.imageio.ImageIO

/**
 * use a holder to make the user can use skins from url
 */
object SkinHolder : ResourceHolder() {
    override val resourceName: String
        get() = "Skin"
    override val resourcePath: String
        get() = Configuration[Configuration.Key.SKIN_PATH]

    lateinit var skin: File
        private set

    var skinBase64 = ""
        private set
    var skinWidth = 0
        private set
    var skinHeight = 0
        private set

    override fun init(file: File) {
        this.skin = file

        // pre-read the skin to make login faster
        val image = ImageIO.read(file)
        skinWidth = image.width
        skinHeight = image.height
        skinBase64 = BedrockLoginHelper.skinFromImage(image)
    }
}