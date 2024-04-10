package com.pragmo.kyeootomi.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.view.activity.SwipeLockActivity

open class BaseActivity : AppCompatActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}