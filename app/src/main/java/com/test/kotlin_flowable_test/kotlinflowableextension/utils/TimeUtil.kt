package com.test.kotlin_flowable_test.kotlinflowableextension.utils

private const val NANOSECONDS_VALUE = 1000000L

fun getCurrentTimeMillis() = (System.nanoTime().toDouble() / NANOSECONDS_VALUE.toDouble()).toLong()
