package com.pragmo.kyeootomi.model.data

class HitomiItem : Item {

    var number : Int
    var downloaded: Boolean

    // 없어도 되는 선택옵션들
    var artist : String?
    var series : String?
    var tags : List<String>?

    constructor(item : Item,
                number : Int,
                downloaded: Boolean,
                artist: String? = null,
                series: String? = null,
                tags: List<String>? = null) : super(item) {
        this.number = number
        this.downloaded = downloaded
        this.artist = artist
        this.series = series
        this.tags= tags
    }
}