package com.pragmo.kyeootomi.model.repository

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.pragmo.kyeootomi.model.KyeootomiApplication
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ItemModel(private val context : Context) {

    /* 히토미 작품 처리 */

    private fun downloadHitomi(number: Int, orders: MutableList<Int>, absolutePath: String) {
        if (orders.size == 0)
            return

        // 자바스크립트 디코더용 웹 뷰
        val webView = WebView(context.applicationContext)

        val onLoad = object: WebViewClient() {

            override fun onPageFinished(view : WebView, url : String) {
                super.onPageFinished(view, url);
                if (url.startsWith("https://hitomi.la/reader")) {
                    // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                    // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                    view.loadUrl("javascript:window.Android.getGalleryHtml(document.getElementsByTagName('body')[0].innerHTML);")
                }
            }
        }
        class MyJavascriptInterface {
            var completeGallery = false

            val objOnCompleteListener = object : DownloadReceiver.onCompleteListener {
                override fun onSuccess() {
                    if (orders.size > 1)
                        Toast.makeText(context, "히토미 ${number}번 작품 ${orders[0]}번 이미지 다운로드 성공(${orders.size - 1}개 남음)", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "히토미 ${number}번 작품 다운로드가 완료되었습니다", Toast.LENGTH_SHORT).show()
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (connectivityManager.activeNetwork == null) {
                        Toast.makeText(context, "네트워크 연결이 불안정하여 다운로드에 실패하였습니다", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // 네트워크가 연결되었고 다운로드 할 대기열이 남아있으면 다음 다운로드 요청
                    orders.removeFirst()
                    Handler(Looper.getMainLooper()).post {
                        downloadHitomi(number, orders, absolutePath)
                    }
                }

                override fun onFail() {
                    Toast.makeText(context, "히토미 ${number}번 작품 ${orders[0]}번 이미지 다운로드 재요청(${orders.size}개 남음)", Toast.LENGTH_SHORT).show()
                    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (connectivityManager.activeNetwork == null) {
                        Toast.makeText(context, "네트워크 연결이 불안정하여 다운로드에 실패하였습니다", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // 네트워크가 연결되있으면 다시 다운로드 요청

                    Handler(Looper.getMainLooper()).post {
                        downloadHitomi(number, orders, absolutePath)
                    }
                }
            }
            @JavascriptInterface
            fun getGalleryHtml(html: String) {

                if (completeGallery)
                    return

                // imageUrl = 작품 이미지의 url 추출
                val galleryDoc = Jsoup.parse(html)
                val imgs = galleryDoc.select("#mobileImages")?.select("picture")
                    ?.select("img")?:return
                if (imgs.size <= 0) return
                val imageUrl = imgs[0]?.attr("src")?: return

                completeGallery = true

                // absolutePath 디렉터리 생성
                val filesDir = File(absolutePath)
                filesDir.mkdirs()

                // 추출한 url 다운로드
                val request = DownloadManager.Request(Uri.parse(imageUrl))
                request.setTitle("히토미 ${number}번 작품 다운로드 중(${orders.size}개 남음)")
                request.setDescription("#${orders[0]}")
                request.setDestinationUri(Uri.fromFile(File(filesDir, "/${orders[0]}.webp")))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                request.setAllowedOverMetered(true)
                request.setAllowedOverRoaming(true)
                request.addRequestHeader("Sec-Fetch-Site", "same-site")
                request.addRequestHeader("Sec-Fetch-Mode", "no-cors")
                request.addRequestHeader("Sec-Fetch-Dest", "image")
                request.addRequestHeader("Referer", "https://hitomi.la/reader/$number.html")
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downId = downloadManager.enqueue(request)

                // 다운로드 완료 콜백 설정
                val kyeootomiApplication = if (context.applicationContext is KyeootomiApplication)
                    context.applicationContext as KyeootomiApplication
                else null
                kyeootomiApplication?.downloadReceiver?.setOnCompleteListener(downId, objOnCompleteListener)
            }
        }
        val javascriptInterface = MyJavascriptInterface()
        webView.addJavascriptInterface(javascriptInterface, "Android")
        webView.settings.javaScriptEnabled = true
        webView.visibility = View.VISIBLE
        webView.webViewClient = onLoad

        webView.loadUrl("https://hitomi.la/reader/$number.html#${orders[0]}")

        /*
         * 2024.3.16 : 안정성을 확인하기 위해 다운로드를 수없이 진행하던 중 중간에 다운로드가 끊기는 현상을 여러번 겪었다.
         * 에러도 딱히 나는게 없고 너무 불규칙적으로 발생하다보니 개선하기가 쉽지 않았다.
         * 혹시 히토미 쪽에서 타 사이트로 redirect를 한다던가 하는것 때문에 페이지 로딩에 실패한건 아닐까 싶어
         * webview client에 shouldOverrideURLloading에서 무조건 리다이렉트를 차단하도록 한다던가
         * onReceiveError에 무슨 이벤트가 걸리나 관찰해본다던가 여러가지 방법을 시도해봤으나 그 원인을 좀처럼 잡아낼 수 없었다.
         * 그렇게 수없이 다운로드 테스트를 지속하던 중 로그에서 특징적인 현상이 보였는데,chromium(WebView)에서 Rederer process가 크래시났다는 로그였다.
         * 아닐때도 있기는 하지만, 다운로드가 끊기는 거의 대부분의 경우에서 이 로그가 따라나타났다.
         * 문제를 해결하는데 실마리가 될 것이라는 느낌이 들어 구글링을 많이 했고, webview client에서 onRenderProcessGone이라는 콜백을 override할 수 있음을 배웠다.
         * => 실패했다. 그냥 콜백 호출이 아예 안된다.
         *
         * 지쳐서 포기하고 자다가 일어나서 다시 작업했는데 의외로 간단하게 해결됐다.
         * 특정 시간 이상 페이지를 불러오지 못하면 다시 요청하는 것이다. 아래가 그 코드이다.
         * 덕분에 onReceiveError나 shouldOverrideURLloading을 override 하지 않아도 됐다.
         * 어차피 실패해도 여기서 재요청 보내면 그만이니까.
         * ㅎㅎ;;
         */
        Handler(Looper.getMainLooper()).postDelayed({
            // 타임아웃 콜백 설정
            if (!javascriptInterface.completeGallery) {
                Toast.makeText(context, "히토미 ${number}번 작품 ${orders[0]}번 이미지 다운로드 재요청(${orders.size}개 남음)", Toast.LENGTH_SHORT).show()
                javascriptInterface.completeGallery = true
                downloadHitomi(number, orders, absolutePath)
            }
        }, 6000)
    }
    /*
     * bundle values : title: String, artist: String, series: String, tags: StringArrayList
     */
    private fun crawlHitomiInfo(number : Int, onComplete : (Bundle?) -> Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager.activeNetwork == null) {
            Toast.makeText(context, "네트워크 연결이 불안정하여 히토미 ${number}번 작품 정보 로딩에 실패하였습니다", Toast.LENGTH_SHORT).show()
            onComplete(null)
            return
        }

        val bun = Bundle()

        // 자바스크립트 디코더용 웹 뷰
        val webView = WebView(context.applicationContext)

        val onLoad = object: WebViewClient() {

            override fun onPageFinished(view : WebView, url : String) {
                super.onPageFinished(view, url);
                if (url.startsWith("https://hitomi.la/reader/"))
                    view.loadUrl("javascript:window.Android.getGalleryHtml(document.getElementsByTagName('body')[0].innerHTML);")
                else
                    view.loadUrl("javascript:window.Android.getInfoHtml(document.getElementsByTagName('body')[0].innerHTML);")

            }
        }
        class MyJavascriptInterface {
            // 각각의 이벤트를 한 번만 실행하기 위한 플래그
            var completeGetGallery = false
            var completeGetInfo = false

            @JavascriptInterface
            fun getGalleryHtml(html: String) {

                if (completeGetGallery)
                    return

                val galleryDoc = Jsoup.parse(html)
                // 작품 메인페이지 uri
                val infoUri = galleryDoc?.select("a.brand")?.get(0)?.attr("href")
                    ?: return
                if (infoUri.isEmpty())
                    return

                // 작품 이미지 개수 추출
                val count = galleryDoc.select("#mobile-single-page-select")?.first()?.select("option")?.size ?: return
                if (count == 0)
                    return

                bun.putInt("count", count)

                completeGetGallery = true

                // webviewLoadInfo.post => Handler(Looper.getMainLooper()).post로 바꾸니까 되네 뭔 차이일까
                Handler(Looper.getMainLooper()).post {
                    webView.loadUrl("https://hitomi.la$infoUri")
                }
            }
            @JavascriptInterface
            fun getInfoHtml(html: String) {
                if (completeGetInfo) // 중복 호출 여부 검사
                    return

                val infoDoc = Jsoup.parse(html)
                var title = bun.getString("title")?:""
                infoDoc?.select("#gallery-brand")?.select("a")?.text()?.let {
                    title = it
                }
                if (title.isEmpty()) // 크롤링 실패 여부 검사
                    return

                // 크롤링 완전 성공으로 판정
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

                bun.putString("title", title)
                bun.putString("artist", artist)
                bun.putString("series", series)
                bun.putStringArrayList("tags", tags)

                onComplete.invoke(bun)
            }
        }
        val javascriptInterface = MyJavascriptInterface()
        webView.addJavascriptInterface(javascriptInterface, "Android")
        webView.settings.javaScriptEnabled = true
        webView.visibility = View.VISIBLE
        webView.webViewClient = onLoad

        webView.loadUrl("https://hitomi.la/reader/$number.html#1")
        Handler(Looper.getMainLooper()).postDelayed({
            if (!javascriptInterface.completeGetInfo) {
                Toast.makeText(context, "히토미 ${number}번 작품 정보 로드 실패", Toast.LENGTH_SHORT).show()
                javascriptInterface.completeGetInfo = true
                onComplete(null)
            }
        }, 11000)
    }

    fun addHitomi(hitomiValues : HitomiItem, useTitle : Boolean, onComplete : (Boolean) -> Unit) {


        val onGetInfoComplete : (Bundle?) -> Unit = onGetInfoComplete@{ bun ->

            /*
             * bun, item에 담긴 작품 정보를 DB에 등록
             * 코드 가독성이 훼손된 부분은 널 안전성을 위하여 어쩔 수 없었음.
             */

            val values = ContentValues()
            hitomiValues.collection.num?.let {
                values.put("collection", it)
            } ?: values.putNull("collection")
            if (useTitle)
                values.put("title", hitomiValues.title)
            else
                bun?.let {
                    values.put("title", it.getString("title"))
                } ?: values.putNull("title")
            values.put("number", hitomiValues.number)
            values.put("downloaded", hitomiValues.downloaded)
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

            val db = ItemDBHelper(context).writableDatabase
            db.insert("HitomiItem", null, values)
            onComplete.invoke(true)

            if (bun == null)
                return@onGetInfoComplete

            /* 현재 작품 전용 파일 디렉터리 생성 */

            val cursor = db.query("HitomiItem", arrayOf("_no"),
                null, null, null, null, "_no DESC")
            cursor.moveToNext()
            hitomiValues._no = cursor.getInt(0)
            cursor.close()

            /* 여기서부터는 작품 표지 이미지파일 다운로드 */

            /* 2024.3.14. DownloadManager 사용하여 파일 다운로드.
               파일 다운로드가 안됨.
               BroadcastReceiver 등록하여 이벤트를 관찰했지만 BroadcastReceiver는 다운로드 성공으로 판단함
               이래저래 시도해보다가 알게된건 데이터를 받아오는건 성공했는데 파일로 저장하지 못했던거였음.
               request.setDestinationInExternalFilesDir 쓰던거를
               request.setDestinationInExternalPublicDir로 바꾸니 성공함. 여기서 엄청 막혔었는데 해결되는건 너무 허무하네 */

            /*
             * 2024.3.15 : 처음엔 그냥 쌩으로 for() downloadHitomi() 떄려박았는데 파일 제대로 안받아져서 고민좀 했었다.
             * 근데 Handler.postDelay로 작품 하나 다운로드당 0.5초 딜레이 주니까 수율 좋아짐ㅋㅋ
             * 다운로드가 생각보다 무거운 동작이었던 듯 하다. 트래픽은 나눠서 받자.
             *
             * 딜레이를 주는 방식은 수율은 개선할수 있으나 완벽해지는데 한계가 있음을 깨달음. 그래서 downloadHitomi랑 DownloadReceiver 설계를
             * 갈아 엎어서 하나씩 다운로드 받게끔 변경했다. 구체적으로
             * 1. downloadHitomi에서 이미지 하나 다운로드 요청, 다운로드 완료 콜백 객체 등록
             * 2. DownloadReceiver에서 이미지 다운로드 완료 이벤트를 받음
             * 2-1. 다운로드 성공 시 다운로드 완료 콜백 실행 => 다운로드 완료 콜백에서 다음 이미지 다운로드 요청함
             * 2-2. 다운로드 실패 시 다운로드 실패 콜백 실행 => 다운로드 실패 콜백에서 현재 이미지 재다운 요청함
             * 3. 다운로드 대기열이 빌떄까지 반복
             * => 결과는 성공적이었다. 안정성 확보
             *
             * 여기서 속도가 느린점을 개선하기 위하여 한 번에 n개씩 다운로드를 진행하면 좋을 것 같다고 생각했다.
             * 그걸 어떻게 구현할지 고민을 좀 했는데, 아주 나이스한 방법으로 처리할 수 있었다.
             * downloadHitomi의 다운로드 번호 범위를 n분할하여 여러번 호출하면 n개씩 받아진다
             * 그냥 여기에 몇줄만 추가하면 되니까 아주 쉽고 간편하다.
             * => 시간이 별 차이가 없다. 그냥 단일로 감
             * */
            if (hitomiValues.downloaded) {
                val count = bun.getInt("count")
                count.let {
                    Handler(Looper.getMainLooper()).post {
                        downloadHitomi(hitomiValues.number, (1..it).toMutableList(), hitomiValues.filesDir.absolutePath)
                    }
                }
            }
            else
                Handler(Looper.getMainLooper()).post {
                    downloadHitomi(hitomiValues.number, mutableListOf(1), hitomiValues.filesDir.absolutePath)
                }
        }
        crawlHitomiInfo(hitomiValues.number, onGetInfoComplete)
    }
    fun getHitomi(numItem: Int) : HitomiItem? {
        val db = ItemDBHelper(context).readableDatabase
        val cursor = db.query(
            "HitomiItem",
            arrayOf("collection", "title", "number", "downloaded", "date", "artist", "series", "tags"),
            "_no=? and reachable=1",
            arrayOf(numItem.toString()),
            null, null, null
        )
        if (!cursor.moveToNext())
            return null

        val numCollection = if (cursor.isNull(0)) null else cursor.getInt(0)
        val collectionModel = CollectionModel(context)
        val collection = collectionModel.get(numCollection)!!
        val item = Item(
            Item.ItemType.HITOMI,
            numItem,
            collection,
            cursor.getString(1)
        )
        val hitomiItem = HitomiItem(
            item,
            cursor.getInt(2),
            cursor.getInt(3) != 0,
            if (cursor.isNull(5)) null else cursor.getString(5),
            if (cursor.isNull(6)) null else cursor.getString(6),
            if (cursor.isNull(7)) null else cursor.getString(7).split("|")
        )

        cursor.close()
        return hitomiItem
    }
    fun getHitomiByCollection(numCollection : Int?) : List<HitomiItem> {

        val collectionModel = CollectionModel(context)
        val collection = collectionModel.get(numCollection)!!

        // db에서 cursor참조

        val db = ItemDBHelper(context).readableDatabase
        val listHitomi = mutableListOf<HitomiItem>()
        val cursor = db.query(
            "HitomiItem",
            arrayOf("_no", "title", "number", "downloaded", "date", "artist", "series", "tags"),
            if (numCollection == null) "collection IS NULL and reachable=1" else "collection=? and reachable=1",
            if (numCollection == null) null else arrayOf(numCollection.toString()),
            null, null, null)

        // cursor를 돌며 히토미 작품 항목들을 뽑아냄

        while (cursor.moveToNext()) {
            val item = Item(
                Item.ItemType.HITOMI,
                cursor.getInt(0),
                collection.copy(),
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
    fun updateHitomi(numItem : Int, hitomiValues: HitomiItem,
                     useTitle: Boolean, reloadInfo: Boolean,
                     useDownloadOpt: Boolean, onComplete: (Boolean) -> Unit) {

        val onGetInfoComplete : (Bundle?) -> Unit = onGetInfoComplete@{ bun ->
            val values = ContentValues()
            hitomiValues.collection.num?.let {
                values.put("collection", it)
            } ?: run {
                values.putNull("collection")
            }
            if (useTitle)
                values.put("title", hitomiValues.title)
            else
                bun?.let {
                    values.put("title", it.getString("title"))
                }
            values.put("number", hitomiValues.number)
            if (useDownloadOpt)
                values.put("downloaded", hitomiValues.downloaded)
            if (reloadInfo) {
                bun?.let {
                    values.put("artist", it.getString("artist"))
                    values.put("series", it.getString("series"))
                }
                var concatenates = ""
                bun?.getStringArrayList("tags")?.let {
                    for (tag in it)
                        concatenates += "$tag|"
                }
                if (concatenates.isNotEmpty()) {
                    concatenates = concatenates.substring(0, concatenates.length - 1)
                    values.put("tags", concatenates)
                }
            }

            val db = ItemDBHelper(context).writableDatabase
            db.update("HitomiItem", values, "_no=? and reachable=1", arrayOf(numItem.toString()))
            onComplete(true)

            if (bun == null)
                return@onGetInfoComplete

            if (useDownloadOpt && hitomiValues.downloaded)
                Handler(Looper.getMainLooper()).post {
                    downloadHitomi(
                        hitomiValues.number,
                        (1 .. bun.getInt("count")).toMutableList(),
                        hitomiValues.filesDir.absolutePath)
                }
            else if (hitomiValues.getFile(1) == null && reloadInfo)
                Handler(Looper.getMainLooper()).post {
                    downloadHitomi(hitomiValues.number, mutableListOf(1), hitomiValues.filesDir.absolutePath)
                }
        }

        val hitomiItem = getHitomi(numItem) ?: return

        // 작품 파일 삭제 관련 처리
        if (useDownloadOpt && hitomiValues.downloaded) // 작품 파일 다시 받기 옵션
            hitomiItem.filesDir.deleteRecursively()
        else if (useDownloadOpt) // 작품 파일 삭제 옵션
            hitomiItem.getFile(1)?.let { coverFile ->

            // 커버 이미지(1.webp) 빼고 작품 파일 삭제
            val coverFileName = coverFile.absolutePath.split("/").let {
                it[it.size - 1]
            }
            val coverFileTemp = File(Item.filesDir, "1.tmp")
            coverFile.renameTo(coverFileTemp)
            hitomiItem.filesDir.deleteRecursively()
            File(hitomiValues.filesDir, coverFileName).let { coverFileTemp.renameTo(it) }
        } ?: run { hitomiItem.filesDir.deleteRecursively() }

        // 크롤링 필요 여부 판단
        if (reloadInfo || (useDownloadOpt && hitomiValues.downloaded) || !useTitle) // 작품 정보 다시받기 또는 파일 다시받기 또는 제목 자동설정 시 크롤링 필요
            crawlHitomiInfo(hitomiValues.number, onGetInfoComplete)
        else
            onGetInfoComplete(null)

    }
    fun copyToGallery(hitomiItem: HitomiItem, order: Int): Boolean {

        val contentResolver = context.contentResolver
        val picturesCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .resolve("Kyeootomi").mkdirs()
        val file = hitomiItem.getFile(order) ?: return false

        // 파일 정보 설정
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/${file.extension}")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kyeootomi")
        }

        // 파일을 삽입하기 위해 ContentResolver를 사용하여 OutputStream을 엽니다.
        val insertedImageUri = contentResolver.insert(picturesCollection, imageDetails) ?: return false
        val outputStream = contentResolver.openOutputStream(insertedImageUri) as FileOutputStream

        // 이미지 파일을 읽기 위해 FileInputStream을 엽니다.
        val inputStream = FileInputStream(file)

        // InputStream에서 이미지 파일을 읽어서 OutputStream에 씁니다.
        inputStream.copyTo(outputStream)

        return true
    }
    fun deleteHitomi(hitomiItem: HitomiItem) {
        hitomiItem.filesDir.deleteRecursively()

        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem", "_no=? and reachable=1", arrayOf(hitomiItem._no.toString()))
    }

    /* 커스텀 항목 처리 */

    fun addCustom(item : CustomItem) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        if (item.collection.num == null)
            values.putNull("collection")
        else
            values.put("collection", item.collection.num)
        values.put("title", item.title?:"auto")
        values.put("URL", item.url)
        db.insert("CustomItem", null, values)
        return true
    }
    fun getCustom(numItem : Int) : CustomItem? {
        val db = ItemDBHelper(context).readableDatabase
        val cursor = db.query(
            "CustomItem",
            arrayOf("collection", "title", "URL", "date"),
            "_no=? and reachable=1",
            arrayOf(numItem.toString()),
            null, null, null
        )

        if (!cursor.moveToNext())
            return null

        val numCollection = if (cursor.isNull(0)) null else cursor.getInt(0)
        val collectionModel = CollectionModel(context)
        val collection = collectionModel.get(numCollection)!!
        val item = Item(Item.ItemType.CUSTOM, numItem, collection, cursor.getString(1))
        val customItem = CustomItem(item, cursor.getString(2))

        cursor.close()
        return customItem
    }
    fun getCustomByCollection(numCollection : Int?) : List<CustomItem> {
        val db = ItemDBHelper(context).readableDatabase
        val listCustom = mutableListOf<CustomItem>()
        val cursor = db.query("CustomItem",
            arrayOf("_no", "title", "URL", "date"),
            if (numCollection == null) "collection IS NULL and reachable=1" else "collection=? and reachable=1",
            if (numCollection == null) null else arrayOf(numCollection.toString()),
            null, null, null)

        val collectionModel = CollectionModel(context)
        while (cursor.moveToNext()) {
            val collection = collectionModel.get(numCollection)!!
            val item = Item(Item.ItemType.CUSTOM, cursor.getInt(0), collection, cursor.getString(1))
            val customItem = CustomItem(item, cursor.getString(2))
            listCustom.add(customItem)
        }

        cursor.close()
        return listCustom
    }
    fun updateCustom(numItem : Int, customValues: CustomItem) {
        val values = ContentValues()
        values.put("collection", customValues.collection.num)
        values.put("title", customValues.title)
        values.put("URL", customValues.url)

        val db = ItemDBHelper(context).writableDatabase
        db.update("CustomItem", values, "_no=? and reachable=1", arrayOf(numItem.toString()))
    }
    fun deleteCustom(customItem: CustomItem) {
        val db = ItemDBHelper(context).writableDatabase
        db.delete("CustomItem", "_no=? and reachable=1", arrayOf(customItem._no.toString()))
    }

    /* 공통 */

    fun get(itemType: Item.ItemType, numItem: Int) : Item? = when (itemType) {
        Item.ItemType.HITOMI -> getHitomi(numItem)
        Item.ItemType.CUSTOM -> getCustom(numItem)
        else -> null
    }
    fun delete(itemType: Item.ItemType, item: Item) = when(itemType) {
        Item.ItemType.HITOMI -> deleteHitomi(item as? HitomiItem ?: null!!)
        Item.ItemType.CUSTOM -> deleteCustom(item as? CustomItem ?: null!!)
    }
    fun getByCollection(numCollection: Int?): List<Item> {
        return getHitomiByCollection(numCollection) + getCustomByCollection(numCollection)
    }
    fun setReachable(itemType: Item.ItemType, numItem: Int, reachable: Boolean) {
        val values = ContentValues()
        values.put("reachable", reachable)

        val db = ItemDBHelper(context).writableDatabase
        val tableName = when (itemType) {
            Item.ItemType.HITOMI -> "HitomiItem"
            Item.ItemType.CUSTOM -> "CustomItem"
        }
        db.update(tableName, values, "_no=?", arrayOf(numItem.toString()))
    }
    fun setReachableAll(reachable: Boolean) {
        val values = ContentValues()
        values.put("reachable", reachable)

        val db = ItemDBHelper(context).writableDatabase
        db.update("HitomiItem", values, null, null)
        db.update("CustomItem", values, null, null)
    }
    fun copyToGallery(item: Item) {

        when (item) {
            is HitomiItem -> {

                val contentResolver = context.contentResolver
                val picturesCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .resolve("Kyeootomi/${item.title}").mkdirs()

                // 폴더 내 모든 파일에 대해 반복
                item.filesDir.listFiles()?.forEach { file ->
                    // 파일이 이미지인지 확인
                    if (!file.isFile)
                        return@forEach
                    if (file.extension !in arrayOf("jpg", "jpeg", "png", "webp", "avif"))
                        return@forEach

                    // 파일 정보 설정
                    val imageDetails = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/${file.extension}")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kyeootomi/${item.title}")
                    }

                    // 파일을 삽입하기 위해 ContentResolver를 사용하여 OutputStream을 엽니다.
                    val insertedImageUri = contentResolver.insert(picturesCollection, imageDetails) ?: return@forEach
                    val outputStream = contentResolver.openOutputStream(insertedImageUri) as FileOutputStream

                    // 이미지 파일을 읽기 위해 FileInputStream을 엽니다.
                    val inputStream = FileInputStream(file)

                    // InputStream에서 이미지 파일을 읽어서 OutputStream에 씁니다.
                    inputStream.copyTo(outputStream)
                }
            }

            else -> {}
        }
    }
    fun deleteByCollection(numCollection: Int?) {
        val items = getByCollection(numCollection)
        for (item in items) {
            when (item.type) {
                Item.ItemType.HITOMI -> {
                    val hitomiItem = item as HitomiItem
                    hitomiItem.filesDir.deleteRecursively()
                }

                else -> {}
            }
        }
        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem",
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()))
        db.delete("CustomItem",
            if (numCollection == null) "collection IS NULL" else "collection=?",
            if (numCollection == null) null else arrayOf(numCollection.toString()))
    }
}