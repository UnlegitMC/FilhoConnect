package today.getfdp.connect.play

import com.github.steveice10.mc.auth.service.SessionService
import com.github.steveice10.mc.protocol.MinecraftConstants
import com.github.steveice10.mc.protocol.MinecraftProtocol
import com.github.steveice10.mc.protocol.ServerLoginHandler
import com.github.steveice10.mc.protocol.codec.MinecraftCodec
import com.github.steveice10.mc.protocol.data.status.PlayerInfo
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo
import com.github.steveice10.mc.protocol.data.status.VersionInfo
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder
import com.github.steveice10.packetlib.Server
import com.github.steveice10.packetlib.Session
import com.github.steveice10.packetlib.tcp.TcpServer
import net.kyori.adventure.text.Component
import today.getfdp.connect.network.ServerEventHandler


class Server {

    lateinit var eventHandler: ServerEventHandler

    val connectedClients = mutableMapOf<Session, Client>()
    lateinit var server: Server

    fun bind(host: String, port: Int) {
        if(this::server.isInitialized && server.isListening) {
            server.close()
        }
        server = TcpServer(host, port) { MinecraftProtocol() }
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, SessionService())
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, false)
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100)
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, ServerInfoBuilder {
            val motd = eventHandler.getMotd()
            ServerStatusInfo(VersionInfo(MinecraftCodec.CODEC.minecraftVersion, MinecraftCodec.CODEC.protocolVersion),
                PlayerInfo(motd.nowPlayers, motd.maxPlayers, emptyArray()), Component.text(motd.message), motd.icon)
        })
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, ServerLoginHandler { session: Session ->
            eventHandler.onConnected(createClient(session))
        })

        server.bind()
    }

    fun stop() {
        if(!this::server.isInitialized) {
            throw IllegalStateException("Server is not initialized")
        }
        server.close()
    }

    private fun createClient(session: Session): Client {
        val client = Client(session)
        connectedClients[session] = client
        return client
    }
}