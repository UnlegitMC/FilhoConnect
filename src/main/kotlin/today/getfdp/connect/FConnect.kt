package today.getfdp.connect

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.Parser
import com.nukkitx.protocol.bedrock.BedrockPacketCodec
import today.getfdp.connect.network.ServerEventHandler
import today.getfdp.connect.play.AutoLoginManager
import today.getfdp.connect.play.Server
import today.getfdp.connect.utils.Configuration
import java.lang.reflect.Modifier
import java.util.logging.Logger

object FConnect {

    const val PROGRAM_NAME = "FilhoConnect"
    const val PROGRAM_VERSION = "0.0.1"

    lateinit var server: Server
    val logger = Logger.getLogger(PROGRAM_NAME)

    lateinit var bedrockCodec: BedrockPacketCodec
        private set

    val klaxon = Klaxon()
    val klaxonJson = KlaxonJson()
    val parser = Parser.default()

    @JvmStatic
    fun main(args: Array<String>) {
        val time = System.currentTimeMillis()
        logger.info("Starting $PROGRAM_NAME v$PROGRAM_VERSION...")

        // start server
        start()
        // add stop handler
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })

        logger.info("Started in ${System.currentTimeMillis() - time}ms")
    }

    fun start() {
        Configuration.load()

        if(Configuration[Configuration.Key.XBOX_AUTOLOGIN]) {
            AutoLoginManager.load()
        }

        syncBedrockCodecFromConfig()
        logger.info("Loaded bedrock codec for Minecraft ${bedrockCodec.minecraftVersion}(Protocol Version ${bedrockCodec.protocolVersion})")

        server = Server()
        server.eventHandler = ServerEventHandler()
        server.bind(Configuration[Configuration.Key.SERVER_HOST], Configuration[Configuration.Key.SERVER_PORT])
    }

    fun stop() {
        server.stop()
        Configuration.save()
    }

    private fun syncBedrockCodecFromConfig() {
        val codecVersion = Configuration.get<String>(Configuration.Key.BEDROCK_CODEC)
        try {
            val klass = Class.forName("com.nukkitx.protocol.bedrock.$codecVersion.Bedrock_$codecVersion")
            klass.fields.forEach {
                if(Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers)) {
                    val value = it.get(null)
                    if(value is BedrockPacketCodec) {
                        bedrockCodec = value
                        return
                    }
                }
            }
        } catch (t: ClassNotFoundException) {
            logger.severe("Unsupported bedrock codec version: $codecVersion")
            throw t
        }
    }

    // logger functions

    fun logInfo(message: Any?) {
        logger.info((message ?: "null").toString())
    }

    fun logWarn(message: Any?) {
        logger.warning((message ?: "null").toString())
    }

    fun logError(message: Any?) {
        logger.severe((message ?: "null").toString())
    }
}