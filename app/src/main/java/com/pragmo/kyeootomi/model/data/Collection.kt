package com.pragmo.kyeootomi.model.data

import java.io.File

data class Collection(var num : Int?, var numParentCollection : Int?, var name : String) {
    companion object {
        lateinit var filesDir : File
    }
    val dir : File
        get() {
            val dir = File(filesDir, "/${num ?: 0}")
            if (!dir.exists())
                dir.mkdirs()
            return dir
        }
}