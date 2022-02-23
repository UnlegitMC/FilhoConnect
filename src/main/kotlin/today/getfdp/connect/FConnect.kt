package today.getfdp.connect

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.Parser
import com.nukkitx.protocol.bedrock.BedrockPacketCodec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.plugins.util.PluginManager
import today.getfdp.connect.console.CommandManager
import today.getfdp.connect.console.Console
import today.getfdp.connect.network.ServerEventHandler
import today.getfdp.connect.play.AutoLoginManager
import today.getfdp.connect.play.Server
import today.getfdp.connect.resources.ResourceHolder
import today.getfdp.connect.translate.TranslateManager
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logError
import java.lang.reflect.Modifier

object FConnect {

    const val PROGRAM_NAME = "FilhoConnect"
    const val PROGRAM_VERSION = "0.0.1"

    var running = false

    lateinit var server: Server
    val logger =
        PluginManager.addPackage("net.minecrell.terminalconsole") // define terminal as log4j2 plugin first
            .let { LogManager.getLogger(PROGRAM_NAME) }

    lateinit var bedrockCodec: BedrockPacketCodec
        private set

    val klaxon = Klaxon()
    val klaxonJson = KlaxonJson()
    val parser = Parser.default()

    private val consoleThread = Thread {
        Console().start()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val time = System.currentTimeMillis()
        logger.info("Starting $PROGRAM_NAME v$PROGRAM_VERSION...")

        // start server
        start()

        // start console
        startConsole()

        logger.info("Started in ${System.currentTimeMillis() - time}ms")
    }

    fun startConsole() {
        CommandManager.registerCommands()
        consoleThread.start()
    }

    fun start() {
        running = true

        Configuration.load()

        if(Configuration[Configuration.Key.XBOX_AUTOLOGIN]) {
            AutoLoginManager.load()
        }

        ResourceHolder.loadResources()

        TranslateManager.initialize()

        syncBedrockCodecFromConfig()
        logger.info("Loaded bedrock codec for Minecraft ${bedrockCodec.minecraftVersion}(Protocol Version ${bedrockCodec.protocolVersion})")

        server = Server()
        server.eventHandler = ServerEventHandler()
        server.bind(Configuration[Configuration.Key.SERVER_HOST], Configuration[Configuration.Key.SERVER_PORT])
    }

    fun stop() {
        running = false
        server.stop()
        // stop console
        if(consoleThread.isAlive) {
            consoleThread.interrupt()
        }
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
            logError("Unsupported bedrock codec version: $codecVersion")
            throw t
        }
    }
}