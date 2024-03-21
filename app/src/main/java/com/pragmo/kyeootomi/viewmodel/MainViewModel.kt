package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.CollectionModel
import com.pragmo.kyeootomi.model.repository.ItemModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val collectionModel = CollectionModel(application)
    private val itemModel = ItemModel(application)

    private val _listItem = MutableLiveData<List<Item>>()
    private val _listCollection = MutableLiveData<List<Collection>>()
    private val _collection = MutableLiveData<Collection>()
    private val _formCollectionName = MutableLiveData<String>()

    val listItem : LiveData<List<Item>> = _listItem
    val listCollection : LiveData<List<Collection>> = _listCollection
    val collection : LiveData<Collection> = _collection
    val formCollectionName : LiveData<String> = _formCollectionName

    val addCollectionListener = DialogInterface.OnClickListener { _, _ ->
        collectionModel.add((_collection.value?:return@OnClickListener).num,
            _formCollectionName.value?:return@OnClickListener)
        loadCollection()
        Toast.makeText(application, "추가되었습니다", Toast.LENGTH_SHORT).show()
    }
    val updateCollectionListener = DialogInterface.OnClickListener { _, _ ->
        if (_collection.value!!.num == null) // 최상위 컬렉션은 이름 변경 불가능
            return@OnClickListener

        collectionModel.update(_collection.value!!.num!!, _formCollectionName.value?:return@OnClickListener)
        Toast.makeText(application, "변경되었습니다", Toast.LENGTH_SHORT).show()
    }
    val deleteCollectionListener = DialogInterface.OnClickListener { _, _ ->
        if (_collection.value == null)
            return@OnClickListener

        collectionModel.delete(_collection.value!!.num)
        revertCollection()
    }

    fun onFormCollectionNameChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        _formCollectionName.value = s.toString()
    }
    fun loadItems() {
        val collection = _collection.value?:return
        _listItem.value = itemModel.getByCollection(collection.num)
    }
    fun loadCollection() {
        val collection = _collection.value?:return
        _listCollection.value = collectionModel.getSubCollections(collection.num)
    }
    fun setCollection(numCollection: Int?) {
        _collection.value = collectionModel.get(numCollection)
        loadItems()
        loadCollection()
    }
    fun getPath() : String {
        return collectionModel.getPath(_collection.value?.num)
    }
    fun revertCollection() {
        val collection = _collection.value?:return
        setCollection(collection.numTopCollection)
    }
}