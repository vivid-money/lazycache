package vivid.money.lazycache

import com.google.gson.reflect.TypeToken
import vivid.money.CacheConfig

/**
 * Generates unique keys for cache handles.
 * Uses combination of information about Args and Result
 */
class CacheKeyGenerator<Result : Any>(
    private val resultToken: TypeToken<Result>,
    private val customKey: String?,
) {

    private val gson = CacheConfig.gson

    fun generatePrefix(): String = customKey ?: resultToken.toString()

    fun <Args : Any> generate(args: Args): String {
        val className = args.javaClass.canonicalName ?: error("Can't get class name of $args")
        val serializedArgs = gson.toJson(args)
        val prefix = generatePrefix()
        return "$prefix$serializedArgs$className"
    }
}
