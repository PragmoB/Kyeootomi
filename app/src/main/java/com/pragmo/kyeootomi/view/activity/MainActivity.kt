package com.pragmo.kyeootomi.view.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityMainBinding
import com.pragmo.kyeootomi.databinding.DialogFormCollectionBinding
import com.pragmo.kyeootomi.view.adapter.ItemAdapter
import com.pragmo.kyeootomi.viewmodel.MainViewModel
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel : MainViewModel
    private lateinit var binding : ActivityMainBinding
    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.setCollection(null)

        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerDocument.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle = ActionBarDrawerToggle(this, binding.drawer,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        toggle.syncState()

        /* 레이아웃 조정 */

        binding.refreshDocument.setDistanceToTriggerSync(350)

        /* 이벤트 콜백 설정 */

        binding.refreshDocument.setOnRefreshListener {
            viewModel.loadItems()
            binding.refreshDocument.isRefreshing = false
        }
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.refreshDocument.isEnabled = binding.scrollView.scrollY == 0
        }
        binding.btnAddItem.setOnClickListener {
            val intentAddItem = Intent(this, AddItemActivity::class.java)
            intentAddItem.putExtra("numCollection", viewModel.collection.value!!.num)
            startActivity(intentAddItem)
        }
        binding.naviView.setNavigationItemSelectedListener(this)
        viewModel.listItem.observe(this) {

            /* 작품 리스트 렌더링 */

            if (it != null)
                binding.recyclerDocument.adapter = ItemAdapter(it)
        }
        viewModel.listCollection.observe(this) {

            /* 컬렉션 메뉴 렌더링 */

            binding.naviView.menu.clear()
            menuInflater.inflate(R.menu.menu_main_collection_upside, binding.naviView.menu)
            for (i in it.indices) {
                val collection = it[i]
                binding.naviView.menu.add(0, collection.num!!, 0, collection.name)
            }
            menuInflater.inflate(R.menu.menu_main_collection_downside, binding.naviView.menu)
            binding.naviView.postInvalidate()
        }
        viewModel.collection.observe(this) {
            val txtPath = binding.naviView.getHeaderView(0).findViewById<TextView>(R.id.txtPath)
            txtPath.text = viewModel.getPath().replace("/", " > ")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadItems()
        viewModel.loadCollection()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val startTransformIntent = Intent(this, DrawerTransformActivity::class.java)

        val formCollectionBinding = DialogFormCollectionBinding.inflate(layoutInflater)
        formCollectionBinding.lifecycleOwner = this
        formCollectionBinding.viewModel = viewModel

        val dlg = AlertDialog.Builder(this)
        dlg.setNegativeButton("취소", null)

        when (item.itemId) {
            R.id.menuRevertCollection -> {
                viewModel.revertCollection()
                startActivity(startTransformIntent) // 컬렉션 리스트 변경사항 출력을 위한 화면전환
            }
            R.id.menuAddCollection -> {
                formCollectionBinding.editCollectionName.requestFocus()
                formCollectionBinding.editCollectionName.postDelayed({
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(formCollectionBinding.editCollectionName, InputMethodManager.SHOW_IMPLICIT)
                }, 300)

                dlg.setView(formCollectionBinding.root)
                dlg.setTitle("컬렉션 추가")
                dlg.setPositiveButton("확인", viewModel.addCollectionListener)
                dlg.show()
            }
            R.id.menuUpdateCollection -> {
                val nowCollectionName = viewModel.collection.value!!.name

                // 루트 컬렉션은 이름 변경 불가능
                if (viewModel.collection.value!!.num == null) {
                    Toast.makeText(this, "내 컬렉션은 이름 변경이 불가능합니다", Toast.LENGTH_SHORT).show()
                    return false
                }

                // 컬렉션 입력 편의기능 세팅
                formCollectionBinding.editCollectionName.setText(nowCollectionName)
                formCollectionBinding.editCollectionName.requestFocus(nowCollectionName.length)
                formCollectionBinding.editCollectionName.postDelayed({
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(formCollectionBinding.editCollectionName, InputMethodManager.SHOW_IMPLICIT)
                }, 300)

                dlg.setView(formCollectionBinding.root)
                dlg.setTitle("컬렉션 이름 변경")
                dlg.setPositiveButton("확인", viewModel.updateCollectionListener)
                dlg.show()
            }
            R.id.menuDeleteCollection -> {
                dlg.setTitle("컬렉션 삭제")
                dlg.setMessage("남아있는 작품 및\n하위 컬렉션이 모두 삭제됩니다.\n${viewModel.collection.value!!.name} 을(를) 삭제하시겠습니까?")
                dlg.setPositiveButton("확인", viewModel.deleteCollectionListener)
                dlg.show()
            }

            /* 컬렉션 선택 */

            else -> {
                binding.drawer.close()
                viewModel.setCollection(item.itemId)
                startActivity(startTransformIntent) // 컬렉션 리스트 변경사항 출력을 위한 화면전환
            }
        }
        return true
    }
}