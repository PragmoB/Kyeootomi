package com.pragmo.kyeootomi.view.activity

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
import com.pragmo.kyeootomi.view.fragment.UpdateHitomiFragment
import com.pragmo.kyeootomi.viewmodel.AddItemViewModel
import com.pragmo.kyeootomi.viewmodel.UpdateHitomiItemViewModel
import com.pragmo.kyeootomi.viewmodel.UpdateItemViewModel

class UpdateItemActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUpdateItemBinding
    private lateinit var viewModel : UpdateItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("itemType", Item.ItemType::class.java) ?: return
        } else {
            (intent.getSerializableExtra("itemType") ?: return) as Item.ItemType
        }
        val numItem = intent.getIntExtra("numItem", -1)
        if (numItem == -1)
            return

        viewModel = when (itemType) {
            Item.ItemType.HITOMI -> ViewModelProvider(this)[UpdateHitomiItemViewModel::class.java]
            else -> null!!
        }
        val fragment = when (itemType) {
            Item.ItemType.HITOMI -> UpdateHitomiFragment.newInstance()
            else -> null!!
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentUpdateInput, fragment).commit()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_item)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        when (itemType) {
            Item.ItemType.HITOMI -> supportActionBar?.title = "히토미 작품 수정"
            Item.ItemType.CUSTOM -> supportActionBar?.title = "작품 수정"
            else -> null!!
        }

        /* 콜백 등록 */

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
        when(item.itemId) {
            R.id.menuComplete -> {
                if (viewModel.commit()) {
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