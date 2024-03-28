package com.pragmo.kyeootomi.viewmodel.item.update

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.Item

class UpdateCustomViewModel(application: Application) : UpdateItemViewModel(application) {

    private val _customItem = MutableLiveData<CustomItem>()
    val customItem: LiveData<CustomItem> = _customItem

    val url = MutableLiveData<String>()

    override fun setItem(numItem: Int) {
        _customItem.value = itemModel.getCustom(numItem)
        val customItemValue = _customItem.value ?: return
        title.value = customItemValue.title
        url.value = customItemValue.url
    }
    override fun commit(onComplete: (Boolean) -> Unit): Boolean {
        val customItemValue = _customItem.value ?: return false
        val titleValue = title.value ?: return false
        if (titleValue.isEmpty())
            return false
        val urlValue = url.value ?: return false
        if (urlValue.isEmpty())
            return false

        val itemValues = Item(Item.ItemType.CUSTOM, customItemValue._no, customItemValue.collection, titleValue)
        val customValues = CustomItem(itemValues, urlValue)
        itemModel.updateCustom(customItemValue._no, customValues)
        onComplete(true)
        return true
    }
}