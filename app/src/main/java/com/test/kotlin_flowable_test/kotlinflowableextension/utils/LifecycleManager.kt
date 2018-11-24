package com.test.kotlin_flowable_test.kotlinflowableextension.utils

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.Flowable

class LifecycleManager: LifecycleObserver {

    var initialState = true
    private var lifecycleProcessor = BehaviorProcessor.create<Boolean>()
    private var openValve: Flowable<Boolean>

    init {
        openValve = lifecycleProcessor
            .filter { v -> v }
            .take(1)
    }

    fun <R> attachValve(flow: Flowable<R>): Flowable<R> {
        val valved = flow
            .concatMap { i ->
                openValve.map { v -> i }
            }
        initState()
        return valved
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
        lifecycleProcessor.onNext(true)
        println("LifecycleManager, received ON_RESUME")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        lifecycleProcessor.onNext(false)
        println("LifecycleManager, received ON_PAUSE")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycleProcessor.onComplete()
        println("LifecycleManager, received ON_DESTROY")
    }
}
