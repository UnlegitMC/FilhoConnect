package today.getfdp.connect.console

abstract class Command(val name: String, val description: String) {

    abstract fun execute(args: Array<String>)

    open fun complete(args: Array<String>): List<String> {
        return emptyList()
    }
}