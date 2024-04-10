package com.pragmo.kyeootomi.model

import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.pragmo.kyeootomi.view.activity.SwipeLockActivity

class KyeootomiLifecycleObserver(
    private val context: Context
) : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                val intentSwipeLock = Intent(context, SwipeLockActivity::class.java)
                intentSwipeLock.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                context.startActivity(intentSwipeLock)
            }

            else -> {}
        }
    }
}