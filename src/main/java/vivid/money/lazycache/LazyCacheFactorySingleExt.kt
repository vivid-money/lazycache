package vivid.money.lazycache

import io.reactivex.rxjava3.core.Single

inline fun <reified Result : Any> LazyCacheFactory.of(
    crossinline updater: () -> Single<Result>
) = of(null, updater)

inline fun <Arg : Any, reified Result : Any> LazyCacheFactory.of(
    crossinline updater: (arg: Arg) -> Single<Result>
) = of(null, updater)

inline fun <Arg1, Arg2, reified Result : Any> LazyCacheFactory.of(
    crossinline updater: (arg1: Arg1, arg2: Arg2) -> Single<Result>
) = of(null, updater)

inline fun <Arg1, Arg2, Arg3, reified Result : Any> LazyCacheFactory.of(
    crossinline updater: (arg1: Arg1, arg2: Arg2, arg3: Arg3) -> Single<Result>
) = of(null, updater)

inline fun <reified Result : Any> LazyCacheFactory.of(
    customKey: String?,
    crossinline updater: () -> Single<Result>
) = ofObservable<Result>(customKey) { updater().toObservable() }

inline fun <Arg : Any, reified Result : Any> LazyCacheFactory.of(
    customKey: String?,
    crossinline updater: (arg: Arg) -> Single<Result>
) = ofObservable<Arg, Result>(customKey) { arg -> updater(arg).toObservable() }

inline fun <Arg1, Arg2, reified Result : Any> LazyCacheFactory.of(
    customKey: String?,
    crossinline updater: (arg1: Arg1, arg2: Arg2) -> Single<Result>
) = ofObservable<Arg1, Arg2, Result>(customKey) { arg1, arg2 -> updater(arg1, arg2).toObservable() }

inline fun <Arg1, Arg2, Arg3, reified Result : Any> LazyCacheFactory.of(
    customKey: String?,
    crossinline updater: (arg1: Arg1, arg2: Arg2, arg3: Arg3) -> Single<Result>
) = ofObservable<Arg1, Arg2, Arg3, Result>(customKey) { arg1, arg2, arg3 -> updater(arg1, arg2, arg3).toObservable() }
