package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.CollectionModel
import com.pragmo.kyeootomi.model.repository.ItemModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val collectionModel = CollectionModel(application)
    private val itemModel = ItemModel(application)

    private val _listItemsWaitingToRelocate = MutableLiveData<MutableList<Item>?>(null)
    private val _listItems = MutableLiveData<MutableList<Item>>()
    private val _listSubCollections = MutableLiveData<List<Collection>>()
    private val _collection = MutableLiveData<Collection>()

    val listItemsWaitingToRelocate: LiveData<MutableList<Item>?> = _listItemsWaitingToRelocate
    val listItems : LiveData<MutableList<Item>> = _listItems
    val listSubCollections : LiveData<List<Collection>> = _listSubCollections
    val collection : LiveData<Collection> = _collection

    val formCollectionName = MutableLiveData<String>()

    fun addCollection(): Boolean {
        val collectionValue = _collection.value ?: return false
        val formCollectionNameValue = formCollectionName.value ?: return false
        if (formCollectionNameValue.isEmpty())
            return false

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
        if (formCollectionNameValue.isEmpty())
            return false

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
        val collectionValue = _collection.value ?: return

        // DB에서 작품 받아온 뒤, 컬렉션 변경 대기중인 작품만 제외하고 반영
        val results = itemModel.getByCollection(collectionValue.num).toMutableList()
        _listItems.value = results
    }
    // listItem의 작품을 listItemWaitingForMovement로 재배치
    fun requeueItemIntoWaiting(index: Int) {
        val listItemsValue = _listItems.value ?: return
        val listItemsWaitingToRelocateValue = _listItemsWaitingToRelocate.value ?: mutableListOf()

        val item = listItemsValue[index]
        listItemsWaitingToRelocateValue.add(item)
        listItemsValue.removeAt(index)
        _listItemsWaitingToRelocate.value = listItemsWaitingToRelocateValue

        itemModel.setReachable(item.type, item._no, false)
    }
    fun relocateItemsWaiting(onEachComplete: (Item, Boolean) -> Unit) {
        val listItemsWaitingToRelocateValue = _listItemsWaitingToRelocate.value ?: return
        val collectionValue = _collection.value ?: return

        itemModel.setReachableAll(true)

        for (itemWaitingToRelocate in listItemsWaitingToRelocateValue) {
            itemWaitingToRelocate.collection = collectionValue.copy()
            when (itemWaitingToRelocate) {
                is HitomiItem -> {
                    itemModel.updateHitomi(itemWaitingToRelocate._no, itemWaitingToRelocate,
                        true, false, false
                    ) {
                        onEachComplete(itemWaitingToRelocate, it)
                    }
                }
                is CustomItem -> {
                    itemModel.updateCustom(itemWaitingToRelocate._no, itemWaitingToRelocate)
                    onEachComplete(itemWaitingToRelocate, true)
                }
            }
        }
        resetItemsWaiting()
    }
    fun resetItemsWaiting() {
        itemModel.setReachableAll(true)
        _listItemsWaitingToRelocate.value = null
    }
    fun insertItem(index: Int, item: Item) {
        val itemsValue = _listItems.value ?: return

        itemsValue.add(index, item)
    }
    fun copyItemToGallery(index: Int) {
        val itemsValue = _listItems.value ?: return
        itemModel.copyToGallery(itemsValue[index])
    }
    fun deleteItem(index: Int) {
        val itemsValue = _listItems.value ?: return

        val item = itemsValue[index]
        itemModel.delete(item.type, item)
        itemsValue.removeAt(index)
    }

    fun setCollection(numCollection: Int?) {
        _collection.value = collectionModel.get(numCollection)
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