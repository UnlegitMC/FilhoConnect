package today.getfdp.connect.translate

import today.getfdp.connect.network.provider.BedrockProxyProvider

interface TranslatorBase<T> {

    val intendedClass: Class<T>

    fun translate(provider: BedrockProxyProvider, packet: T)
}