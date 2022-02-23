package today.getfdp.connect.translate

import today.getfdp.connect.network.provider.BedrockProxyProvider

interface TranslatorBase<T> {

    /**
     * @return does this translator process in async if async mode is enabled
     */
    fun async(): Boolean {
        return false
    }

    val intendedClass: Class<T>

    fun translate(provider: BedrockProxyProvider, packet: T)
}