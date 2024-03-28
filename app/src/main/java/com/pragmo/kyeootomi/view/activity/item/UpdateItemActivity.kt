package com.pragmo.kyeootomi.view.activity.item

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityUpdateItemBinding
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.fragment.item.update.UpdateCustomFragment
import com.pragmo.kyeootomi.view.fragment.item.update.UpdateHitomiFragment
import com.pragmo.kyeootomi.viewmodel.item.update.UpdateCustomViewModel
import com.pragmo.kyeootomi.viewmodel.item.update.UpdateHitomiViewModel
import com.pragmo.kyeootomi.viewmodel.item.update.UpdateItemViewModel

class UpdateItemActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUpdateItemBinding
    private lateinit var viewModel : UpdateItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 넘어온 데이터 받아내기 */

        val itemType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("itemType", Item.ItemType::class.java) ?: return
        } else {
            (intent.getSerializableExtra("itemType") ?: return) as Item.ItemType
        }
        val numItem = intent.getIntExtra("numItem", -1)
        if (numItem == -1)
            return
        //
        viewModel = when (itemType) {
            Item.ItemType.HITOMI -> ViewModelProvider(this)[UpdateHitomiViewModel::class.java]
            Item.ItemType.CUSTOM -> ViewModelProvider(this)[UpdateCustomViewModel::class.java]
            else -> null!!
        }
        val fragment = when (itemType) {
            Item.ItemType.HITOMI -> UpdateHitomiFragment.newInstance()
            Item.ItemType.CUSTOM -> UpdateCustomFragment.newInstance()
            else -> null!!
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentUpdateInput, fragment).commit()

        /* 바인딩 설정 */

        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_item)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        when (itemType) {
            Item.ItemType.HITOMI -> supportActionBar?.title = "히토미 작품 수정"
            Item.ItemType.CUSTOM -> supportActionBar?.title = "작품 수정"
            else -> null!!
        }

        //
        viewModel.setItem(numItem)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_update_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val onCommitComplete: (Boolean) -> Unit = { isSucceed ->
            if (isSucceed)
                Toast.makeText(this, "변경되었습니다", Toast.LENGTH_SHORT).show()
        }
        when(item.itemId) {
            R.id.menuComplete -> {
                if (viewModel.commit(onCommitComplete)) {
                    Toast.makeText(this, "변경 중 입니다 잠시만 기다려주세요", Toast.LENGTH_SHORT).show()
                    intent.putExtra("result", "complete")
                    setResult(RESULT_OK, intent)
                    finish()
                }
                else
                    Toast.makeText(this, "남아있는 입력 폼을 작성해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}