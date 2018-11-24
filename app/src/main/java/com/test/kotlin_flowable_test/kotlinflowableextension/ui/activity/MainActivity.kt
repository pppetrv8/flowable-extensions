package com.test.kotlin_flowable_test.kotlinflowableextension.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.test.kotlin_flowable_test.kotlinflowableextension.R

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        

        startButton.setOnClickListener { v ->

        }
    }

    fun addToLog(str: String) {
        textView.append(str)
    }

}
