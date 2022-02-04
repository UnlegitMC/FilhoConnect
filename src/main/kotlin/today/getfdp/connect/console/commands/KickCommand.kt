package today.getfdp.connect.console.commands

import today.getfdp.connect.FConnect
import today.getfdp.connect.console.Command
import today.getfdp.connect.play.Client
import today.getfdp.connect.utils.other.logWarn

class KickCommand : Command("kick", "Kick a client from the server") {
    override fun execute(args: Array<String>) {
        if(args.isEmpty()) {
            logWarn("Usage: kick <client id> [reason] / kick ip <client ip> [reason]")
            return
        } else if (args[0].lowercase() == "ip") {
            if(args.size < 2) {
                logWarn("Usage: kick ip <client ip> [reason]")
                return
            }
            val reason = if(args.size > 2) args.slice(2 until args.size).joinToString(" ") else "No reason"
            kickPlayers(FConnect.server.connectedClients.filter { it.value.session.host == args[1] }.map { it.value }, reason)
        } else {
            val reason = if(args.size > 1) args.slice(1 until args.size).joinToString(" ") else "No reason"
            kickPlayers(FConnect.server.connectedClients.filter { it.value.name == args[0] }.map { it.value }, reason)
        }
    }

    private fun kickPlayers(list: List<Client>, reason: String) {
        list.forEach {
            it.disconnect(reason)
            logWarn("Kicked ${it.name} for $reason")
        }
    }
}