package com.test.kotlin_flowable_test.kotlinflowableextension.utils

private const val NANOSECONDS_VALUE = 1000000L

fun getCurrentTimeMillis() = (System.nanoTime().toDouble() / NANOSECONDS_VALUE.toDouble()).toLong()

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