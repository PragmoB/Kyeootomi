package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.CollectionModel
import com.pragmo.kyeootomi.model.repository.ItemModel

/* 뷰모델에서의 Context참조는 application과 AndroidViewModel을 이용한다고 함
 * 이유는 뷰모델과 액티비티의 lifecycle이 달라서 */

class AddItemViewModel(application: Application): AndroidViewModel(application) {

    private val collectionModel = CollectionModel(application)
    private val itemModel = ItemModel(application)

    private val _collection = MutableLiveData<Collection>()
    private val _contentsProvider = MutableLiveData<Item.ItemType>()
    private val _title = MutableLiveData<String>()

    val collection : LiveData<Collection> = _collection
    val contentsProvider : LiveData<Item.ItemType> = _contentsProvider
    val title : LiveData<String> = _title

    // hitomi
    private val _useTitle = MutableLiveData<Boolean>()
    private val _number = MutableLiveData<Int>()
    private val _downloaded = MutableLiveData<Boolean>()

    val useTitle : LiveData<Boolean> = _useTitle
    val number : LiveData<Int> = _number
    val downloaded : LiveData<Boolean> = _downloaded

    // custom
    private val _url = MutableLiveData<String>()
    val url : LiveData<String> = _url

    fun onContentsProviderSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this._contentsProvider.value = when (position) {
            Item.ItemType.HITOMI.ordinal -> Item.ItemType.HITOMI
            Item.ItemType.CUSTOM.ordinal -> Item.ItemType.CUSTOM
            else -> null!!
        }
    }
    fun setUseTitle(useTitle : Boolean) {
        this._useTitle.value = useTitle
    }
    fun setCollection(numCollection : Int?) {
        this._collection.value = collectionModel.get(numCollection)
    }
    fun onTitleChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        if (s.isEmpty())
            this._title.value = null
        else
            this._title.value = s.toString()
    }
    fun onNumberChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        if (s.isEmpty())
            this._number.value = null
        else
            this._number.value = s.toString().toInt()
    }
    fun onUrlChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        if (s.isEmpty())
            this._url.value = null
        else
            this._url.value = s.toString()
    }
    fun setDownloaded(downloaded : Boolean) {
        this._downloaded.value = downloaded
    }

    fun commit() : Boolean {
        val onCommitComplete : (Boolean) -> Unit = { isSucceed ->
            if (isSucceed)
                Toast.makeText(getApplication(), "추가되었습니다", Toast.LENGTH_SHORT).show()
        }
        when (_contentsProvider.value) {
            Item.ItemType.HITOMI -> {
                val useTitleValue = _useTitle.value ?: return false
                val titleValue = if (useTitleValue)
                    _title.value ?: return false
                else
                    ""
                val collectionValue = _collection.value ?: return false
                val numberValue = _number.value ?: return false
                val downloadValue = _downloaded.value ?: return false

                val item = Item(Item.ItemType.HITOMI, 0, collectionValue, titleValue)
                val hitomiItem = HitomiItem(
                    item,
                    numberValue,
                    downloadValue)
                itemModel.addHitomi(hitomiItem, useTitleValue, onCommitComplete)
                return true
            }
            Item.ItemType.CUSTOM -> {
                val collectionValue = _collection.value ?: return false
                val titleValue = _title.value ?: return false
                val urlValue = _url.value ?: return false

                val item = Item(Item.ItemType.CUSTOM, 0, collectionValue, titleValue)
                val customItem = CustomItem(item, urlValue)
                return itemModel.addCustom(customItem)
            }
            else -> return false
        }
    }
}