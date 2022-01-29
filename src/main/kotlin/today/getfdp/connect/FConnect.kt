package today.getfdp.connect

import today.getfdp.connect.network.ServerEventHandler
import today.getfdp.connect.play.Server
import java.util.logging.Logger

object FConnect {

    const val PROGRAM_NAME = "FilhoConnect"
    const val PROGRAM_VERSION = "0.0.1"

    lateinit var server: Server
    val logger = Logger.getLogger(PROGRAM_NAME)

    @JvmStatic
    fun main(args: Array<String>) {
        val time = System.currentTimeMillis()
        logger.info("Starting $PROGRAM_NAME v$PROGRAM_VERSION...")
        start()
        // add stop handler
        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })
        logger.info("Started in ${System.currentTimeMillis() - time}ms")
    }

    fun start() {
        server = Server()
        server.eventHandler = ServerEventHandler()
        server.bind("0.0.0.0", 25565)
    }

    fun stop() {
        server.stop()
    }
}