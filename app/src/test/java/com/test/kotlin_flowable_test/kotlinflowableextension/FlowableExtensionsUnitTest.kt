package com.test.kotlin_flowable_test.kotlinflowableextension

import com.test.kotlin_flowable_test.kotlinflowableextension.extension.*
import com.test.kotlin_flowable_test.kotlinflowableextension.utils.LifecycleManager
import io.reactivex.Flowable
import org.junit.Test

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import java.util.concurrent.TimeUnit

class FlowableExtensionsUnitTest {

    val list = ArrayList<Int>()

    @Test
    fun testRetryOnError_ProcessWithoutExceptions() {
        println("testRetryOnIOError Process Without Exceptions")
        val source = getTestFlowable(10L, 1L)
        val sourceWithRetryOnIOError = source.retryOnError(
            arrayOf(IOException::class.java as Class<Throwable>))

        processResults(sourceWithRetryOnIOError)
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
            arrayOf(IOException::class.java as Class<Throwable>))

        processResults(sourceWithRetryOnIOError)
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
            arrayOf(IOException::class.java as Class<Throwable>))
        processResults(sourceWithRetryOnIOError)
    }

    @Test
    fun testLifecycleAndRetryOnIOError() {
        val source = getTestFlowable(30L, 1L)

        val lman = LifecycleManager()
        val testSubscriber = getTestSubscriber()

        emulateLifecycleEvents(lman)

        val sourceIOAndLifecycle = source.lifecycleAndRetryOnIOError(lman)
            .doOnNext { i ->
                println("source item: $i")
            }
        sourceIOAndLifecycle
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.io())
            .subscribe(testSubscriber)

        testSubscriber.awaitDone(2, TimeUnit.MINUTES)

        assert(!list.isEmpty())
        println("values: $list")
    }

    private fun emulateLifecycleEvents(lman: LifecycleManager, delay: Long = 15, maxCount: Long = 2) {
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
    }

    private fun processResults(source: Flowable<Int>) {
        processResults(getTestSubscriber(), source)
    }

    private fun processResults(subscriber: TestSubscriber<Int>, source: Flowable<Int>) {
        val sourcePrintLn = source.doOnNext {
            println("source item: $it")
        }
        sourcePrintLn
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

    private fun getTestFlowable(maxItemsCount: Long = 20L, interval: Long = 3L): Flowable<Int> {
        val atomicInteger = AtomicInteger()
        return Flowable.fromCallable {
            if (atomicInteger.incrementAndGet() < maxItemsCount) {
                atomicInteger.get()
            } else {
                maxItemsCount.toInt()
            }
        }.delay(interval, TimeUnit.SECONDS).repeat(maxItemsCount)
    }
}
