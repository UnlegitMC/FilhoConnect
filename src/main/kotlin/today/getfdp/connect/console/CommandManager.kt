package today.getfdp.connect.console

import today.getfdp.connect.FConnect
import today.getfdp.connect.utils.ClassUtils
import today.getfdp.connect.utils.logError

object CommandManager {
    val commands = mutableMapOf<String, Command>()

    fun registerCommands() {
        ClassUtils.resolvePackage(this.javaClass.`package`.name, Command::class.java).forEach {
            val command = it.newInstance()
            commands[command.name.lowercase()] = command
        }
    }

    fun registerCommand(command: Command) {
        commands[command.name] = command
    }

    fun executeCommand(command: String) {
        val args = command.split(" ")
        val name = args[0]
        val cmd = commands[name]
        if (cmd != null) {
            cmd.execute(args.drop(1).toTypedArray())
        } else {
            logError("Command not found")
        }
    }

    fun completeCommand(command: String): List<String> {
        val args = command.split(" ")
        val name = args[0]
        if(args.size == 1) {
            return commands.keys.filter { it.startsWith(name) }
        }
        val cmd = commands[name]
        if (cmd != null) {
            return cmd.complete(args.drop(1).toTypedArray())
        }
        return emptyList()
    }
}