package com.pragmo.kyeootomi.model.repository

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import org.jsoup.Jsoup

class ItemModel(private val context : Context) {

    private fun getHitomiInfo(number : Int, onComplete : (Bundle?) -> Unit) {

        val bun = Bundle()

        // 자바스크립트 디코더용 웹 뷰
        val webview = WebView(context)

        val onLoad = object: WebViewClient() {

            override fun onPageFinished(view : WebView, url : String) {
                super.onPageFinished(view, url);
                val javascriptUrl = if (url.startsWith("https://hitomi.la/reader/"))
                    "javascript:window.Android.getGalleryHtml(document.getElementsByTagName('body')[0].innerHTML);"
                else
                    "javascript:window.Android.getInfoHtml(document.getElementsByTagName('body')[0].innerHTML);"

                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.loadUrl(javascriptUrl)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                request?.url?.host?.let {
                    // loadUrl로 요청한 hitomi.la 도메인이 아닌, 받아온 html의 href속성의 링크 요청도 걸리는걸 확인했기에 중복 방지를 위하여 검사함
                    if (it.startsWith("hitomi.la"))
                        onComplete.invoke(null)
                }
            }
        }
        class MyJavascriptInterface {
            // 각각의 이벤트를 한 번만 실행하기 위한 플래그
            private var completeGetGallery = false
            private var completeGetInfo = false

            @JavascriptInterface
            fun getGalleryHtml(html: String) {

                val galleryDoc = Jsoup.parse(html)
                val links = galleryDoc?.select("a.brand") ?: return
                val link = links[0]?.attr("href")?:""
                if (link.isEmpty())
                    return

                // webviewLoadInfo.post => Handler(Looper.getMainLooper()).post로 바꾸니까 되네 뭔 차이일까
                Handler(Looper.getMainLooper()).post {
                    if (completeGetGallery)
                        return@post
                    completeGetGallery = true
                    webview.loadUrl("https://hitomi.la$link")
                }
            }
            @JavascriptInterface
            fun getInfoHtml(html: String) {
                val infoDoc = Jsoup.parse(html)
                var autoTitle = bun.getString("autoTitle")?:""
                infoDoc?.select("#gallery-brand")?.select("a")?.text()?.let {
                    autoTitle = it
                }
                if (autoTitle.isEmpty()) // 크롤링 실패 여부 검사
                    return

                /* 여기서부터는 크롤링 완전 성공으로 판정 */

                if (completeGetInfo) // 중복 호출 여부 검사
                    return
                completeGetInfo = true

                val artist = infoDoc?.select("#artists")?.text()
                val series = infoDoc?.select("#series")?.select(".comma-list")?.first()
                    ?.select("a")?.get(0)?.text()
                val tags = arrayListOf<String>()
                infoDoc?.select("#tags")?.let {
                    for (i in it.select("li")) {
                        i?.select("a")?.text()?.let { tag ->
                            tags.add(tag)
                        }
                    }
                }

                bun.putString("autoTitle", autoTitle)
                bun.putString("artist", artist)
                bun.putString("series", series)
                bun.putStringArrayList("tags", tags)

                onComplete.invoke(bun)
            }
        }
        webview.addJavascriptInterface(MyJavascriptInterface(), "Android")
        webview.settings.javaScriptEnabled = true
        webview.visibility = View.VISIBLE
        webview.webViewClient = onLoad

        webview.loadUrl("https://hitomi.la/reader/$number.html#1")
    }

    fun addHitomi(item : HitomiItem, useTitle : Boolean, onComplete : (Boolean) -> Unit) {


        val onGetInfoComplete : (Bundle?) -> Unit = { bun ->

            // bun, item에 담긴 작품 정보를 DB에 등록
            // 코드 가독성이 훼손된 부분은 널 안전성을 위하여 어쩔 수 없었음.

            val db = ItemDBHelper(context).writableDatabase
            val values = ContentValues()
            item.numCollection?.let {
                values.put("collection", it)
            } ?: values.putNull("collection")
            if (useTitle)
                values.put("title", item.title)
            else
                bun?.let {
                    values.put("title", it.getString("autoTitle"))
                } ?: values.putNull("title")
            values.put("number", item.number)
            values.put("downloaded", item.downloaded)
            bun?.let {
                values.put("artist", it.getString("artist"))
                values.put("series", it.getString("series"))
            } ?: run {
                values.putNull("artist")
                values.putNull("series")
            }
            var concatenates = ""
            bun?.getStringArrayList("tags")?.let {
                for (tag in it)
                    concatenates += "$tag|"
            }
            if (concatenates.isEmpty())
                values.putNull("tags")
            else {
                concatenates = concatenates.substring(0, concatenates.length - 1)
                values.put("tags", concatenates)
            }

            db.insert("HitomiItem", null, values)
            onComplete.invoke(true)
        }
        getHitomiInfo(item.number, onGetInfoComplete)
    }
    fun addCustom(item : CustomItem) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        if (item.numCollection == null)
            values.putNull("collection")
        else
            values.put("collection", item.numCollection)
        values.put("title", item.title?:"auto")
        values.put("URL", item.url)
        db.insert("CustomItem", null, values)
        return true
    }
    fun getHitomi(numCollection : Int?) : List<HitomiItem> {
        val db = ItemDBHelper(context).readableDatabase
        val listHitomi = mutableListOf<HitomiItem>()
        val cursor = db.query(
            "HitomiItem",
            arrayOf<String>("_no", "title", "number", "downloaded", "date", "artist", "series", "tags"),
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()),
            null, null, null)

        while (cursor.moveToNext()) {

            val item = Item(
                "hitomi",
                cursor.getInt(0),
                numCollection,
                if (cursor.isNull(1)) null else cursor.getString(1))

            val hitomiItem = HitomiItem(
                item,
                cursor.getInt(2),
                cursor.getInt(3) != 0,
                if (cursor.isNull(5)) null else cursor.getString(5),
                if (cursor.isNull(6)) null else cursor.getString(6),
                if (cursor.isNull(7)) null else cursor.getString(7).split("|"))
            listHitomi.add(hitomiItem)
        }

        cursor.close()
        return listHitomi
    }
    fun getCustom(numCollection : Int?) : List<CustomItem> {
        val db = ItemDBHelper(context).readableDatabase
        val listCustom = mutableListOf<CustomItem>()
        val cursor = db.query("CustomItem",
            arrayOf<String>("_no", "title", "URL", "date"),
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()),
            null, null, null)

        while (cursor.moveToNext()) {
            val item = Item("custom", cursor.getInt(0), numCollection, cursor.getString(1))
            val customItem = CustomItem(item, cursor.getString(2))
            listCustom.add(customItem)
        }

        cursor.close()
        return listCustom
    }
    fun get(numCollection: Int?): List<Item> {
        return getHitomi(numCollection) + getCustom(numCollection)
    }
    fun delete(numItem: Int) {
        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem", "_no=?", arrayOf(numItem.toString()))
        db.delete("CustomItem", "_no=?", arrayOf(numItem.toString()))
    }
    fun deleteByCollection(numCollection: Int?) {
        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem",
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()))
        db.delete("CustomItem",
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()))
    }
}