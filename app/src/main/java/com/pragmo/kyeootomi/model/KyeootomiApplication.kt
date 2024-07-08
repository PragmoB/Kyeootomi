package com.pragmo.kyeootomi.model

import android.app.Application
import android.app.DownloadManager
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ProcessLifecycleOwner
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.DownloadReceiver
import java.io.File

class KyeootomiApplication : Application() {
    val downloadReceiver = DownloadReceiver()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED)
        else
            registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        Item.filesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!

        ProcessLifecycleOwner.get().lifecycle.addObserver(KyeootomiLifecycleObserver(applicationContext))
    }

}