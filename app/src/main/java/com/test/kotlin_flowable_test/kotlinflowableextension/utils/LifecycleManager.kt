package com.test.kotlin_flowable_test.kotlinflowableextension.utils

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Flowable

class LifecycleManager: LifecycleObserver {

    var initialState = true
    @Volatile var isPaused = false

    fun <R> attachFlow(flow: Flowable<R>): Flowable<R> {
        val valveFilter = flow.filter { i -> !isPaused }
        initState()
        return valveFilter
    }

    fun attachLifecycle(lifecycle: Lifecycle?) {
        lifecycle?.addObserver(this)
    }

    fun detachLifecycle(lifecycle: Lifecycle?) {
        lifecycle?.removeObserver(this)
    }

    private fun initState() {
        if (initialState) {
            onResume()
        } else {
            onPause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isPaused = false
        println("LifecycleManager, received ON_RESUME")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        isPaused = true
        println("LifecycleManager, received ON_PAUSE")
    }
}
