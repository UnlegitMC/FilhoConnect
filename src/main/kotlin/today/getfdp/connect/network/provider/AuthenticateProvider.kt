package today.getfdp.connect.network.provider

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.title.ClientboundSetTitlesAnimationPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket
import com.github.steveice10.opennbt.tag.builtin.*
import com.github.steveice10.packetlib.packet.Packet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import today.getfdp.connect.FConnect
import today.getfdp.connect.network.utility.PayloadEncoder
import today.getfdp.connect.play.AutoLoginManager
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.network.HttpUtils
import java.io.IOException
import kotlin.concurrent.thread

class AuthenticateProvider : PlayProvider() {
    private val posPacket = ClientboundPlayerPositionPacket(0.0, 100.0, 0.0, 0f, 0f, 0, false)
    private var inLoginProcess = false

    override fun apply(client: Client) {
        super.apply(client)
        if(!client.isLogin) {
            packetOut(ClientboundLoginPacket(0, false, GameMode.SURVIVAL, GameMode.SURVIVAL, 1, arrayOf("minecraft:world"), getDimensionTag(), getOverworldTag(), "minecraft:world", 100, 0, 16, 16, false, false, false, false))
            PayloadEncoder.sendBrand(client.session)
        }
        // no respawn packet cause dimension don't affect the extra login process
        // spawn the player
        packetOut(posPacket)
        // send title
        packetOut(ClientboundSetTitlesAnimationPacket(0, 114514, 0))
        client.title("§eLogin is required!")
        client.subtitle("§7${FConnect.PROGRAM_NAME} ver${FConnect.PROGRAM_VERSION}")
        // send tip message
        client.chat(Component.text("Open ").append(Component.text("§nthis§r")
                .style(Style.empty().clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://login.live.com/oauth20_authorize.srf?client_id=00000000441cc96b&redirect_uri=https://login.live.com/oauth20_desktop.srf&response_type=code&scope=service::user.auth.xboxlive.com::MBI_SSL"))))
            .append(Component.text(" link to get your token!")))
        client.chat("And then §acopy the link which redirected§f or §cpaste the M.R3_BAY.token§f in chat!")
        if(Configuration[Configuration.Key.XBOX_AUTOLOGIN] && AutoLoginManager[client.name] != null) {
            client.chat("§a§lAuto login is supported! Swing your arm to auto login!")
        }
    }

    override fun remove() {
        // reset title animation
        packetOut(ClientboundSetTitlesAnimationPacket(10, 70, 20))
        // reset title
        client.title("")
        client.subtitle("")
    }

    override fun packetIn(packet: Packet) {
        if(packet is ServerboundMovePlayerPosPacket || packet is ServerboundMovePlayerRotPacket || packet is ServerboundMovePlayerPosRotPacket) {
            packetOut(posPacket)
        } else if (packet is ServerboundChatPacket) {
            val msg = packet.message
            if (inLoginProcess) {
                client.chat("§c§lPlease wait...")
            } else if (msg.startsWith("M.R3")) { // M.R3_BAY token
                clientLogin(msg)
            } else if (msg.startsWith("https://login.live.com/oauth20_desktop.srf")) { // link redirected
                clientLogin(msg.split("code=")[1].let {
                    if(it.contains("&")) it.split("&")[0] else it
                })
            } else {
                client.chat("§c§lIllegal input! Input should like this format: §r§6https://login.live.com/oauth20_desktop.srf?code=M.R3_BL2.00000000-0000-0000-0000-000000000000&lc=1033")
            }
        } else if (packet is ServerboundSwingPacket) {
            if(Configuration[Configuration.Key.XBOX_AUTOLOGIN] && AutoLoginManager[client.name] != null) {
                client.chat("§aTrying autologin...")
                thread {
                    inLoginProcess = true
                    submitToken(AutoLoginManager[client.name]!!)
                    try {
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        client.chat("§c§lAutologin failed! Please do normal login!")
                        AutoLoginManager[client.name] = null
                    } finally {
                        inLoginProcess = false
                    }
                }
            }
        }
    }

    private fun clientLogin(token: String) {
        client.chat("§aVerifying token...")
        // bedrock requires access token, not m.r3_bay token, so we need to get the refresh token and transform it to access token
        thread {
            inLoginProcess = true
            try {
                val conn = HttpUtils.make("https://login.live.com/oauth20_token.srf", "POST",
                    "client_id=00000000441cc96b&redirect_uri=https://login.live.com/oauth20_desktop.srf&grant_type=authorization_code&code=$token",
                    mapOf("Content-Type" to "application/x-www-form-urlencoded"))
                val json = FConnect.klaxon.parseJsonObject(try {
                    conn.inputStream.reader(Charsets.UTF_8)
                } catch (t: IOException) {
                    conn.errorStream.reader(Charsets.UTF_8)
                })

                if(json.containsKey("refresh_token")) {
                    submitToken(json.string("refresh_token")!!)
                } else if(json.containsKey("error")) {
                    client.disconnect("Something wrong with your token! (error=${json["error"]}, description=${json["error_description"]})")
                } else {
                    throw Exception("Unable to read json")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                client.disconnect("§c§lException thrown when verifying token: $t")
            } finally {
                inLoginProcess = false
            }
        }
    }

    /**
     * get the usable access token from the refresh token
     */
    private fun submitToken(refreshToken: String) {
        val json = FConnect.klaxon.parseJsonObject(
            HttpUtils.make("https://login.live.com/oauth20_token.srf", "POST",
            "client_id=00000000441cc96b&scope=service::user.auth.xboxlive.com::MBI_SSL&grant_type=refresh_token&redirect_uri=https://login.live.com/oauth20_desktop.srf&refresh_token=$refreshToken",
            mapOf("Content-Type" to "application/x-www-form-urlencoded")).inputStream.reader(Charsets.UTF_8))
        if(json.containsKey("access_token")) {
            client.flags[Client.FLAG_ACCESS_TOKEN] = json.string("access_token")!!
        } else if(json.containsKey("error")) {
            client.disconnect("Something wrong while refreshing token! (error=${json["error"]}, description=${json["error_description"]})")
            return
        } else {
            throw Exception("Unable to find access token")
        }
        if(Configuration[Configuration.Key.XBOX_AUTOLOGIN]) {
            AutoLoginManager[client.name] = json.string("refresh_token")!!
        }
        client.provider = BedrockProxyProvider() // auth success, switch to bedrock proxy
    }

    private fun getDimensionTag(): CompoundTag {
        val tag = CompoundTag("")
        val dimensionTypes = CompoundTag("minecraft:dimension_type")
        dimensionTypes.put(StringTag("type", "minecraft:dimension_type"))
        val dimensionTag = ListTag("value")
        val overworldTag = convertToValue("minecraft:overworld", 0, getOverworldTag().value)
        dimensionTag.add(overworldTag)
        dimensionTypes.put(dimensionTag)
        tag.put(dimensionTypes)
        val biomeTypes = CompoundTag("minecraft:worldgen/biome")
        biomeTypes.put(StringTag("type", "minecraft:worldgen/biome"))
        val biomeTag = ListTag("value")
        val plainsTag = convertToValue("minecraft:plains", 0, getPlainsTag().value)
        biomeTag.add(plainsTag)
        biomeTypes.put(biomeTag)
        tag.put(biomeTypes)
        return tag
    }

    private fun getOverworldTag(): CompoundTag {
        val overworldTag = CompoundTag("")
        overworldTag.put(StringTag("name", "minecraft:overworld"))
        overworldTag.put(ByteTag("piglin_safe", 0.toByte()))
        overworldTag.put(ByteTag("natural", 1.toByte()))
        overworldTag.put(FloatTag("ambient_light", 0f))
        overworldTag.put(StringTag("infiniburn", "minecraft:infiniburn_overworld"))
        overworldTag.put(ByteTag("respawn_anchor_works", 0.toByte()))
        overworldTag.put(ByteTag("has_skylight", 1.toByte()))
        overworldTag.put(ByteTag("bed_works", 1.toByte()))
        overworldTag.put(StringTag("effects", "minecraft:overworld"))
        overworldTag.put(ByteTag("has_raids", 1.toByte()))
        overworldTag.put(IntTag("logical_height", 256))
        overworldTag.put(FloatTag("coordinate_scale", 1f))
        overworldTag.put(ByteTag("ultrawarm", 0.toByte()))
        overworldTag.put(ByteTag("has_ceiling", 0.toByte()))
        overworldTag.put(IntTag("height", 256))
        overworldTag.put(IntTag("min_y", 0))

        return overworldTag
    }

    private fun getPlainsTag(): CompoundTag {
        val plainsTag = CompoundTag("")
        plainsTag.put(StringTag("name", "minecraft:plains"))
        plainsTag.put(StringTag("precipitation", "rain"))
        plainsTag.put(FloatTag("depth", 0.125f))
        plainsTag.put(FloatTag("temperature", 0.8f))
        plainsTag.put(FloatTag("scale", 0.05f))
        plainsTag.put(FloatTag("downfall", 0.4f))
        plainsTag.put(StringTag("category", "plains"))
        val effects = CompoundTag("effects")
        effects.put(LongTag("sky_color", 7907327))
        effects.put(LongTag("water_fog_color", 329011))
        effects.put(LongTag("fog_color", 12638463))
        effects.put(LongTag("water_color", 4159204))
        val moodSound = CompoundTag("mood_sound")
        moodSound.put(IntTag("tick_delay", 6000))
        moodSound.put(FloatTag("offset", 2.0f))
        moodSound.put(StringTag("sound", "minecraft:ambient.cave"))
        moodSound.put(IntTag("block_search_extent", 8))
        effects.put(moodSound)
        plainsTag.put(effects)
        return plainsTag
    }

    private fun convertToValue(name: String, id: Int, values: Map<String, Tag>): CompoundTag {
        val tag = CompoundTag(name)
        tag.put(StringTag("name", name))
        tag.put(IntTag("id", id))
        val element = CompoundTag("element")
        element.value = values
        tag.put(element)
        return tag
    }
}