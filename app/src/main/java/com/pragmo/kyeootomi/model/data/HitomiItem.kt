package com.pragmo.kyeootomi.model.data

import java.io.File

class HitomiItem(
    item: Item,
    var number: Int,
    var downloaded: Boolean,
    var artist: String? = null,
    var series: String? = null,
    var tags: List<String>? = null
) : Item(item) {


    val filesDir : File
        get() {
            val filesDir = File(Item.filesDir, "/hitomi-$_no")
            if (!filesDir.exists())
                filesDir.mkdirs()
            return filesDir
        }
    fun getFile(order : Int) : File? {
        val file = File(filesDir, "/$order.webp")
        if (!file.exists())
            return null
        return file
    }
}