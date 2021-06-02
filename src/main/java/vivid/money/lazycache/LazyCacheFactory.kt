package vivid.money.lazycache

import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Observable
import vivid.money.inmemorycache.InMemoryCache
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for caches of a specific updater.
 *
 * Use [of] functions to create instances.
 * Each function takes an `updater` to load data, while will be cached inside [inMemoryCache].
 *
 * Usage example:
 * ```
 * class SomeClass(
 *     private val lazyCacheFactory: LazyCacheFactory,
 *     private val api: SomeApi
 * ) {
 *     private val requestCache = lazyCacheFactory.of(api::someApiMethod)
 *
 *     fun observeData(argument: SomeType) = requestCache.get(argument).observe()
 *
 *     fun forceUpdateData(argument: SomeType) = requestCache.get(argument).forceUpdate()
 * }
 * ```
 */
@Singleton
class LazyCacheFactory @Inject constructor(
    val inMemoryCache: InMemoryCache
) {

    val holders = ConcurrentHashMap<String, LazyCacheHolder<Any, Any>>()

    inline fun <Args : Any, reified Result : Any> cacheHolder(
        customKey: String?,
        noinline updater: (args: Args) -> Observable<Result>
    ): LazyCacheHolder<Result, @ParameterName(name = "args") Args> {
        val token = object : TypeToken<Result>() {}
        val key = CacheKeyGenerator(token, customKey).generatePrefix()
        return holders.getOrPut(key) {
            LazyCacheHolder(inMemoryCache, updater, customKey, token) as LazyCacheHolder<Any, Any>
        } as LazyCacheHolder<Result, Args>
    }
}
