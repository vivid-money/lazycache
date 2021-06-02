package vivid.money

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler

class TestSchedulerPlugin(
    private val scheduler: TestScheduler = TestScheduler(),
    private val triggerActionsBeforeEachTest: Boolean = true
) : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        RxJavaPlugins.setIoSchedulerHandler { scheduler }
        RxJavaPlugins.setComputationSchedulerHandler { scheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { scheduler }
    }

    override suspend fun beforeTest(testCase: TestCase) {
        if (triggerActionsBeforeEachTest) {
            scheduler.triggerActions()
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        RxJavaPlugins.reset()
    }
}