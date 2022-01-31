package today.getfdp.connect.network.translate

import today.getfdp.connect.utils.ClassUtils

object TranslateManager {

    val bedrockTranslatores = mutableListOf<BedrockTranslator>()
    val javaTranslatores = mutableListOf<JavaTranslator>()

    fun initialize() {
        bedrockTranslatores.clear()
        javaTranslatores.clear()

        ClassUtils.resolvePackage(this.javaClass.`package`.name, BedrockTranslator::class.java).forEach {
            try {
                bedrockTranslatores.add(it.newInstance())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        ClassUtils.resolvePackage(this.javaClass.`package`.name, JavaTranslator::class.java).forEach {
            try {
                javaTranslatores.add(it.newInstance())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}