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
    private val _listSubCollections = MutableLiveData<List<Collection>>()
    private val _collection = MutableLiveData<Collection>()
    private val _formCollectionName = MutableLiveData<String>()

    val listItem : LiveData<List<Item>> = _listItem
    val listSubCollections : LiveData<List<Collection>> = _listSubCollections
    val collection : LiveData<Collection> = _collection
    val formCollectionName : LiveData<String> = _formCollectionName

    val addCollectionListener = DialogInterface.OnClickListener { _, _ ->
        val collectionValue = _collection.value ?: return@OnClickListener
        val formCollectionNameValue = _formCollectionName.value ?: return@OnClickListener

        val subCollection = Collection(0, collectionValue.num, formCollectionNameValue)
        collectionModel.add(subCollection)
        loadSubCollections()
        Toast.makeText(application, "추가되었습니다", Toast.LENGTH_SHORT).show()
    }
    val updateCollectionListener = DialogInterface.OnClickListener { _, _ ->
        val collectionValue = _collection.value ?: return@OnClickListener
        val formCollectionNameValue = _formCollectionName.value ?: return@OnClickListener

        collectionValue.num?.let { // 최상위 컬렉션은 이름 변경 불가능
            collectionModel.update(it, formCollectionNameValue)
            Toast.makeText(application, "변경되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
    val deleteCollectionListener = DialogInterface.OnClickListener { _, _ ->
        val collectionValue = _collection.value ?: return@OnClickListener

        collectionModel.delete(collectionValue.num)
        revertCollection()
    }

    fun onFormCollectionNameChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        _formCollectionName.value = s.toString()
    }
    fun loadItems() {
        val collectionValue = _collection.value?:return
        _listItem.value = itemModel.getByCollection(collectionValue.num)
    }
    fun loadSubCollections() {
        val collectionValue = _collection.value?:return
        _listSubCollections.value = collectionModel.getSubCollections(collectionValue.num)
    }
    fun setCollection(numCollection: Int?) {
        _collection.value = collectionModel.get(numCollection)
        loadItems()
        loadSubCollections()
    }
    fun getPath() : String {
        return collectionModel.getPath(_collection.value?.num)
    }
    fun revertCollection() {
        val collectionValue = _collection.value?:return
        setCollection(collectionValue.numParentCollection)
    }
}