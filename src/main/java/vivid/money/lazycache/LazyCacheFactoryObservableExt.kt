package vivid.money.lazycache

import io.reactivex.rxjava3.core.Observable

inline fun <reified Result : Any> LazyCacheFactory.ofObservable(
    crossinline updater: () -> Observable<Result>
) = ofObservable(null, updater)

inline fun <Arg : Any, reified Result : Any> LazyCacheFactory.ofObservable(
    crossinline updater: (arg: Arg) -> Observable<Result>
) = ofObservable(null, updater)

inline fun <Arg1, Arg2, reified Result : Any> LazyCacheFactory.ofObservable(
    crossinline updater: (arg1: Arg1, arg2: Arg2) -> Observable<Result>
) = ofObservable(null, updater)

inline fun <Arg1, Arg2, Arg3, reified Result : Any> LazyCacheFactory.ofObservable(
    crossinline updater: (arg1: Arg1, arg2: Arg2, arg3: Arg3) -> Observable<Result>
) = ofObservable(null, updater)

inline fun <reified Result : Any> LazyCacheFactory.ofObservable(
    customKey: String?,
    crossinline updater: () -> Observable<Result>
) = NoArgLazyCacheHolder(cacheHolder(customKey) {
    updater()
})

inline fun <Arg : Any, reified Result : Any> LazyCacheFactory.ofObservable(
    customKey: String?,
    crossinline updater: (arg: Arg) -> Observable<Result>
) = OneArgLazyCacheHolder(cacheHolder(customKey) { arg: Arg ->
    updater(arg)
})

inline fun <Arg1, Arg2, reified Result : Any> LazyCacheFactory.ofObservable(
    customKey: String?,
    crossinline updater: (arg1: Arg1, arg2: Arg2) -> Observable<Result>
) = TwoArgsLazyCacheHolder(cacheHolder(customKey) { args: Pair<Arg1, Arg2> ->
    updater(args.first, args.second)
})

inline fun <Arg1, Arg2, Arg3, reified Result : Any> LazyCacheFactory.ofObservable(
    customKey: String?,
    crossinline updater: (arg1: Arg1, arg2: Arg2, arg3: Arg3) -> Observable<Result>
) = ThreeArgsLazyCacheHolder(cacheHolder(customKey) { args: Triple<Arg1, Arg2, Arg3> ->
    updater(args.first, args.second, args.third)
})
