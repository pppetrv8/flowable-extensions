package com.test.kotlin_flowable_test.kotlinflowableextension.extension

import com.test.kotlin_flowable_test.kotlinflowableextension.utils.LifecycleManager
import com.test.kotlin_flowable_test.kotlinflowableextension.utils.getCurrentTimeMillis
import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 *
 *  @param retryExceptions list of exception classes that method will handle
 *  @param retryMaxTimes number, default: 3 tries
 *  @param maxDelayTime milliseconds, default: 30000 (30 sec)
 *  @return the new Flowable instance
 */
fun <R> Flowable<R>.retryOnError(
    vararg retryExceptions: Class<Throwable>,
    retryMaxTimes: Long = 3,
    maxDelayTime: Long = 30000): Flowable<R> {
    val timestamp = Timestamp()
    var first = true
    return timeout(maxDelayTime, TimeUnit.SECONDS)
        .retry(retryMaxTimes) { error ->
        val catchException = error.javaClass in retryExceptions
        var retry = catchException
        if (catchException) {
            println("retry block, error: $error")
            if (first) {
                timestamp.reset()
                first = false
                println("retry block, init timestamp: $timestamp")
            }
            val timeElapsed = timestamp.getTime()
            println("retry block, timeElapsed: $timeElapsed")
            if (timeElapsed >= maxDelayTime) {
                println("retry block, timeout, time exceeded")
                retry = false
            }
        } else {
            println("retry block, unmanageable exception: $error")
        }
        retry
    }
}

/**
 *
 *  @param retryExceptions list of exception classes that method will handle
 *  @param retryMaxTimes number, default: 3 tries
 *  @param maxDelayTime seconds, default: 30 sec
 *  @return the new Flowable instance
 */
fun <R> Flowable<R>.retryOnError1(
    vararg retryExceptions: Class<Throwable>,
    retryMaxTimes: Long = 3,
    maxDelayTime: Long = 30): Flowable<R> {
    val timestamp = Timestamp()
    var count = 0
    var first = true
    return retryWhen { src ->
            src.flatMap { error ->
            val isErrorHandled = error::class.java in retryExceptions
            if (first) {
                timestamp.reset()
                first = false
                println("init timestamp: $timestamp")
            }
            val timeElapsed = timestamp.getTime()
            println("timeElapsed: $timeElapsed")
            if (count < retryMaxTimes - 1 && isErrorHandled
                && timeElapsed <= maxDelayTime * 1000) {
                count++
                println("handle IOException, count: $count")
                Flowable.just { null }
            } else {
                println("unhandled exception or count: $count, error: $error")
                Flowable.error { error }
            }
        }
    }
}

fun <R> Flowable<R>.subscribeAndRetryOnIOError(subscriber: FlowableSubscriber<R>,
                                               manager: LifecycleManager,
                                               scheduler: Scheduler = Schedulers.trampoline()) {
    val sourceValved = manager.attachValve(this)
    sourceValved.retryOnError1(IOException::class.java as Class<Throwable>)
        .subscribeOn(scheduler)
        .subscribe(subscriber)
}

class Timestamp {
    private var timestamp: Long = getCurrentTimeMillis()
    fun reset() {
        timestamp = getCurrentTimeMillis()
    }
    fun getTime(): Long {
        val curTime = getCurrentTimeMillis()
        return curTime - timestamp
    }
    override fun toString(): String {
        return "$timestamp"
    }
}
