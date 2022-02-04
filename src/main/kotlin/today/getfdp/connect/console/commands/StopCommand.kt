package today.getfdp.connect.console.commands

import today.getfdp.connect.FConnect
import today.getfdp.connect.console.Command
import today.getfdp.connect.utils.other.logWarn

class StopCommand : Command("stop", "Stop the server") {
    override fun execute(args: Array<String>) {
        logWarn("Stopping...")
        FConnect.stop()
    }
}