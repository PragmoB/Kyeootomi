package com.pragmo.kyeootomi.model.data

import android.content.Context
import java.io.File

open class Item(
    val type : ItemType,
    var _no : Int,
    var collection: Collection,
    var title : String?
) {

    enum class ItemType(val otherName: String, val domain: String) {
        HITOMI("히토미", "hitomi.la"), CUSTOM("사용자 지정", "")
    }

    constructor(item : Item) : this(item.type, item._no, item.collection, item.title) {
    }
}