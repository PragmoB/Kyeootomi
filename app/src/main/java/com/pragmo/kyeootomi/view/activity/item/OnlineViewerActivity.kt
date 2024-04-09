package com.pragmo.kyeootomi.view.activity.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityOnlineViewerBinding
import com.pragmo.kyeootomi.databinding.DialogFormUrlBinding
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.viewmodel.item.OnlineViewerViewModel

class OnlineViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnlineViewerBinding
    private lateinit var viewModel: OnlineViewerViewModel

    fun add(url: String) {
        val uri = Uri.parse(url)
        val intentAddItem = Intent(this, AddItemActivity::class.java)

        val onDefault = {
            intentAddItem.putExtra("itemType", Item.ItemType.CUSTOM)
            intentAddItem.putExtra("url", url)
        }
        when (uri.host) {
            Item.ItemType.HITOMI.domain -> run {
                val splitSlash = url.split("/")
                val splitDot = splitSlash[splitSlash.size - 1].split(".")

                // url이 https://hitomi.la/reader/(뽑아낼 번호).html#(대충 숫자) 형태였던 경우
                val number = splitDot[0].toIntOrNull() ?: run {
                    // url이 https://hitomi.la/(만화 종류)/(띄어쓰기가 하이픈인 작품 이름)-(언어 종류)-(뽑아낼 번호).html#(대충 숫자) 형태였던 경우
                    val splitHyphen = splitDot[0].split("-")
                    splitHyphen[splitHyphen.size - 1].toIntOrNull()
                }

                number?.let {
                    intentAddItem.putExtra("itemType", Item.ItemType.HITOMI)
                    intentAddItem.putExtra("number", it)
                    intentAddItem.putExtra("downloaded", true)
                } ?: onDefault()
            }
            else -> onDefault()
        }

        startActivity(intentAddItem)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 전달받은 값 설정 */

        val url = intent.getStringExtra("url")

        /* 뷰모델 설정 */

        viewModel = ViewModelProvider(this)[OnlineViewerViewModel::class.java]

        /* 바인딩 설정 */

        binding = DataBindingUtil.setContentView(this, R.layout.activity_online_viewer)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* 콜백 설정 */

        binding.webView.webViewClient = object: WebViewClient() {
            private var blockRedirect = false

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view ?: return true
                request ?: return true

                val uri = Uri.parse(view.url)

                if (uri.host != request.url.host) {
                    if (!blockRedirect) {
                        val dlg = AlertDialog.Builder(this@OnlineViewerActivity)
                        dlg.setTitle("리다이렉트 요청")
                        dlg.setMessage("${request.url.host}(으)로 리다이렉트 하려고 합니다.\n계속하시겠습니까?")
                        dlg.setNegativeButton("취소", null)
                        dlg.setPositiveButton("확인") { _, _ ->
                            view.loadUrl(request.url.toString())
                        }
                        dlg.show()
                    } else
                        Toast.makeText(this@OnlineViewerActivity, "납치 광고를 차단했어요. 광고 차단은 맡겨주세요", Toast.LENGTH_SHORT).show()
                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                view ?: return
                url ?: return

                val uri = Uri.parse(url)

                // 컨텐츠 제공업체에 따라 타이틀 설정
                supportActionBar?.title = Item.ItemType.CUSTOM.otherName
                Item.ItemType.values().forEach {
                    if (uri.host == it.domain)
                        supportActionBar?.title = it.otherName
                }

                // 컨텐츠 제공업체에 따라 User-Agent설정
                view.settings.userAgentString = when (uri.host) {
                    Item.ItemType.HITOMI.domain -> {
                        "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
                    }
                    else -> null
                }

                // 기본적으로 리다이렉트 차단 옵션은 true이나, 사용자 지정 domain이라면 false로 설정함
                blockRedirect = false
                for (value in Item.ItemType.values()) {
                    if (uri.host == value.domain) {
                        blockRedirect = true
                        break
                    }
                }
            }
        }
        binding.webView.webChromeClient = object: WebChromeClient() {
            private var customView: View? = null

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                if (customView != null) { // 이미 Full Screen이 표시된 상황이라면 제거 이벤트 위로 전달
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view // 제거할 때 참조하기 위해 변수에 저장
                windowManager.addView(customView, WindowManager.LayoutParams()) // Window에 풀 스크린 뷰 추가
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                windowManager.removeView(customView)
                customView = null
            }
        }
        binding.webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true // pornhub에서 검색창 열기 허용
            setSupportZoom(true)
            domStorageEnabled = true // pornhub에서 썸네일 로딩 허용
            loadWithOverviewMode = true
            useWideViewPort = true
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = false // spankbang에서 동영상 자동재생 허용
        }
        binding.webView.setOnLongClickListener { v ->
            val hitTestResult = (v as WebView).hitTestResult
            if (hitTestResult.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                val selectedUrl = hitTestResult.extra ?: return@setOnLongClickListener true
                val dlg = AlertDialog.Builder(this@OnlineViewerActivity)
                dlg.setTitle("작품 추가")
                dlg.setMessage("선택하신 링크를 활용하여 작품 추가를 진행합니다. 계속할까요?")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("확인") { _, _ ->
                    add(selectedUrl)
                }
                dlg.show()
                true // 이벤트가 소비되었음을 알리기 위해 true를 반환합니다.
            } else {
                false
            }
        }
        binding.refreshWebView.setOnRefreshListener {
            binding.webView.reload()
            binding.refreshWebView.isRefreshing = false
        }

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.webView.goBack()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        /* 시작 */

        url?.let { binding.webView.loadUrl(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_online_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAddItem -> {
                binding.webView.url?.let { add(it) }
            }
            R.id.menuChangeSite -> {
                val sites = Item.ItemType.values()
                val siteNames = Array(sites.size) {
                    sites[it].otherName
                }
                val selectDialog = AlertDialog.Builder(this)
                selectDialog.setTitle("사이트 변경")
                selectDialog.setItems(siteNames
                ) { _, which ->
                    val domain = sites[which].domain
                    if (domain.isEmpty()) {
                        val dialogFormURlBinding = DialogFormUrlBinding.inflate(layoutInflater)
                        dialogFormURlBinding.lifecycleOwner = this
                        dialogFormURlBinding.viewModel = viewModel

                        val showURLDialog = AlertDialog.Builder(this)
                        showURLDialog.setTitle("URL 지정")
                        showURLDialog.setView(dialogFormURlBinding.root)
                        showURLDialog.setNegativeButton("취소", null)
                        showURLDialog.setNeutralButton("복사") { _, _ ->
                            val formUrlValue = viewModel.formUrl.value ?: return@setNeutralButton
                            if (formUrlValue.isEmpty())
                                return@setNeutralButton

                            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("url", formUrlValue)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(this, "클립보드에 복사했어요", Toast.LENGTH_SHORT).show()
                        }
                        showURLDialog.setPositiveButton("확인"
                        ) { _, _ ->
                            val formUrlValue = viewModel.formUrl.value ?: return@setPositiveButton
                            binding.webView.loadUrl(formUrlValue)
                        }
                        showURLDialog.show()
                        binding.webView.url?.let {
                            viewModel.formUrl.value = it
                        }
                    }
                    else
                        binding.webView.loadUrl("https://$domain")
                }
                selectDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}