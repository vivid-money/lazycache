package vivid.money.lazycache

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.scopes.WhenScope
import io.kotest.matchers.nulls.shouldBeNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.TestScheduler
import vivid.money.inmemorycache.InMemoryCache
import io.kotest.core.spec.style.BehaviorSpec
import vivid.money.TestSchedulerPlugin
import java.util.concurrent.TimeUnit

private const val INTEGER_VALUE = 1

class LazyCacheTest : BehaviorSpec() {

    private val scheduler = TestScheduler()
    private val factory = LazyCacheFactory(InMemoryCache())
    private val integers = TestUpdaterProvider(Single.just(INTEGER_VALUE))

    init {
        /* Basic behavior test for empty cache */
        Given("Empty lazy cache of integers") {
            val cache = emptyCacheOfIntegers()
            When("Checks if has value") {
                val hasValue = cache.hasValue()
                Then("It is not present") {
                    hasValue.shouldBeFalse()
                }
                `Then value wasn't requested`()
            }
            When("Updates if empty") {
                cache.updateIfEmpty().test()
                Then("It is loaded") {
                    cache.peek() shouldBe INTEGER_VALUE
                }
                `Then value was requested once`()
            }
            When("Values is force updated and requested") {
                val observer = cache.forceUpdateAndGet().test()
                Then("It is loaded") {
                    observer.assertResult(INTEGER_VALUE)
                }
                `Then value was requested once`()
            }
            When("Value is force updated") {
                cache.forceUpdate().test()
                Then("It is loaded") {
                    cache.peek() shouldBe INTEGER_VALUE
                }
                `Then value was requested once`()
            }
            When("Gets value") {
                val observer = cache.get().test()
                Then("It is loaded") {
                    observer.assertResult(INTEGER_VALUE)
                }
                `Then value was requested once`()
            }
            When("Peeks value") {
                val value = cache.peek()
                Then("It is null") {
                    value.shouldBeNull()
                }
                `Then value wasn't requested`()
            }
            When("Observers value") {
                val observer = cache.observe().test()
                Then("It is loaded") {
                    observer.assertValuesOnly(INTEGER_VALUE)
                }
                `Then value was requested once`()
            }
            When("Value is set") {
                cache.set(2)
                Then("It can be peeked") {
                    cache.peek() shouldBe 2
                }
                `Then value wasn't requested`()
            }
            When("Value is updated if present") {
                cache.updateIfPresent { 2 }
                Then("Cache is still empty") {
                    cache.peek().shouldBeNull()
                }
                `Then value wasn't requested`()
            }
            When("Value is updated") {
                cache.update { 2 }
                Then("It can be peeked") {
                    cache.peek() shouldBe 2
                }
                `Then value wasn't requested`()
            }
            When("Value is cleared") {
                cache.clear()
                Then("It is still null") {
                    cache.peek().shouldBeNull()
                }
                `Then value wasn't requested`()
            }
        }

        /* Basic behavior test for cache with value */
        Given("Lazy cache of integers with value") {
            val cache = cacheOfIntegersWithValue(INTEGER_VALUE)

            When("Checks if has value") {
                val hasValue = cache.hasValue()
                Then("It is present") {
                    hasValue.shouldBeTrue()
                }
                `Then value wasn't requested`()
            }
            When("Peeks value") {
                val value = cache.peek()
                Then("It is equal to initial one") {
                    value shouldBe INTEGER_VALUE
                }
                `Then value wasn't requested`()
            }
            When("Value is requested") {
                val observer = cache.get().test()
                Then("Value is received") {
                    observer.assertResult(INTEGER_VALUE)
                }
                `Then value wasn't requested`()
            }
            When("Value is observed") {
                val observer = cache.observe().test()
                Then("Value is received") {
                    observer.assertValuesOnly(INTEGER_VALUE)
                }
                `Then value wasn't requested`()
            }
            When("Value is updated if present") {
                cache.updateIfPresent { 2 }
                Then("New value can be peeked") {
                    cache.peek() shouldBe 2
                }
                `Then value wasn't requested`()
            }
            When("Value is updated") {
                cache.update { 2 }
                Then("New value can be peeked") {
                    cache.peek() shouldBe 2
                }
                `Then value wasn't requested`()
            }
            When("Value is cleared") {
                cache.clear()
                Then("Cache is empty") {
                    cache.peek().shouldBeNull()
                }
                `Then value wasn't requested`()
            }
        }

        /* Observable cache test */
        Given("Empty cache of an observable") {
            val cache = factory.ofObservable<Int> { Observable.just(1, 2) }.get()
            When("forceUpdate is called after forceUpdateAndObserve") {
                val observer = cache.forceUpdateAndObserve().test()
                cache.forceUpdate().test()
                Then("Observer doesn't receive subsequent values") {
                    observer.assertResult(1, 2)
                }
            }
        }
        Given("Cache of observable with loaded data") {
            val cache = factory.ofObservable<Int> { Observable.just(1, 2) }.get()
            cache.set(3)
            When("forceUpdate is called after forceUpdateAndObserve") {
                val observer = cache.forceUpdateAndObserve().test()
                cache.forceUpdate().test()
                Then("Observer doesn't receive the initial value") {
                    observer.assertResult(1, 2)
                }
            }
        }

        /* Concurrency tests */
        Given("Empty cache") {
            val integers = TestUpdaterProvider(IncrementalSingle(0).delay(1, TimeUnit.SECONDS))
            val cache = factory.of(integers).get()
            When("Requested value twice simultaneously") {
                val firstObserver = cache.get().test()
                val secondObserver = cache.get().test()
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                Then("First observer receives the value") {
                    firstObserver.assertResult(0)
                }
                Then("Second observer receives the value") {
                    secondObserver.assertResult(0)
                }
                Then("Value was requested only once") {
                    integers.totalSubscriptions shouldBe 1
                }
            }
            When("Value is observed by two observers simultaneously") {
                val firstObserver = cache.observe().test()
                val secondObserver = cache.observe().test()
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                Then("First observer receives the value") {
                    firstObserver.assertValuesOnly(0)
                }
                Then("Second observer receives the value") {
                    secondObserver.assertValuesOnly(0)
                }
                Then("Value was requested only once") {
                    integers.totalSubscriptions shouldBe 1
                }
            }
            When("Value is force updated WHILE cache is being observed") {
                val firstObserver = cache.observe().test()
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                firstObserver.assertValuesOnly(0)

                val secondObserver = cache.forceUpdateAndGet().test()
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                secondObserver.assertResult(1)

                Then("The observer receives the updated value") {
                    firstObserver.assertValuesOnly(0, 1)
                }
            }
            When("Value is manually updated while cache is being observed") {
                val firstObserver = cache.observe().test()
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS)
                firstObserver.assertValuesOnly(0)

                cache.update { it!! + 1 }

                Then("The observer receives the updated value") {
                    firstObserver.assertValuesOnly(0, 1)
                }
            }
        }
    }

    //region Assertions
    private suspend fun WhenScope.`Then value was requested once`() = `Then value was requested exactly`(1)
    private suspend fun WhenScope.`Then value wasn't requested`() = `Then value was requested exactly`(0)
    private suspend fun WhenScope.`Then value was requested exactly`(times: Int) {
        Then("Then value was requested exactly $times times") {
            integers.totalSubscriptions shouldBe times
        }
    }
    //endregion

    private fun cacheOfIntegersWithValue(value: Int) = emptyCacheOfIntegers().apply { set(value) }
    private fun emptyCacheOfIntegers() = factory.of(integers).get()

    // Required because of mutable lazy cache state
    override fun isolationMode() = IsolationMode.InstancePerTest

    override fun listeners() = listOf(TestSchedulerPlugin(scheduler))
}

private class IncrementalSingle(
    initial: Int
) : Single<Int>() {

    private var current = initial

    override fun subscribeActual(downstream: SingleObserver<in Int>) {
        downstream.onSubscribe(Disposable.disposed())
        downstream.onSuccess(current++)
    }
}

private class TestSingle<T>(
    private val single: Single<T>
) : Single<T>() {

    private var subscriberCountInternal = 0

    val subscriberCount: Int
        get() = subscriberCountInternal

    override fun subscribeActual(downstream: SingleObserver<in T>) {
        single.subscribe(Observer(downstream) { ++subscriberCountInternal })
    }

    private class Observer<T>(
        private val downstream: SingleObserver<T>,
        private val finally: () -> Unit
    ) : SingleObserver<T> {

        override fun onSuccess(t: T) {
            downstream.onSuccess(t)
            finally()
        }

        override fun onSubscribe(d: Disposable) {
            downstream.onSubscribe(d)
        }

        override fun onError(e: Throwable) {
            downstream.onError(e)
            finally()
        }
    }
}

private class TestUpdaterProvider<T>(
    private val single: Single<T>
) : () -> Single<T> {

    private val singles = mutableListOf<TestSingle<T>>()

    val totalSubscriptions: Int
        get() = singles.sumBy { it.subscriberCount }

    override fun invoke(): Single<T> = TestSingle(single).also { singles.add(it) }
}
