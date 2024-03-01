package com.pragmo.kyeootomi.model.data

class CustomItem : Item {

    var url: String

    constructor(item : Item, url : String) : super(item) {
        this.url = url
    }
}