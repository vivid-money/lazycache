package vivid.money

import java.util.concurrent.atomic.AtomicReference

/**
 * Concurrent getOrPut, that is thread safe.
 *
 * Returns the value. If the value is not present, calls the [defaultValue] function,
 * set its result and returns it.
 *
 * This method guarantees not to put the value if it's already there,
 * but the [defaultValue] function may be invoked even if value is already present.
 */
fun <T> AtomicReference<T>.getOrPut(defaultValue: () -> T): T {
    // Implementation is taken from ConcurrentHashMap.getOrPut
    return this.get() ?: defaultValue().also { this.compareAndSet(null, it) }
}