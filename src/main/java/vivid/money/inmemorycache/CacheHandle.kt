package vivid.money.inmemorycache

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable

/**
 * Represents a single entry in cache.
 * For example, it can be a cache for a list of items.
 */
interface CacheHandle<T> {

    fun set(value: T)

    fun get(): Maybe<T>

    fun observe(): Observable<T>

    fun clear()

    fun peek(): T?

    fun updateIfPresent(newValue: (previousValue: T) -> T)

    fun update(newValue: (previousValue: T?) -> T)

    fun hasValue(): Boolean
}