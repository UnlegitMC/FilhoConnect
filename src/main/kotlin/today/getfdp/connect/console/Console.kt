package today.getfdp.connect.console

import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.*
import today.getfdp.connect.FConnect

class Console : SimpleTerminalConsole() {

    private val completer = ConsoleCompleter()

    override fun isRunning() = FConnect.running

    override fun runCommand(command: String) {
        CommandManager.executeCommand(command)
    }

    override fun shutdown() {
        FConnect.stop()
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        return super.buildReader(builder
            .appName(FConnect.PROGRAM_NAME)
            .completer(completer))
    }

    class ConsoleCompleter : Completer {
        override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
            candidates.addAll(CommandManager.completeCommand(line.line()).map { Candidate(it) })
        }
    }
}