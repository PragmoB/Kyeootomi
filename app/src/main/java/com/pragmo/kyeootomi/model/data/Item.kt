package com.pragmo.kyeootomi.model.data

import android.content.Context
import java.io.File

open class Item(
    val type : String,
    var _no : Int,
    var collection: Collection,
    var title : String?
) {

    constructor(item : Item) : this(item.type, item._no, item.collection, item.title) {

    }
}