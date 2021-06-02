package vivid.money.inmemorycache

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.TestScheduler
import vivid.money.TestSchedulerPlugin

private const val CACHE_KEY = "key"

private const val VALUE_1 = "Value 1"
private const val VALUE_2 = "Value 2"

class InMemoryCacheTest : BehaviorSpec() {

    private val scheduler = TestScheduler()

    init {
        Given("Empty cache: Case 1") {
            val cache = InMemoryCache().of<String>(CACHE_KEY)
            When("Value is put to cache") {
                cache.set(VALUE_1)
                Then("peek returns it") {
                    cache.peek() shouldBe VALUE_1
                }
                Then("get returns it") {
                    cache.get().test().trigger().assertResult(VALUE_1)
                }
                Then("When observing starts with it") {
                    cache.observe().test().trigger().assertValuesOnly(VALUE_1)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Empty cache: Case 2") {
            val cache = InMemoryCache().of<String>(CACHE_KEY)
            When("updateIfPresent is called") {
                cache.updateIfPresent { VALUE_2 }
                Then("peek return it") {
                    cache.peek().shouldBeNull()
                }
                Then("get returns empty maybe") {
                    cache.get().test().trigger().assertResult()
                }
                Then("When observing doesn't emit values") {
                    cache.observe().test().trigger().assertEmpty()
                }
                Then("Cache doesn't have a value") {
                    cache.hasValue().shouldBeFalse()
                }
            }
        }
        Given("Empty cache: Case 3") {
            val cache = InMemoryCache().of<String>(CACHE_KEY)
            When("update is called") {
                cache.update { VALUE_2 }
                Then("peek return it") {
                    cache.peek() shouldBe VALUE_2
                }
                Then("get returns it") {
                    cache.get().test().trigger().assertResult(VALUE_2)
                }
                Then("When observing starts with it") {
                    cache.observe().test().trigger().assertValuesOnly(VALUE_2)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Empty cache: Case 4") {
            val cache = InMemoryCache().of<String>(CACHE_KEY)
            When("It is cleared") {
                cache.clear()
                Then("peek returns null") {
                    cache.peek().shouldBeNull()
                }
                Then("get completes") {
                    cache.get().test().trigger().assertResult()
                }
                Then("When observing doesn't emit anything") {
                    cache.observe().test().trigger().assertEmpty()
                }
                Then("Cache doesn't have a value") {
                    cache.hasValue().shouldBeFalse()
                }
            }
        }
        Given("Cache with value: Case 1") {
            val cache = InMemoryCache().of<String>(CACHE_KEY)
            cache.set(VALUE_1)
            When("New values is put to cache") {
                cache.set(VALUE_2)
                Then("peek return it") {
                    cache.peek() shouldBe VALUE_2
                }
                Then("get returns the new value") {
                    cache.get().test().trigger().assertResult(VALUE_2)
                }
                Then("When observing starts with new value") {
                    cache.observe().test().trigger().assertValuesOnly(VALUE_2)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Cache with value: Case 2") {
            val cache = InMemoryCache().of<String>(CACHE_KEY, false)
            cache.set(VALUE_1)
            When("It is cleared") {
                cache.clear()
                Then("peek returns null") {
                    cache.peek().shouldBeNull()
                }
                Then("get completes") {
                    cache.get().test().trigger().assertResult()
                }
                Then("When observing doesn't emit anything") {
                    cache.observe().test().trigger().assertEmpty()
                }
                Then("Cache doesn't have a value") {
                    cache.hasValue().shouldBeFalse()
                }
            }
        }
        Given("Cache with value: Case 3") {
            val inMemoryCache = InMemoryCache()
            val cache = inMemoryCache.of<String>(CACHE_KEY, true)
            cache.set(VALUE_1)
            When("Cache is cleared") {
                inMemoryCache.clear()
                Then("peek returns the value") {
                    cache.peek() shouldBe VALUE_1
                }
                Then("get returns the value") {
                    cache.get().test().trigger().assertResult(VALUE_1)
                }
                Then("When observing starts with the value") {
                    cache.observe().test().trigger().assertValuesOnly(VALUE_1)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Cache with value = 1: Case 1") {
            val cache = InMemoryCache().of<Int>(CACHE_KEY)
            cache.set(1)
            When("updateIfPresent increments the value") {
                cache.updateIfPresent { it + 1 }
                Then("peek return 2") {
                    cache.peek() shouldBe 2
                }
                Then("get returns 2") {
                    cache.get().test().trigger().assertResult(2)
                }
                Then("When observing starts with 2") {
                    cache.observe().test().trigger().assertValuesOnly(2)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Cache with value = 1: Case 2") {
            val cache = InMemoryCache().of<Int>(CACHE_KEY)
            cache.set(1)
            When("update increments the value") {
                cache.update { it!! + 1 }
                Then("peek return 2") {
                    cache.peek() shouldBe 2
                }
                Then("get returns 2") {
                    cache.get().test().trigger().assertResult(2)
                }
                Then("When observing starts with 2") {
                    cache.observe().test().trigger().assertValuesOnly(2)
                }
                Then("Cache has value") {
                    cache.hasValue().shouldBeTrue()
                }
            }
        }
        Given("Cache with value 1 and an observer") {
            val cache = InMemoryCache().of<Int>(CACHE_KEY)
            cache.set(1)
            val observer = cache.observe().test()
            When("Cache is cleared and new vaue is set") {
                cache.clear()
                cache.set(2)
                Then("Observer receives both initial and new values") {
                    observer.assertValuesOnly(1, 2)
                }
            }
        }
        Given("Cache with two handles") {
            val cache = InMemoryCache()
            val firstHandle = cache.of<String>("key1")
            val secondHandle = cache.of<String>("key2")
            When("Value is set to the first handle") {
                firstHandle.set(VALUE_1)
                Then("peeking second handle returns null") {
                    secondHandle.peek().shouldBeNull()
                }
                Then("Second handle is empty") {
                    secondHandle.get().test().trigger().assertResult()
                }
                Then("When subscribing to second handle it's empty") {
                    secondHandle.observe().test().trigger().assertValuesOnly()
                }
                Then("Second handle doesn't have a value") {
                    secondHandle.hasValue().shouldBeFalse()
                }
            }
        }
    }

    private fun <T> TestObserver<T>.trigger(): TestObserver<T> {
        scheduler.triggerActions()
        return this
    }

    override fun listeners() = listOf(TestSchedulerPlugin(scheduler))
}