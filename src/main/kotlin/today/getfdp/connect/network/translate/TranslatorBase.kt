package today.getfdp.connect.network.translate

import today.getfdp.connect.network.provider.BedrockProxyProvider

interface TranslatorBase<T> {

    val intendedClass: Class<T>

    fun translate(provider: BedrockProxyProvider, packet: T)
}