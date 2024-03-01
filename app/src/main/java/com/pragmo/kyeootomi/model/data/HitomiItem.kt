package com.pragmo.kyeootomi.model.data

class HitomiItem : Item {

    var number : Int
    var downloaded: Boolean
    constructor(item : Item, number : Int, downloaded: Boolean) : super(item) {
        this.number = number
        this.downloaded = downloaded
    }
}