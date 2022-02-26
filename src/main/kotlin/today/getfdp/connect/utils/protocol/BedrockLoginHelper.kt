package today.getfdp.connect.utils.protocol

import coelho.msftauth.api.xbox.*
import com.beust.klaxon.json
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import today.getfdp.connect.FConnect
import today.getfdp.connect.play.AutoLoginManager
import today.getfdp.connect.play.Client
import today.getfdp.connect.resources.SkinHolder
import today.getfdp.connect.utils.network.HttpUtils
import today.getfdp.connect.utils.network.JWTUtils
import today.getfdp.connect.utils.other.Configuration
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO


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
        val accessToken = AutoLoginManager.accessTokens.remove(client.name) ?: kotlin.run {
            client.disconnect("Unable to find access token for ${client.name}")
            throw IllegalStateException("Unable to find access token for ${client.name}")
        }
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
        val random = Random() // used to generate random data
        val json = json { obj(
            "UIProfile" to 1,
            "CapeOnClassicSkin" to false,
            "SkinData" to SkinHolder.skinBase64,
            "SkinImageWidth" to SkinHolder.skinWidth,
            "SkinImageHeight" to SkinHolder.skinHeight,
            "SkinColor" to "#0",
            "SkinResourcePatch" to Base64.getEncoder().encodeToString(SKIN_RESOURCE_PATCH.toByteArray(Charsets.UTF_8)),
            "SkinGeometryData" to Base64.getEncoder().encodeToString(SKIN_GEOMETRY_DATA.toByteArray(Charsets.UTF_8)),
            "CapeData" to "",
            "CapeId" to "",
            "ThirdPartyNameOnly" to false,
            "DeviceId" to UUID.randomUUID().toString().replace("-", ""),
            "ClientRandomId" to random.nextLong(),
            "ServerAddress" to "${Configuration.get<String>(Configuration.Key.TARGET_HOST)}:${Configuration.get<Int>(Configuration.Key.TARGET_PORT)}",
            "PlatformOnlineId" to "",
            "SkinAnimationData" to "",
            "GameVersion" to FConnect.bedrockCodec.minecraftVersion,
            "LanguageCode" to Configuration.get<String>(Configuration.Key.LANGUAGE_CODE),
            "CurrentInputMode" to 2,
            "SkinGeometryDataEngineVersion" to "MC4wLjA=", // 0.0.0
            "DefaultInputMode" to 2,
            "PremiumSkin" to false,
            "DeviceModel" to ByteArray(random.nextInt(10) + 3).let { random.nextBytes(it); Base64.getEncoder().encodeToString(it) },
            "SelfSignedId" to UUID.randomUUID(),
            "ThirdPartyName" to client.name,
            "DeviceOS" to Configuration.get<Int>(Configuration.Key.DEVICE_OS),
            "PlayFabId" to "",
            "SkinId" to "${client.uuid}.Custom",
            "PersonaSkin" to false,
            "PersonaPieces" to array(),
            "PieceTintColors" to array(),
            "GuiScale" to 0, // this really is 0 in vanilla
            "ArmSize" to "",
            "CapeImageHeight" to 0,
            "PlatformOfflineId" to "",
            "AnimatedImageData" to array(),
            "CapeImageWidth" to 0
        )}
        return JWTUtils.toJWT(json.toJsonString(), keyPair)
    }

    companion object {
        const val SKIN_GEOMETRY_DATA = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"bones\":[{\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"name\":\"waist\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-5,8,3],\"size\":[10,16,1],\"uv\":[0,0]}],\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,3],\"rotation\":[0,180,0]}],\"description\":{\"identifier\":\"geometry.cape\",\"texture_height\":32,\"texture_width\":64}},{\"bones\":[{\"name\":\"root\",\"pivot\":[0,0,0]},{\"cubes\":[{\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,16]}],\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"name\":\"waist\",\"parent\":\"root\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[0,0]}],\"name\":\"head\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,3]},{\"cubes\":[{\"inflate\":0.5,\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[32,0]}],\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[4,12,-2],\"size\":[4,12,4],\"uv\":[32,48]}],\"name\":\"leftArm\",\"parent\":\"body\",\"pivot\":[5,22,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[4,12,-2],\"size\":[4,12,4],\"uv\":[48,48]}],\"name\":\"leftSleeve\",\"parent\":\"leftArm\",\"pivot\":[5,22,0]},{\"name\":\"leftItem\",\"parent\":\"leftArm\",\"pivot\":[6,15,1]},{\"cubes\":[{\"origin\":[-8,12,-2],\"size\":[4,12,4],\"uv\":[40,16]}],\"name\":\"rightArm\",\"parent\":\"body\",\"pivot\":[-5,22,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-8,12,-2],\"size\":[4,12,4],\"uv\":[40,32]}],\"name\":\"rightSleeve\",\"parent\":\"rightArm\",\"pivot\":[-5,22,0]},{\"locators\":{\"lead_hold\":[-6,15,1]},\"name\":\"rightItem\",\"parent\":\"rightArm\",\"pivot\":[-6,15,1]},{\"cubes\":[{\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[16,48]}],\"name\":\"leftLeg\",\"parent\":\"root\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[0,48]}],\"name\":\"leftPants\",\"parent\":\"leftLeg\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,16]}],\"name\":\"rightLeg\",\"parent\":\"root\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,32]}],\"name\":\"rightPants\",\"parent\":\"rightLeg\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,32]}],\"name\":\"jacket\",\"parent\":\"body\",\"pivot\":[0,24,0]}],\"description\":{\"identifier\":\"geometry.humanoid.custom\",\"texture_height\":64,\"texture_width\":64,\"visible_bounds_height\":2,\"visible_bounds_offset\":[0,1,0],\"visible_bounds_width\":1}},{\"bones\":[{\"name\":\"root\",\"pivot\":[0,0,0]},{\"name\":\"waist\",\"parent\":\"root\",\"pivot\":[0,12,0]},{\"cubes\":[{\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,16]}],\"name\":\"body\",\"parent\":\"waist\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[0,0]}],\"name\":\"head\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"cubes\":[{\"inflate\":0.5,\"origin\":[-4,24,-4],\"size\":[8,8,8],\"uv\":[32,0]}],\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0]},{\"cubes\":[{\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,16]}],\"name\":\"rightLeg\",\"parent\":\"root\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-3.9,0,-2],\"size\":[4,12,4],\"uv\":[0,32]}],\"name\":\"rightPants\",\"parent\":\"rightLeg\",\"pivot\":[-1.9,12,0]},{\"cubes\":[{\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[16,48]}],\"mirror\":true,\"name\":\"leftLeg\",\"parent\":\"root\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-0.1,0,-2],\"size\":[4,12,4],\"uv\":[0,48]}],\"name\":\"leftPants\",\"parent\":\"leftLeg\",\"pivot\":[1.9,12,0]},{\"cubes\":[{\"origin\":[4,11.5,-2],\"size\":[3,12,4],\"uv\":[32,48]}],\"name\":\"leftArm\",\"parent\":\"body\",\"pivot\":[5,21.5,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[4,11.5,-2],\"size\":[3,12,4],\"uv\":[48,48]}],\"name\":\"leftSleeve\",\"parent\":\"leftArm\",\"pivot\":[5,21.5,0]},{\"name\":\"leftItem\",\"parent\":\"leftArm\",\"pivot\":[6,14.5,1]},{\"cubes\":[{\"origin\":[-7,11.5,-2],\"size\":[3,12,4],\"uv\":[40,16]}],\"name\":\"rightArm\",\"parent\":\"body\",\"pivot\":[-5,21.5,0]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-7,11.5,-2],\"size\":[3,12,4],\"uv\":[40,32]}],\"name\":\"rightSleeve\",\"parent\":\"rightArm\",\"pivot\":[-5,21.5,0]},{\"locators\":{\"lead_hold\":[-6,14.5,1]},\"name\":\"rightItem\",\"parent\":\"rightArm\",\"pivot\":[-6,14.5,1]},{\"cubes\":[{\"inflate\":0.25,\"origin\":[-4,12,-2],\"size\":[8,12,4],\"uv\":[16,32]}],\"name\":\"jacket\",\"parent\":\"body\",\"pivot\":[0,24,0]},{\"name\":\"cape\",\"parent\":\"body\",\"pivot\":[0,24,-3]}],\"description\":{\"identifier\":\"geometry.humanoid.customSlim\",\"texture_height\":64,\"texture_width\":64,\"visible_bounds_height\":2,\"visible_bounds_offset\":[0,1,0],\"visible_bounds_width\":1}}]}"
        const val SKIN_RESOURCE_PATCH = "{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}"

        fun skinFromFile(file: File): String {
            return skinFromImage(ImageIO.read(file))
        }

        fun skinFromImage(image: BufferedImage): String {
            val bos = ByteArrayOutputStream()
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val array = ByteBuffer.allocate(4).putInt(image.getRGB(x, y)).array()
                    val bytes = ByteArray(4)
                    bytes[0] = array[1]
                    bytes[1] = array[2]
                    bytes[2] = array[3]
                    bytes[3] = array[0] // bedrock stores the byte in a special order
                    bos.write(bytes)
                }
            }
            bos.close()
            return Base64.getEncoder().encodeToString(bos.toByteArray())
        }
    }
}