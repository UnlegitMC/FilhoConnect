package today.getfdp.connect.resources

import today.getfdp.connect.utils.network.HttpUtils
import today.getfdp.connect.utils.network.JWTUtils
import today.getfdp.connect.utils.other.logWarn
import java.io.File

abstract class ResourceHolder {

    abstract val resourceName: String

    abstract val resourcePath: String

    protected abstract fun init(file: File)

    open var isLoaded = false
        protected set

    open fun loadResource() {
        if(isLoaded) {
            return
        }

        val file = if(resourcePath.startsWith("http")) { // online resource
            val tempFile = File(".tmp/${resourceName}", JWTUtils.md5(resourcePath))
            if(tempFile.exists()) {
                tempFile
            } else {
                // download resource
                logWarn("Downloading resource $resourceName from $resourcePath")
                val conn = HttpUtils.make(resourcePath, "GET")
                tempFile.parentFile.mkdirs()
                tempFile.writeBytes(conn.inputStream.readBytes())

                tempFile
            }
        } else {
            File(resourcePath)
        }

        init(file) // initialize resource
        isLoaded = true
    }
}