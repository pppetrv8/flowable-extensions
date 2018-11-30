package com.test.kotlin_flowable_test.kotlinflowableextension.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.test.kotlin_flowable_test.kotlinflowableextension.R
import com.test.kotlin_flowable_test.kotlinflowableextension.ui.model.MainActivityViewModel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        setButtonState(viewModel.started)
        viewModel.attach(this, Observer { str ->
            addToLog(str)
        })
        startButton.setOnClickListener { v ->
            viewModel.turn()
            setButtonState(viewModel.started)
        }
        clearButton.setOnClickListener { v ->
            clearLog()
        }
    }

    private fun setButtonState(started: Boolean) {
        val btnText = if (started) {
            R.string.stop_emitting_text
        } else {
            R.string.start_emitting_text
        }
        startButton.setText(btnText)
    }

    private fun addToLog(str: String?) {
        textView.append("$str\n")
    }

    private fun clearLog() {
        textView.text = ""
    }
}
