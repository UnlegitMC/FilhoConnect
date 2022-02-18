package today.getfdp.connect.utils.protocol

import com.nukkitx.protocol.bedrock.BedrockClient
import today.getfdp.connect.utils.other.Configuration
import java.net.DatagramSocket
import java.net.InetSocketAddress

object BedrockConnections {
    val targetAddress: InetSocketAddress
        get() = InetSocketAddress(Configuration.get<String>(Configuration.Key.TARGET_HOST), Configuration[Configuration.Key.TARGET_PORT])

    fun getClient(): BedrockClient {
        // get a usable port
        val socket = DatagramSocket()
        val address = InetSocketAddress("0.0.0.0", socket.localPort)
        socket.close()
        val bedrockClient = BedrockClient(address)
        bedrockClient.bind().join() // start RakNet
        return bedrockClient
    }
}