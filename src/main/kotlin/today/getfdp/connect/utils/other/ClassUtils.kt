package today.getfdp.connect.utils.other

import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil
import java.lang.reflect.Modifier
import java.net.URI

object ClassUtils {
    /**
     * scan classes with specified superclass like what Reflections do but with log4j [ResolverUtil]
     * @author liulihaocai
     */
    fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {
        // use resolver in log4j to scan classes in target package
        val resolver = ResolverUtil()

        // set class loader
        resolver.classLoader = klass.classLoader

        // set package to scan
        resolver.findInPackage(object : ResolverUtil.Test {
            override fun matches(type: Class<*>): Boolean {
                return true
            }

            override fun matches(resource: URI): Boolean {
                throw UnsupportedOperationException()
            }

            override fun doesMatchClass() = true

            override fun doesMatchResource() = false
        }, packagePath)

        // use a list to cache classes
        val list = mutableListOf<Class<out T>>()

        for(resolved in resolver.classes) {
            // check if class is assignable from target class
            if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
                // add to list
                list.add(resolved as Class<out T>)
            }
        }

        return list
    }

    fun <T : Any> resolveInstances(packagePath: String, klass: Class<T>): List<T> {
        val list = mutableListOf<T>()

        for(clazz in resolvePackage(packagePath, klass)) {
            list.add(getInstance(clazz))
        }

        return list
    }

    fun <T : Any> getInstance(klass: Class<T>): T {
        return try {
            klass.newInstance()
        } catch (e: IllegalAccessException) {
            klass.getDeclaredField("INSTANCE").get(null) as T
        }
    }
}