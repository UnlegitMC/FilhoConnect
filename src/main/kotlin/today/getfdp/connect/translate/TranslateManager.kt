package today.getfdp.connect.translate

import today.getfdp.connect.network.provider.BedrockProxyProvider
import today.getfdp.connect.utils.other.ClassUtils
import today.getfdp.connect.utils.other.Configuration
import today.getfdp.connect.utils.other.logWarn
import java.lang.reflect.Method
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy
import java.util.concurrent.TimeUnit


object TranslateManager {

    val translators = mutableListOf<TranslatorBase<*>>()
    val translateMethod: Method

    private var asyncEnabled = false
    private lateinit var threadPool: ThreadPoolExecutor

    init {
        TranslatorBase::class.java.declaredMethods.find { it.name == "translate" }!!.let {
            translateMethod = it
        }
    }

    fun initialize() {
        translators.clear()

        ClassUtils.resolveInstances(this.javaClass.`package`.name, TranslatorBase::class.java).forEach {
            translators.add(it)
        }

        if (Configuration[Configuration.Key.DO_ASYNC]) {
            asyncEnabled = true
            threadPool = ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(), Int.MAX_VALUE,
                60,
                TimeUnit.SECONDS,
                SynchronousQueue(),
                CallerRunsPolicy()
            )
        } else {
            asyncEnabled = false
            logWarn("Async mode is disabled! This makes all translation tasks process synchronously and may slow down your server.")
        }
    }

    fun handle(provider: BedrockProxyProvider, packet: Any) {
        translators.filter { it.intendedClass.isInstance(packet) }.forEach {
            if(asyncEnabled && it.async()) {
                threadPool.execute {
                    translateMethod.invoke(it, provider, packet)
                }
            } else {
                translateMethod.invoke(it, provider, packet)
            }
        }
    }
}