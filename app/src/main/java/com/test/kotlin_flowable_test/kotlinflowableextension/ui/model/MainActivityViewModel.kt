package com.test.kotlin_flowable_test.kotlinflowableextension.ui.model

import android.arch.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    fun testSubscribeAndRetryOnIOError() {
//        val source = getTestFlowable(60L, 1L)
//
//        val lman = LifecycleManager()
//        val testSubscriber = getTestSubscriber()
//
//        emulateLifecycleEvents(lman)
//
//        source.subscribeAndRetryOnIOError(testSubscriber, lman)
//
//        testSubscriber.awaitDone(2, TimeUnit.MINUTES)
//
//        assert(!list.isEmpty())
//        println("values: $list")
    }

    override fun onCleared() {
        super.onCleared()
    }
}