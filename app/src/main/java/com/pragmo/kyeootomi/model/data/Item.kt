package com.pragmo.kyeootomi.model.data

open class Item(val type : String, var numCollection: Int?, var title : String) {

    constructor(item : Item) : this(item.type, item.numCollection, item.title) {

    }
}