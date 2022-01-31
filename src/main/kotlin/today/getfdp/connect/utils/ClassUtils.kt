package today.getfdp.connect.utils

import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil
import java.lang.reflect.Modifier
import java.net.URI

object ClassUtils {
    /**
     * scan classes with specified superclass like what Reflections do but with log4j [ResolverUtil]
     * @author liulihaocai
     */
    fun <T : Any> resolvePackage(packagePath: String, clazz: Class<T>): List<Class<out T>> {
        // use resolver in log4j to scan classes in target package
        val resolver = ResolverUtil()

        // set class loader
        resolver.classLoader = clazz.classLoader

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
            if(clazz.isAssignableFrom(resolved) && !clazz.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
                // add to list
                list.add(resolved as Class<out T>)
            }
        }

        return list
    }
}