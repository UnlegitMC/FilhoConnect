package today.getfdp.connect.play

import today.getfdp.connect.play.skin.BedrockSkinModSkinHandler
import today.getfdp.connect.play.skin.ISkinHandler

class ModManager(val client: Client) {

    /**
     * https://github.com/Camotoy/BedrockSkinUtility/
     */
    var bedrockSkinMod = false
        private set
    var skinHandler: ISkinHandler? = BedrockSkinModSkinHandler(client) // default to this, cuz channel registration will send to server when the client is in the game
        private set

//    var formGuiMod = false // todo: code this mod
//        private set

    fun handleRegister(channels: List<String>) {
        if(channels.contains("bedrockskin:data")) {
            bedrockSkinMod = true
        } else {
            skinHandler = null
        }
    }

    fun handlePayload(channel: String, payload: ByteArray) {
    }
}