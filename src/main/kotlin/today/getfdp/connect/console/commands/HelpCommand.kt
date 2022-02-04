package today.getfdp.connect.console.commands

import today.getfdp.connect.console.Command
import today.getfdp.connect.console.CommandManager
import today.getfdp.connect.utils.other.logError
import today.getfdp.connect.utils.other.logInfo
import today.getfdp.connect.utils.other.logWarn
import kotlin.math.ceil

class HelpCommand : Command("help", "List all commands") {
    override fun execute(args: Array<String>) {
        val page = if (args.isNotEmpty()) {
            try {
                args[0].toInt()
            } catch (e: NumberFormatException) {
                logError("Invalid page number: ${args[0]}")
                1
            }
        } else { 1 }

        val commands = CommandManager.commands.map { it.value }
        val maxPage = ceil(commands.size / 8.0).toInt()
        if(page > maxPage) {
            logError("Page number $page is out of range, max page is $maxPage")
        } else if(page <= 0) {
            logError("Page number $page is out of range, min page is 1")
        } else {
            logWarn("Help ($page/$maxPage)")
            commands.let { it.subList(8 * (page - 1), (8 * page).coerceAtMost(it.size)) }
                .forEach {
                    logInfo("> ${it.name} - ${it.description}")
                }
        }
    }
}