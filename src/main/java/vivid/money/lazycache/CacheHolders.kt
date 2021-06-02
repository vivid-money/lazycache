package vivid.money.lazycache

class NoArgLazyCacheHolder<T : Any>(
    private val holder: LazyCacheHolder<T, Unit>
) : BaseLazyCacheHolder<T, Unit>(holder) {

    fun get() = holder.getCache(Unit)
}

class OneArgLazyCacheHolder<T : Any, Arg : Any>(
    private val holder: LazyCacheHolder<T, Arg>
) : BaseLazyCacheHolder<T, Arg>(holder) {

    fun get(arg: Arg) = holder.getCache(arg)
}

class TwoArgsLazyCacheHolder<T : Any, Arg1, Arg2>(
    private val holder: LazyCacheHolder<T, Pair<Arg1, Arg2>>
) : BaseLazyCacheHolder<T, Pair<Arg1, Arg2>>(holder) {

    fun get(arg1: Arg1, arg2: Arg2) = holder.getCache(arg1 to arg2)
}

class ThreeArgsLazyCacheHolder<T : Any, Arg1, Arg2, Arg3>(
    private val holder: LazyCacheHolder<T, Triple<Arg1, Arg2, Arg3>>
) : BaseLazyCacheHolder<T, Triple<Arg1, Arg2, Arg3>>(holder) {

    fun get(arg1: Arg1, arg2: Arg2, arg3: Arg3) = holder.getCache(Triple(arg1, arg2, arg3))
}

open class BaseLazyCacheHolder<T : Any, Arg : Any>(
    private val holder: LazyCacheHolder<T, Arg>
) {

    fun clear() = holder.clear()

    fun <V> mapAllCached(action: (LazyCache<T, Arg>) -> V): List<V> = holder.getAllCached().map { action(it) }
}
