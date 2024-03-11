package com.pragmo.kyeootomi.model.data

open class Item(val type : String, var _no : Int, var numCollection: Int?, var title : String?) {

    constructor(item : Item) : this(item.type, item._no, item.numCollection, item.title) {

    }
}