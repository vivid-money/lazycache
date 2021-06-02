package vivid.money.inmemorycache

import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers.io
import io.reactivex.rxjava3.subjects.BehaviorSubject
import vivid.money.Option
import vivid.money.lazycache.CacheKeyGenerator
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class InMemoryCache @Inject constructor() {

    private val clearableKeys = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val cache = ConcurrentHashMap<String, BehaviorSubject<Option<Any>>>()

    fun <T : Any> put(key: String, value: T) = getSubject<T>(key).onNext(Option(value))

    fun remove(key: String) {
        cache[key]?.onNext(Option(null))
    }

    fun <T : Any> peek(key: String) = getSubject<T>(key).value?.value

    fun <T : Any> putIfPresent(
        key: String,
        newValue: (previousValue: T) -> T,
    ) = updateInternal<T>(key) { it?.let(newValue) }

    fun <T : Any> put(
        key: String,
        newValue: (previousValue: T?) -> T,
    ) = updateInternal(key, newValue)

    fun <T : Any> get(key: String): Maybe<T> = Maybe.create<T> { emitter ->
        getSubject<T>(key).value?.value?.let(emitter::onSuccess) ?: emitter.onComplete()
    }.observeOn(io())

    fun <T : Any> observe(key: String): Observable<T> = getSubject<T>(key)
        .hide()
        .observeOn(io())
        .filter { it.hasValue }
        .map { it.value!! }

    fun contains(key: String): Boolean = getSubject<Any>(key).value?.hasValue ?: false

    fun <T : Any> of(
        key: String,
        keepAlways: Boolean,
    ): CacheHandle<T> {
        if (!keepAlways) clearableKeys.add(key)
        return DelegatingCacheHandle(key, this)
    }

    fun <T : Any> of(
        customKey: String? = null,
        keepAlways: Boolean = false,
        type: Class<T>,
    ): CacheHandle<T> {
        val key = CacheKeyGenerator(TypeToken.get(type), customKey).generate(InMemoryCacheArg)
        return of(key, keepAlways)
    }

    /**
     * Clears data in cache and all previously returned [CacheHandle]s, except then ones created with keepAlways = true
     */
    fun clear() {
        clearableKeys.forEach { remove(it) }
    }

    private fun <T : Any> updateInternal(key: String, newValue: (previousValue: T?) -> T?) {
        val subject = getSubject<T>(key)
        synchronized(subject) {
            newValue(subject.value?.value)?.let { subject.onNext(Option(it)) }
        }
    }

    private fun <T : Any> getSubject(key: String): BehaviorSubject<Option<T>> {
        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(key) { BehaviorSubject.create() } as BehaviorSubject<Option<T>>
    }
}

/**
 * Creates a handle for a single entry in cache.
 * It can be used to avoid providing a key to every call.
 *
 * @param keepAlways Set to true to not clear data after calling [InMemoryCache.clear].
 * @param customKey Key to which data will be stored. You should not create handles with the same [customKey]
 * In case [customKey] is not specified a unique key by [T] will be generated.
 */
inline fun <reified T : Any> InMemoryCache.of(
    customKey: String? = null,
    keepAlways: Boolean = false,
): CacheHandle<T> = of(customKey, keepAlways, T::class.java)

/** Class that is used to distinguish keys for lazy and in-memory caches */
object InMemoryCacheArg
