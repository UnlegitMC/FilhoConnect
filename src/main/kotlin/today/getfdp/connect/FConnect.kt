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
import today.getfdp.connect.resources.BedrockBlockPaletteHolder
import today.getfdp.connect.resources.BlockMappingHolder
import today.getfdp.connect.resources.SkinHolder
import today.getfdp.connect.translate.TranslateManager
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logError
import java.lang.reflect.Modifier
import java.text.DecimalFormat

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

        val decimalFormat = DecimalFormat("#.##")
        logger.info("Successfully started in ${decimalFormat.format((System.currentTimeMillis() - time) / 1000f)}s!")
    }

    fun startConsole() {
        CommandManager.registerCommands()
        consoleThread.start()
    }

    fun start() {
        running = true

        Configuration.load()

        loadResources()

        if(Configuration[Configuration.Key.XBOX_AUTOLOGIN]) {
            AutoLoginManager.load()
        }

        TranslateManager.initialize()

        syncBedrockCodecFromConfig()
        logger.info("Loaded bedrock codec for Minecraft ${bedrockCodec.minecraftVersion}(Protocol Version ${bedrockCodec.protocolVersion})")

        server = Server()
        server.eventHandler = ServerEventHandler()
        server.bind(Configuration[Configuration.Key.SERVER_HOST], Configuration[Configuration.Key.SERVER_PORT])
    }

    fun loadResources() {
        val tasks = listOf(
            SkinHolder::loadResource,
            BedrockBlockPaletteHolder::loadResource,
            BlockMappingHolder::loadResource
        )
        tasks.forEach {
            it.invoke()
        }
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