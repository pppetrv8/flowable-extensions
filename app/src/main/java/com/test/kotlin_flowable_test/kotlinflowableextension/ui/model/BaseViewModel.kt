package com.test.kotlin_flowable_test.kotlinflowableextension.ui.model

import android.arch.lifecycle.*
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit

open class BaseViewModel<D>: ViewModel() {

    val liveData = MutableLiveData<D>()
    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    open fun attach(owner: LifecycleOwner, observer: Observer<D>) {
        liveData.observe(owner, observer)
    }

    open fun detach(owner: LifecycleOwner) {
    }

    fun clear() {
        mCompositeDisposable.clear()
    }

    fun dispose() {
        mCompositeDisposable.dispose()
    }

    override fun onCleared() {
        dispose()
    }
}
