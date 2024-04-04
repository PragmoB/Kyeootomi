package com.pragmo.kyeootomi.view.activity.item

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 전달받은 값 설정 */

        val url = intent.getStringExtra("url") ?: "https://${Item.ItemType.HITOMI.domain}"

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

        val onLoad = object: WebViewClient() {
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
        binding.webView.webViewClient = onLoad
        binding.webView.settings.javaScriptEnabled = true

        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.webView.goBack()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        /* 시작 */

        binding.webView.loadUrl(url)
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
                    binding.webView.loadUrl("https://${domain.ifEmpty { "www.google.com" }}")
                }
                selectDialog.show()
            }
            R.id.menuShowURL -> {
                val dialogFormURlBinding = DialogFormUrlBinding.inflate(layoutInflater)
                dialogFormURlBinding.lifecycleOwner = this
                dialogFormURlBinding.viewModel = viewModel

                val showURLDialog = AlertDialog.Builder(this)
                showURLDialog.setTitle("URL 확인")
                showURLDialog.setView(dialogFormURlBinding.root)
                showURLDialog.setNegativeButton("취소", null)
                showURLDialog.setPositiveButton("복사"
                ) { _, _ ->
                    val formURLValue = viewModel.formUrl.value ?: return@setPositiveButton
                    if (formURLValue.isEmpty())
                        return@setPositiveButton

                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("url", formURLValue)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(this, "클립보드에 복사했어요", Toast.LENGTH_SHORT).show()
                }
                showURLDialog.show()

                viewModel.formUrl.value = binding.webView.url
            }
        }
        return super.onOptionsItemSelected(item)
    }
}