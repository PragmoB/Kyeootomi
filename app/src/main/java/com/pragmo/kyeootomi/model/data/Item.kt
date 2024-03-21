package com.pragmo.kyeootomi.model.data

import android.content.Context
import java.io.File

open class Item(
    val type : String,
    var _no : Int,
    var numCollection: Int?,
    var title : String?
) {

    companion object {
        lateinit var filesDir : File
    }
    constructor(item : Item) : this(item.type, item._no, item.numCollection, item.title) {

    }
}