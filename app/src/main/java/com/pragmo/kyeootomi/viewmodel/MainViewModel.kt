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

    private val _listItem = MutableLiveData<MutableList<Item>>()
    private val _listSubCollections = MutableLiveData<List<Collection>>()
    private val _collection = MutableLiveData<Collection>()

    val listItem : LiveData<MutableList<Item>> = _listItem
    val listSubCollections : LiveData<List<Collection>> = _listSubCollections
    val collection : LiveData<Collection> = _collection

    val formCollectionName = MutableLiveData<String>()

    fun addCollection(): Boolean {
        val collectionValue = _collection.value ?: return false
        val formCollectionNameValue = formCollectionName.value ?: return false

        val subCollection = Collection(0, collectionValue.num, formCollectionNameValue)
        collectionModel.add(subCollection)
        return true
    }
    fun loadSubCollections() {
        val collectionValue = _collection.value ?: return
        _listSubCollections.value = collectionModel.getSubCollections(collectionValue.num)
    }
    fun updateCollection(): Boolean {
        val collectionValue = _collection.value ?: return false
        val formCollectionNameValue = formCollectionName.value ?: return false

        if (collectionValue.num == null) // 최상위 컬렉션은 이름 변경 불가능
            return false

        collectionModel.update(collectionValue, formCollectionNameValue)
        return true
    }
    fun deleteCollection() {
        val collectionValue = _collection.value ?: return
        collectionModel.delete(collectionValue)
    }

    fun loadItems() {
        val collectionValue = _collection.value?:return
        _listItem.value = itemModel.getByCollection(collectionValue.num).toMutableList()
    }
    fun deleteItem(index: Int) {
        val listItemValue = _listItem.value ?: return

        val item = listItemValue[index]
        itemModel.delete(item.type, item)
        listItemValue.removeAt(index)
    }
    fun setCollection(numCollection: Int?) {
        _collection.value = collectionModel.get(numCollection)
        loadItems()
        loadSubCollections()
    }
    fun revertCollection() {
        val collectionValue = _collection.value?:return
        setCollection(collectionValue.numParentCollection)
    }
    fun getPath() : String {
        return collectionModel.getPath(_collection.value?.num)
    }
}