package today.getfdp.connect.console.commands

import today.getfdp.connect.FConnect
import today.getfdp.connect.console.Command
import today.getfdp.connect.utils.other.logInfo

class ListCommand : Command("list", "List all connected clients") {
    override fun execute(args: Array<String>) {
        val clientNames = FConnect.server.connectedClients.map { it.value.name }
        logInfo("Connected clients(${clientNames.size}): ${clientNames.joinToString(", ")}")
    }
}