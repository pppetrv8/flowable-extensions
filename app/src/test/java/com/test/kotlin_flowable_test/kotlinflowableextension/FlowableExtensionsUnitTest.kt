package com.test.kotlin_flowable_test.kotlinflowableextension

import com.test.kotlin_flowable_test.kotlinflowableextension.extension.*
import com.test.kotlin_flowable_test.kotlinflowableextension.utils.LifecycleManager
import io.reactivex.Flowable
import org.junit.Test

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import timber.log.Timber
import java.util.concurrent.TimeUnit

class FlowableExtensionsUnitTest {

    val list = ArrayList<Int>()

    @Test
    fun testRetryOnError_ProcessWithoutExceptions() {
        println("testRetryOnIOError Process Without Exceptions")
        val source = getTestFlowable(10L, 1L)
        val sourceWithRetryOnIOError = source.retryOnError(
            IOException::class.java as Class<Throwable>)
        processResults(sourceWithRetryOnIOError, false)
    }

    @Test
    fun testRetryOnError_ProcessOnIOExceptions() {
        println("testRetryOnIOError Process IOExceptions")
        val source = getTestFlowable(20L, 1L)
        val sourceIoErrors = source
            .flatMap { i: Int ->
                when {
                    i % 4 == 0 -> Flowable.error(IOException())
                    else -> Flowable.just(i)
                }
            }

        val sourceWithRetryOnIOError = sourceIoErrors.retryOnError(
            IOException::class.java as Class<Throwable>)

        processResults(sourceWithRetryOnIOError, true)
    }

    @Test
    fun testRetryOnError_TestTimeout() {
        println("testRetryOnIOError Test Timeout")
        val source = getTestFlowable(25L, 3L)
        val sourceIoErrors = source
            .flatMap { i: Int ->
                when {
                    i % 10 == 0 -> Flowable.error(IOException())
                    else -> Flowable.just(i)
                }
            }

        val sourceWithRetryOnIOError = sourceIoErrors.retryOnError(
            IOException::class.java as Class<Throwable>)

        processResults(sourceWithRetryOnIOError, true)
    }

    @Test
    fun testRetryOnError_TestTimeoutIfNoItemsInStream() {
        println("testRetryOnIOError Test Timeout If No Items In Stream")
        val source = getTestFlowableWithDelayAndIOErr(20L, 1L)
        val sourceWithRetryOnIOError = source.retryOnError(
            IOException::class.java as Class<Throwable>)
        processResults(sourceWithRetryOnIOError, true)
    }

    @Test
    fun testRetryOnError1() {
        val source = getTestFlowableWithDelayAndIOErr(20L, 1L)
        val methodResultFlowable = source.retryOnError1(IOException::class.java as Class<Throwable>)
        processResults(methodResultFlowable, false)
    }

    @Test
    fun testSubscribeAndRetryOnIOError() {
        val source = getTestFlowable(60L, 1L)

        val lman = LifecycleManager()
        val testSubscriber = getTestSubscriber()

        emulateLifecycleEvents(lman)

        source.subscribeAndRetryOnIOError(testSubscriber, lman)

        testSubscriber.awaitDone(2, TimeUnit.MINUTES)

        assert(!list.isEmpty())
        println("values: $list")
    }

    private fun emulateLifecycleEvents(lman: LifecycleManager, delay: Long = 10, maxCount: Long = 7) {
        var count = 0
        val emulatedFlow = Flowable.fromCallable { "" }
            .delay(delay, TimeUnit.SECONDS)
            .repeat(maxCount)
            .flatMap { s ->
                count++
                println("flatMap, count: $count")
                if (count % 2 == 0) {
                    lman.onResume()
                } else {
                    lman.onPause()
                }
                Flowable.just(count < maxCount)
            }
        val subscriber = TestSubscriber<Boolean>()
        emulatedFlow
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(subscriber)
        //subscriber.awaitTerminalEvent()
    }

    private fun processResults(source: Flowable<Int>, checkErr: Boolean = false) {
        processResults(getTestSubscriber(), source, checkErr)
    }

    private fun processResults(subscriber: TestSubscriber<Int>, source: Flowable<Int>, checkErr: Boolean = false) {
        source
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.io())
            .subscribe(subscriber)
        subscriber.awaitDone(2, TimeUnit.MINUTES)
        assert(!list.isEmpty())
        println("values: $list")
    }

    private fun getTestSubscriber(): TestSubscriber<Int> {
        return object: TestSubscriber<Int>() {
            override fun onNext(i: Int) {
                super.onNext(i)
                list.add(i)
            }
        }
    }

    private fun getTestFlowableWithDelayAndIOErr(maxItemsCount: Long = 20L, interval: Long = 3L): Flowable<Int> {
        val source = getTestFlowable(maxItemsCount, interval)
        val sourceIoErrors = source
            .flatMap { i: Int ->
                when {
                    i % 4 == 0 -> Flowable.error(IOException())
                    else -> Flowable.just(i)
                }
            }
        return sourceIoErrors.delayForItem(5, 40000)
    }

    private fun Flowable<Int>.delayForItem(itemIdx: Int, delay: Long): Flowable<Int> {
        return delay { i ->
            if (i == itemIdx) {
                try {
                    Thread.sleep(delay)
                } catch (e: InterruptedException) {
                    Timber.e(e)
                }
            }
            Flowable.just(i)
        }
    }

    private fun getTestFlowable(maxItemsCount: Long = 20L, interval: Long = 3L): Flowable<Int> {
        val atomicInteger = AtomicInteger()
        return Flowable.fromCallable {
            if (atomicInteger.incrementAndGet() < maxItemsCount) {
                atomicInteger.get()
            } else {
                maxItemsCount.toInt()
            }
        }.delay(interval, TimeUnit.SECONDS).repeat(maxItemsCount)
            .doOnNext { println("source Flowable: $it") }
            .doOnComplete { println("source Flowable done") }
    }
}
