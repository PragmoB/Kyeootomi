package com.pragmo.kyeootomi.model

import android.app.Application
import android.app.DownloadManager
import android.content.IntentFilter
import com.pragmo.kyeootomi.model.repository.DownloadReceiver

class KyeootomiApplication : Application() {
    val downloadReceiver = DownloadReceiver()

    override fun onCreate() {
        super.onCreate()
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

}