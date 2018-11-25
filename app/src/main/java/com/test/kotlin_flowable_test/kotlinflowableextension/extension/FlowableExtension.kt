package com.test.kotlin_flowable_test.kotlinflowableextension.extension

import com.test.kotlin_flowable_test.kotlinflowableextension.utils.LifecycleManager
import com.test.kotlin_flowable_test.kotlinflowableextension.utils.getCurrentTimeMillis
import io.reactivex.Flowable
import java.io.IOException

/**
 *
 *  @param retryExceptions list of exception classes that method will handle
 *  @param retryMaxTimes number, default: 3 tries
 *  @param maxDelayTime seconds, default: 30 sec
 *  @return the new Flowable instance with retry on errors support
 */
fun <R> Flowable<R>.retryOnError(retryExceptions: Array<Class<Throwable>>,
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
            println("timeElapsed: $timeElapsed ms")
            if (isErrorHandled ) { count++ }
            when {
                timeElapsed >= maxDelayTime * 1000 -> {
                    println("timeout, timeElapsed: $timeElapsed ms")
                    Flowable.error { error }
                }
                count <= retryMaxTimes && isErrorHandled -> {
                    println("handling ${error::class.java.simpleName}, count: $count")
                    Flowable.just { null }
                }
                else -> {
                    println("unhandled exception or max count: $count, exception: ${error::class.java.simpleName}")
                    Flowable.error { error }
                }
            }
        }
    }
}

/**
 *
 *  @param manager: LifecycleManager instance of class that is responsive
 *  for pause/resume Flowable emitting depending on Android UI lifecycle events
 *  @param retryMaxTimes number, default: 3 tries
 *  @param maxDelayTime seconds, default: 30 sec
 *  @return the new Flowable instance with UI lifecycle control support
 */
fun <R> Flowable<R>.lifecycleAndRetryOnIOError(manager: LifecycleManager,
    retryMaxTimes: Long = 3, maxDelayTime: Long = 30): Flowable<R> {
    val sourceValved = manager.attachValve(this)
    return sourceValved.retryOnError(
        arrayOf(IOException::class.java as Class<Throwable>), retryMaxTimes, maxDelayTime)
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
