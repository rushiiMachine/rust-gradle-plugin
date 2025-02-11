package dev.rushii.rgp

import android.app.Activity
import android.os.Bundle
import android.util.Log

class TestActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Log.i("TestActivity", LibHello().helloWorld("Juan"))
	}
}
