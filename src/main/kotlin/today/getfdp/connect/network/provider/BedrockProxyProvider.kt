package today.getfdp.connect.network.provider

import com.github.steveice10.packetlib.packet.Packet
import com.nukkitx.protocol.bedrock.BedrockClient
import com.nukkitx.protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.bedrock.packet.LoginPacket
import io.netty.util.AsciiString
import today.getfdp.connect.FConnect
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.Configuration
import today.getfdp.connect.utils.logWarn
import java.net.DatagramSocket
import java.net.InetSocketAddress


/**
 * this class connects to the bedrock server
 */
class BedrockProxyProvider : PlayProvider() {

    private val targetAddress: InetSocketAddress
        get() = InetSocketAddress(Configuration.get<String>(Configuration.Key.TARGET_HOST), Configuration[Configuration.Key.TARGET_PORT])

    lateinit var bedrockClient: BedrockClient
        private set

    override fun apply(client: Client) {
        super.apply(client)

        // get a usable port
        val socket = DatagramSocket()
        val address = InetSocketAddress("0.0.0.0", socket.localPort)
        socket.close()
        bedrockClient = BedrockClient(address)
        bedrockClient.bind().join() // start RakNet
        try {
            bedrockClient.connect(targetAddress).whenComplete { bedrockSession, throwable ->
                if (throwable != null) {
                    client.disconnect("Exception while connecting to Bedrock server: $throwable")
                    return@whenComplete
                }
                bedrockSession.packetCodec = FConnect.bedrockCodec
                bedrockSession.setBatchHandler { _, _, packets ->
                    packets.forEach(this::bedrockPacketIn)
                }
                bedrockSession.addDisconnectHandler {
                    client.disconnect("Disconnected from Bedrock server")
                }
                bedrockSession.isLogging = false

                // then login
                loginBedrock()
            }.join()
        } catch (t: Throwable) {
            t.printStackTrace()
            client.disconnect("Exception while connecting to Bedrock server: $t")
        }
    }

    override fun remove() {
        if(bedrockClient.session != null && !bedrockClient.session.isClosed) {
            bedrockClient.session.disconnect()
        }
    }

    override fun packetIn(packet: Packet) {
    }

    fun bedrockPacketIn(packet: BedrockPacket) {
    }

    fun bedrockPacketOut(packet: BedrockPacket, immediate: Boolean = false) {
        if (immediate) {
            bedrockClient.session.sendPacketImmediately(packet)
        } else {
            bedrockClient.session.sendPacket(packet)
        }
    }

    /**
     * copy & paste from https://github.com/THEREALWWEFAN231/TunnelMC/blob/0c8c9a6bdfcaedc8ff83db34c746abe203d3df5f/src/main/java/me/THEREALWWEFAN231/tunnelmc/auth/Auth.java
     */
    private fun loginBedrock() {
        val packet = LoginPacket()
        packet.protocolVersion = Configuration[Configuration.Key.BEDROCK_PROTOCOL]
        val chain = client.loginHelper.chain()
        packet.chainData = AsciiString(chain)
        packet.skinData = AsciiString(client.loginHelper.skin())
        bedrockPacketOut(packet, true)

        if(client.name != client.loginHelper.displayName) {
            logWarn("${client.name} is logging in with profile ${client.loginHelper.displayName}(uuid=${client.loginHelper.identity}, xuid=${client.loginHelper.xuid})")
        }
    }
}