package com.pragmo.kyeootomi.view.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityMainBinding
import com.pragmo.kyeootomi.databinding.DialogFormCollectionBinding
import com.pragmo.kyeootomi.view.activity.item.AddItemActivity
import com.pragmo.kyeootomi.view.activity.item.UpdateItemActivity
import com.pragmo.kyeootomi.view.adapter.ItemAdapter
import com.pragmo.kyeootomi.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel : MainViewModel
    private lateinit var binding : ActivityMainBinding
    private lateinit var menu: Menu
    private lateinit var toggle : ActionBarDrawerToggle

    private fun setSelectMode(selectMode: Boolean) {
        val itemAdapter = binding.recyclerDocument.adapter as ItemAdapter

        if (selectMode) {
            itemAdapter.selectMode = true
            if (!menu.hasVisibleItems() && ::menu.isInitialized)
                menuInflater.inflate(R.menu.menu_main_item_select, menu)
            binding.btnAddItem.visibility = View.GONE
            binding.refreshDocument.isEnabled = false
        } else {
            if (::menu.isInitialized)
                menu.clear()
            itemAdapter.selectMode = false
            binding.btnAddItem.visibility = View.VISIBLE
            binding.refreshDocument.isEnabled = true
        }
    }
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
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val itemAdapter = binding.recyclerDocument.adapter as ItemAdapter
                if (itemAdapter.selectMode) {
                    setSelectMode(false)
                } else
                    finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        val onLongClickItemListener: (ItemAdapter) -> Unit = { // 작품 길게 누를때
            setSelectMode(true)
        }
        viewModel.listItem.observe(this) {

            /* 작품 리스트 렌더링 */

            if (it != null)
                binding.recyclerDocument.adapter = ItemAdapter(it, false, onLongClickItemListener)

            setSelectMode(false)
        }
        viewModel.listSubCollections.observe(this) {

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
            val txtTitle = binding.naviView.getHeaderView(0).findViewById<TextView>(R.id.txtTitle)
            txtTitle.text = it.name
            val txtPath = binding.naviView.getHeaderView(0).findViewById<TextView>(R.id.txtPath)
            txtPath.text = viewModel.getPath().replace("/", " > ")
        }

        viewModel.loadItems()
        viewModel.loadSubCollections()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }
    private val onUpdateItemResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intentResult = it.data ?: return@registerForActivityResult
        // '완료'버튼 클릭으로 끝났다면
        if (intentResult.getStringExtra("result") == "complete")
            setSelectMode(false)
    }
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(menuItem))
            return true

        // 선택된 작품 항목의 인덱스 값들을 selectedItemIndex에 추출
        val listItemValue = viewModel.listItem.value ?: return false
        val selectedItemIndexes = mutableListOf<Int>()
        val itemAdapter = binding.recyclerDocument.adapter as ItemAdapter
        for (i in listItemValue.indices) {
            if (itemAdapter.getItemChecked(i))
                selectedItemIndexes.add(i)
        }
        when(menuItem.itemId) {
            R.id.menuSelectAll -> {
                itemAdapter.selectAll(selectedItemIndexes.size != listItemValue.size)
            }
            R.id.menuUpdateItem -> { // 작품 업데이트
                // 업데이트할 작품은 하나여야 함
                if (selectedItemIndexes.size != 1) {
                    Toast.makeText(this, "하나만 선택해주세요", Toast.LENGTH_SHORT).show()
                    return false
                }

                // 데이터 셋팅 후 업데이트 액티비티 시작
                val item = listItemValue[selectedItemIndexes[0]]
                val intentUpdateItem = Intent(this, UpdateItemActivity::class.java)
                intentUpdateItem.putExtra("numItem", item._no)
                intentUpdateItem.putExtra("itemType", item.type)
                onUpdateItemResultLauncher.launch(intentUpdateItem)
            }
            R.id.menuDeleteItem -> { // 작품 삭제
                if (selectedItemIndexes.size < 1) {
                    Toast.makeText(this, "하나 이상 선택해주세요", Toast.LENGTH_SHORT).show()
                    return false
                }

                val dlg = AlertDialog.Builder(this)
                dlg.setTitle("작품 삭제")
                dlg.setMessage("선택하신 작품 ${selectedItemIndexes.size}개 항목을 삭제합니다.\n계속하시겠습니까?")
                dlg.setNegativeButton("취소", null)
                dlg.setPositiveButton("확인") { _, _ ->
                    selectedItemIndexes.sortDescending()
                    for (selectedItemIndex in selectedItemIndexes) {
                        viewModel.deleteItem(selectedItemIndex)
                        itemAdapter.deleteItem(selectedItemIndex)
                    }
                    Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
                dlg.show()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val formCollectionBinding = DialogFormCollectionBinding.inflate(layoutInflater)
        formCollectionBinding.lifecycleOwner = this
        formCollectionBinding.viewModel = viewModel

        val collectionValue = viewModel.collection.value ?: return false

        val dlg = AlertDialog.Builder(this)
        dlg.setNegativeButton("취소", null)

        when (item.itemId) {
            R.id.menuRevertCollection -> {
                // onNavigationItemSelected 콜백 실행 시점에 naviView.menu.clear()가 안먹는 문제가 있었음.
                // 안드로이드 초짜라서 처음엔 액티비티를 껏다켜서 onResume() 상태를 만드는 방법으로 어떻게든 굴러가게 만들었는데,
                // Handler.post라는 좋은 방법을 알게되었다
                Handler(mainLooper).post {
                    viewModel.revertCollection()
                }
            }
            R.id.menuAddCollection -> {
                formCollectionBinding.editCollectionName.requestFocus()
                formCollectionBinding.editCollectionName.postDelayed({
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(formCollectionBinding.editCollectionName, InputMethodManager.SHOW_IMPLICIT)
                }, 300)

                dlg.setView(formCollectionBinding.root)
                dlg.setTitle("컬렉션 추가")
                dlg.setPositiveButton("확인") { _, _ ->
                    if (viewModel.addCollection()) {
                        viewModel.loadSubCollections()
                        Toast.makeText(this, "추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                dlg.show()
            }
            R.id.menuUpdateCollection -> {

                // 루트 컬렉션은 이름 변경 불가능
                if (collectionValue.num == null) {
                    Toast.makeText(this, "내 컬렉션은 이름 변경이 불가능합니다", Toast.LENGTH_SHORT).show()
                    return false
                }

                // 컬렉션 입력 편의기능 세팅
                formCollectionBinding.editCollectionName.setText(collectionValue.name)
                formCollectionBinding.editCollectionName.requestFocus(collectionValue.name.length)
                formCollectionBinding.editCollectionName.postDelayed({
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(formCollectionBinding.editCollectionName, InputMethodManager.SHOW_IMPLICIT)
                }, 300)

                dlg.setView(formCollectionBinding.root)
                dlg.setTitle("컬렉션 이름 변경")
                dlg.setPositiveButton("확인") { _, _ ->
                    if (viewModel.updateCollection())
                        Toast.makeText(this, "변경되었습니다", Toast.LENGTH_SHORT).show()
                }
                dlg.show()
            }
            R.id.menuDeleteCollection -> {
                dlg.setTitle("컬렉션 삭제")
                dlg.setMessage("남아있는 작품 및\n하위 컬렉션이 모두 삭제됩니다.\n${collectionValue.name}을(를) 삭제하시겠습니까?")
                dlg.setPositiveButton("확인") { _, _ ->
                    viewModel.deleteCollection()
                    viewModel.revertCollection()
                }
                dlg.show()
            }

            /* 컬렉션 선택 */

            else -> {
                binding.drawer.close()
                Handler(mainLooper).post {
                    viewModel.setCollection(item.itemId)
                }
            }
        }
        return true
    }
}