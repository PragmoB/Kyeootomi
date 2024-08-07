package com.pragmo.kyeootomi.view.activity.item.read

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityReadHitomiBinding
import com.pragmo.kyeootomi.view.activity.BaseActivity
import com.pragmo.kyeootomi.viewmodel.item.read.ReadHitomiViewModel
import java.io.File

class ReadHitomiActivity : BaseActivity() {

    private lateinit var viewModel : ReadHitomiViewModel
    private lateinit var binding : ActivityReadHitomiBinding
    private var menuIsShown = true

    private fun showMenu(show : Boolean) {
        menuIsShown = show
        if (show) {
            supportActionBar?.show()
            binding.wrapSlider.visibility = View.VISIBLE
        } else {
            supportActionBar?.hide()
            binding.wrapSlider.visibility = View.GONE
        }
    }
    private fun setManga(manga : File) {
        binding.imgManga.setImageURI(Uri.fromFile(manga))
        if (binding.imgManga.drawable == null)
            binding.wrapCoverError.visibility = View.VISIBLE
        else
            binding.wrapCoverError.visibility = View.GONE
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ReadHitomiViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_read_hitomi)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* 콜백 등록 */

        viewModel.hitomiItem.observe(this) {
            supportActionBar?.title = it.title
            val numPage = viewModel.numPage.value ?: return@observe
            val manga = it.getFile(numPage) ?: return@observe
            setManga(manga)
        }
        viewModel.numPage.observe(this) {
            val hitomiItem = viewModel.hitomiItem.value ?: return@observe
            val manga = hitomiItem.getFile(it.toInt()) ?: return@observe
            setManga(manga)
        }
        binding.txtCountPages.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            // txtNumPage의 너비를 txtCountPages의 너비에 맞추기
            val layoutParams = binding.txtNumPage.layoutParams
            layoutParams.width = right - left
            binding.txtNumPage.layoutParams = layoutParams
        }
        binding.sliderPage.addOnChangeListener { _, value, _ ->
            viewModel.numPage.value = value.toInt()
        }
        binding.imgManga.setOnPhotoTapListener { _, _, _ ->
            if (menuIsShown)
                showMenu(false)
            else
                viewModel.pageNext()
        }
        binding.imgManga.setOnOutsidePhotoTapListener {
            if (menuIsShown)
                showMenu(false)
            else
                showMenu(true)
        }
        binding.rootMain.setOnClickListener {
            if (menuIsShown)
                showMenu(false)
            else
                showMenu(true)
        }
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.pagePrevious()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)


        /* 타켓 히토미 작품 정보 설정 */

        viewModel.loadItem(intent.getIntExtra("_no", 0))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_read_hitomi, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuSaveCurrentManga -> {
                if (viewModel.copyToGallery())
                    Toast.makeText(this, "갤러리에 복사되었습니다", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "복사에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}