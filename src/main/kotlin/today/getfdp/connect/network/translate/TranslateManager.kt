package today.getfdp.connect.network.translate

import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.utils.other.ClassUtils
import java.lang.reflect.Method

object TranslateManager {

    val translators = mutableListOf<TranslatorBase<*>>()
    val translateMethod: Method

    init {
        TranslatorBase::class.java.declaredMethods.find { it.name == "translate" }!!.let {
            translateMethod = it
        }
    }

    fun initialize() {
        translators.clear()

        ClassUtils.resolvePackage(this.javaClass.`package`.name, TranslatorBase::class.java).forEach {
            try {
                translators.add(it.newInstance())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun handle(provider: BedrockProxyProvider, packet: Any) {
        translators.filter { it.intendedClass.isInstance(packet) }.forEach {
            translateMethod.invoke(it, provider, packet)
        }
    }
}