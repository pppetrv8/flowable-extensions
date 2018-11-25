package com.test.kotlin_flowable_test.kotlinflowableextension.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import com.test.kotlin_flowable_test.kotlinflowableextension.extension.lifecycleAndRetryOnIOError
import com.test.kotlin_flowable_test.kotlinflowableextension.utils.LifecycleManager
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivityViewModel: BaseViewModel<String>() {

    val manager: LifecycleManager = LifecycleManager()
    @Volatile var started = false

    override fun attach(owner: LifecycleOwner, observer: Observer<String>) {
        super.attach(owner, observer)
        manager.attachLifecycle(owner.lifecycle)
    }

    override fun detach(owner: LifecycleOwner) {
        manager.detachLifecycle(owner.lifecycle)
    }

    private fun getData(interval: Long): Flowable<String> {
        return Flowable
            .interval(interval, TimeUnit.SECONDS)
            .map { i ->
                val valueStr = "item: $i"
                valueStr
            }
    }

    fun turn() {
        if (started) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        stop()
        started = true
        val flow = getData(1)
        runTestFunction(flow)
    }

    fun stop() {
        clear()
        started = false
    }

    private fun runTestFunction(flow: Flowable<String>) {
        val flowFromFunc = flow.lifecycleAndRetryOnIOError(manager)
            .doOnNext { str ->
                println(str)
            }
        val disposable = flowFromFunc
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(liveData::postValue)
        mCompositeDisposable.add(disposable)
    }
}
