package today.getfdp.connect.utils

import today.getfdp.connect.FConnect

fun logInfo(message: Any?) {
    FConnect.logger.info((message ?: "null").toString())
}

fun logWarn(message: Any?) {
    FConnect.logger.warn((message ?: "null").toString())
}

fun logError(message: Any?) {
    FConnect.logger.error((message ?: "null").toString())
}