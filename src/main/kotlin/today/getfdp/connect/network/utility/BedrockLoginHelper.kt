package today.getfdp.connect.network.utility

import coelho.msftauth.api.xbox.*
import com.beust.klaxon.json
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import today.getfdp.connect.FConnect
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.network.HttpUtils
import today.getfdp.connect.utils.network.JWTUtils
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit


class BedrockLoginHelper(val client: Client) {
    val keyPair = EncryptionUtils.createKeyPair()
    lateinit var xuid: String
        private set
    lateinit var identity: UUID
        private set
    lateinit var displayName: String
        private set

    fun offlineChain(): String {
        // So we need to assign the a uuid from a username, or else everytime we join a server with the same name, we will get reset(as if we are a new player)
        // Java does it this way, I'm not sure if bedrock does but it gets our goal accomplished, PlayerEntity.getOfflinePlayerUuid
        this.displayName = client.name
        this.identity = UUID.nameUUIDFromBytes("OfflinePlayer:$displayName".toByteArray())
        this.xuid = identity.leastSignificantBits.toString()
        val chain = json { obj(
            "exp" to (Instant.now().epochSecond + TimeUnit.HOURS.toSeconds(6)).toInt(),
            "nbf" to (Instant.now().epochSecond - TimeUnit.HOURS.toSeconds(6)).toInt(),
            "identityPublicKey" to Base64.getEncoder().encodeToString(keyPair.public.encoded),
            "extraData" to json { obj(
                "titleId" to "2047319603",
                "displayName" to displayName,
                "identity" to identity.toString(),
                "XUID" to xuid
            )}
        )}


        // we need to wrap the chain in this format: {chain: ["chain in jwt format"]}
        return json { obj(
            "chain" to array(JWTUtils.toJWT(chain.toJsonString(), keyPair))
        )}.toJsonString()
    }

    fun onlineChain(): String {
        val mcChain = getMojangOnlineChain()

        // this minecraft chain is login-able, but we should get player name or other thingy from it
        val chains = JWTUtils.parseJsonObj(mcChain).array<String>("chain")!! // the "chain" array must be exists
        chains.forEach { chain ->
            // chain must be a JWT, at least I think...
            val payload = JWTUtils.parseJsonObj(Base64.getDecoder().decode(chain.split(".")[1]).toString(Charsets.UTF_8))
            if(payload.containsKey("extraData")) {
                val extraData = payload.obj("extraData")!!
                this.displayName = extraData.string("displayName")!!
                this.identity = UUID.fromString(extraData.string("identity")!!)
                this.xuid = extraData.string("XUID")!!
            }
        }

        return mcChain
    }

    private fun getMojangOnlineChain(): String {
        val accessToken = client.flags[Client.FLAG_ACCESS_TOKEN] as String
        val key = XboxDeviceKey() // this key used to sign the post content

        val userToken = XboxUserAuthRequest(
            "http://auth.xboxlive.com", "JWT", "RPS",
            "user.auth.xboxlive.com", "t=$accessToken"
        ).request()
        val deviceToken = XboxDeviceAuthRequest(
            "http://auth.xboxlive.com", "JWT", "Nintendo",
            "0.0.0.0", key
        ).request()
        val titleToken: XboxToken = XboxTitleAuthRequest(
            "http://auth.xboxlive.com", "JWT", "RPS",
            "user.auth.xboxlive.com", "t=$accessToken", deviceToken.token, key
        ).request()
        val xstsToken = XboxXSTSAuthRequest(
            "https://multiplayer.minecraft.net/",
            "JWT",
            "RETAIL",
            listOf(userToken),
            titleToken,
            XboxDevice(key, deviceToken)
        ).request()

        // use the xsts token to generate the identity token
        val identityToken = xstsToken.toIdentityToken()

        // then, we can request the chain
        val data = json { obj(
            "identityPublicKey" to Base64.getEncoder().encodeToString(keyPair.public.encoded)
        ) }
        val connection = HttpUtils.make("https://multiplayer.minecraft.net/authentication", "POST", data.toJsonString(),
            mapOf("Content-Type" to "application/json", "Authorization" to identityToken,
                "User-Agent" to "MCPE/UWP", "Client-Version" to FConnect.bedrockCodec.minecraftVersion))

        return connection.inputStream.reader().readText()
    }

    fun chain(): String {
        return if(Configuration[Configuration.Key.ONLINE_MODE]) {
            onlineChain()
        } else {
            offlineChain()
        }
    }

    fun skin(): String {
        val json = json { obj(
            "AnimatedImageData" to array(),
            "ArmSize" to "",
            "CapeData" to "",
            "CapeId" to "",
            "CapeImageHeight" to 0,
            "CapeImageWidth" to 0,
            "CapeOnClassicSkin" to false,
            "ClientRandomId" to Random().nextLong(),
            "CurrentInputMode" to 1,
            "DefaultInputMode" to 1,
            "DeviceId" to UUID.randomUUID().toString(),
            "DeviceModel" to "",
            "DeviceOS" to Configuration.get<Int>(Configuration.Key.DEVICE_OS),
            "GameVersion" to FConnect.bedrockCodec.minecraftVersion,
            "GuiScale" to 0,
            "LanguageCode" to "en_US",
            "PersonaPieces" to array(),
            "PersonaSkin" to false,
            "PieceTintColors" to array(),
            "PlatformOfflineId" to "",
            "PlatformOnlineId" to "",
            "PlayFabId" to "",
            "PremiumSkin" to false,
            "SelfSignedId" to UUID.randomUUID().toString(), // erm? i hope this works?
            "ServerAddress" to "${Configuration.get<String>(Configuration.Key.TARGET_HOST)}:${
                Configuration.get<Int>(
                    Configuration.Key.TARGET_PORT)}",
            "SkinAnimationData" to "",
            "SkinColor" to "#0",
            // todo: skin data from image
            "SkinData" to "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ll9B/5ZfQf+WX0H/lm9b/6p9Zv+Wb1v/lm9b/4dVO/+WX0H/ll9B/5ZfQf+qfWb/lm9b/6p9Zv+Wb1v/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP9WScz/VknM/1ZJzP9WScz/KCgo/ygoKP8oKCj/KCgo/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AKio/wDMzP8AzMz/AKio/1ExJf9qQDD/UTEl/2pAMP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/VknM/1ZJzP9WScz/VknM/ygoKP8oKCj/KCgo/ygoKP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wDMzP8AzMz/AMzM/wDMzP9RMSX/akAw/1ExJf9qQDD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/1ZJzP9WScz/VknM/1ZJzP8oKCj/KCgo/ygoKP8oKCj/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AqKj/AMzM/wDMzP8AzMz/akAw/1ExJf9qQDD/UTEl/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP9WScz/VknM/1ZJzP9WScz/KCgo/ygoKP8oKCj/KCgo/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AKio/wDMzP8AzMz/AKio/2pAMP9RMSX/akAw/1ExJf8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8AAAD/AAAA/wAAAP8wKHL/MChy/yYhW/8wKHL/Rjql/0Y6pf9GOqX/Rjql/zAocv8mIVv/MChy/zAocv86MYn/Rjql/0Y6pf9GOqX/AH9//wB/f/8Af3//AH9//wCoqP8Ar6//AKio/wCenv8Af3//AH9//wB/f/8Af3//AK+v/wCvr/8Ar6//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",
            "SkinGeometryData" to Base64.getEncoder().encodeToString(SKIN_GEOMETRY_DATA.toByteArray(Charsets.UTF_8)),
            "SkinId" to "${UUID.randomUUID()}.Custom", // ok..? :shrug:
            "SkinImageHeight" to 64,
            "SkinImageWidth" to 64,
            "SkinResourcePatch" to Base64.getEncoder().encodeToString(SKIN_RESOURCE_PATCH.toByteArray(Charsets.UTF_8)),
            "ThirdPartyName" to client.name,
            "ThirdPartyNameOnly" to false,
            "UIProfile" to 0,
        )}
        return JWTUtils.toJWT(json.toJsonString(), keyPair)
    }

    companion object {
        const val SKIN_GEOMETRY_DATA = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"bones\":[{\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"name\":\"waist\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-5,8,3],\"size\":[10,16,1],\"uv\":[0,0]}],\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,3],\"rotation\":[0,180,0]}],\"description\":{\"identifier\":\"geometry.cape\",\"texture_height\":32,\"texture_width\":64}},{\"bones\":[{\"name\":\"root\",\"pivot\":[0,0,0]},{\"cubes\":[{\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,16]}],\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"name\":\"waist\",\"parent\":\"root\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[0,0]}],\"name\":\"head\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,3]},{\"cubes\":[{\"inflate\":0.5,\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[32,0]}],\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[4,12,-2],\"size\":[4,12,4],\"uv\":[32,48]}],\"name\":\"leftArm\",\"parent\":\"body\",\"pivot\":[5,22,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[4,12,-2],\"size\":[4,12,4],\"uv\":[48,48]}],\"name\":\"leftSleeve\",\"parent\":\"leftArm\",\"pivot\":[5,22,0]},{\"name\":\"leftItem\",\"parent\":\"leftArm\",\"pivot\":[6,15,1]},{\"cubes\":[{\"origin\":[-8,12,-2],\"size\":[4,12,4],\"uv\":[40,16]}],\"name\":\"rightArm\",\"parent\":\"body\",\"pivot\":[-5,22,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-8,12,-2],\"size\":[4,12,4],\"uv\":[40,32]}],\"name\":\"rightSleeve\",\"parent\":\"rightArm\",\"pivot\":[-5,22,0]},{\"locators\":{\"lead_hold\":[-6,15,1]},\"name\":\"rightItem\",\"parent\":\"rightArm\",\"pivot\":[-6,15,1]},{\"cubes\":[{\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[16,48]}],\"name\":\"leftLeg\",\"parent\":\"root\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[0,48]}],\"name\":\"leftPants\",\"parent\":\"leftLeg\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,16]}],\"name\":\"rightLeg\",\"parent\":\"root\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,32]}],\"name\":\"rightPants\",\"parent\":\"rightLeg\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,32]}],\"name\":\"jacket\",\"parent\":\"body\",\"pivot\":[0,24,0]}],\"description\":{\"identifier\":\"geometry.humanoid.custom\",\"texture_height\":64,\"texture_width\":64,\"visible_bounds_height\":2,\"visible_bounds_offset\":[0,1,0],\"visible_bounds_width\":1}},{\"bones\":[{\"name\":\"root\",\"pivot\":[0,0,0]},{\"name\":\"waist\",\"parent\":\"root\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,16]}],\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[0,0]}],\"name\":\"head\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"cubes\":[{\"inflate\":0.5,\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[32,0]}],\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,16]}],\"name\":\"rightLeg\",\"parent\":\"root\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,32]}],\"name\":\"rightPants\",\"parent\":\"rightLeg\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[16,48]}],\"mirror\":true,\"name\":\"leftLeg\",\"parent\":\"root\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[0,48]}],\"name\":\"leftPants\",\"parent\":\"leftLeg\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"origin\":[4,11.5,-2],\"size\":[3,12,4],\"uv\":[32,48]}],\"name\":\"leftArm\",\"parent\":\"body\",\"pivot\":[5,21.5,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[4,11.5,-2],\"size\":[3,12,4],\"uv\":[48,48]}],\"name\":\"leftSleeve\",\"parent\":\"leftArm\",\"pivot\":[5,21.5,0]},{\"name\":\"leftItem\",\"parent\":\"leftArm\",\"pivot\":[6,14.5,1]},{\"cubes\":[{\"origin\":[-7,11.5,-2],\"size\":[3,12,4],\"uv\":[40,16]}],\"name\":\"rightArm\",\"parent\":\"body\",\"pivot\":[-5,21.5,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-7,11.5,-2],\"size\":[3,12,4],\"uv\":[40,32]}],\"name\":\"rightSleeve\",\"parent\":\"rightArm\",\"pivot\":[-5,21.5,0]},{\"locators\":{\"lead_hold\":[-6,14.5,1]},\"name\":\"rightItem\",\"parent\":\"rightArm\",\"pivot\":[-6,14.5,1]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,32]}],\"name\":\"jacket\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,-3]}],\"description\":{\"identifier\":\"geometry.humanoid.customSlim\",\"texture_height\":64,\"texture_width\":64,\"visible_bounds_height\":2,\"visible_bounds_offset\":[0,1,0],\"visible_bounds_width\":1}}]}";
        const val SKIN_RESOURCE_PATCH = "{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}"

        /**
         * ES256 key pair
         */
        val KEY_PAIR_GEN = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256k1"))
        }
    }
}