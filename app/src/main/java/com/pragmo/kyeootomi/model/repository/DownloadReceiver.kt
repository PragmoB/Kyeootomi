package com.pragmo.kyeootomi.model.repository

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadReceiver : BroadcastReceiver() {
    interface OnCompleteListener {
        fun onSuccess()
        fun onFail()
    }

    private val onCompleteCallbacks = mutableMapOf<Long, OnCompleteListener>()

    override fun onReceive(context: Context, intent: Intent) {
        val downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        val query = DownloadManager.Query()
        query.setFilterById(downId)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(query)
        if (!cursor.moveToFirst())
            return

        val columnStatusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        if (cursor.getInt(columnStatusIndex) == DownloadManager.STATUS_FAILED) // 파일 다운로드가 실패한 경우
            onCompleteCallbacks[downId]?.onFail()
        else
            onCompleteCallbacks[downId]?.onSuccess()
        onCompleteCallbacks.remove(downId)
    }
    fun setOnCompleteListener(downId: Long, callback: OnCompleteListener) {
        onCompleteCallbacks[downId] = callback
    }
}