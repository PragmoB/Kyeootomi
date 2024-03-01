package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.ItemModel

/* 뷰모델에서의 Context참조는 application과 AndroidViewModel을 이용한다고 함
 * 이유는 뷰모델과 액티비티의 lifecycle이 달라서 */

class AddItemViewModel(application: Application): AndroidViewModel(application) {

    private val itemModel = ItemModel(application)

    private val _numCollection = MutableLiveData<Int>()
    private val _contentsProvider = MutableLiveData<String>()
    private val _title = MutableLiveData<String>()

    val numCollection : LiveData<Int> = _numCollection
    val contentsProvider : LiveData<String> = _contentsProvider
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
        this._contentsProvider.value =
            getApplication<Application>().
            resources.getStringArray(R.array.contents_providers)[position]
    }
    fun setUseTitle(useTitle : Boolean) {
        this._useTitle.value = useTitle
    }
    fun setCollection(numCollection : Int?) {
        this._numCollection.value = numCollection
    }
    fun onTitleChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this._title.value = s.toString()
    }
    fun onNumberChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this._number.value = s.toString().toInt()
    }
    fun onUrlChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this._url.value = s.toString()
    }
    fun setDownloaded(downloaded : Boolean) {
        this._downloaded.value = downloaded
    }

    fun commit() : Boolean {
        when (_contentsProvider.value) {
            "hitomi" -> {
                val item = Item("hitomi", _numCollection.value, _title.value?:"")
                val hitomiItem = HitomiItem(
                    item,
                    _number.value?:return false,
                    _downloaded.value?:return false)
                return itemModel.addHitomi(hitomiItem, useTitle.value?:return false)
            }
            "custom" -> {
                val item = Item("custom", _numCollection.value, title.value?:return false)
                val customItem = CustomItem(
                    item,
                    url.value?:return false)
                return itemModel.addCustom(customItem)
            }
            else -> return false
        }
    }
}