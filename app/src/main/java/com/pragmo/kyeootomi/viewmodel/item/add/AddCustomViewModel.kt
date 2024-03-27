package com.pragmo.kyeootomi.viewmodel.item.add

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.Item

class AddCustomViewModel(application: Application): AddItemViewModel(application) {

    val url = MutableLiveData<String>()

    override fun commit(onComplete: (Boolean) -> Unit) : Boolean {
        val collectionValue = collection.value ?: return false
        val titleValue = title.value ?: return false
        if (titleValue.isEmpty())
            return false
        val urlValue = url.value ?: return false
        if (urlValue.isEmpty())
            return false

        val item = Item(Item.ItemType.CUSTOM, 0, collectionValue, titleValue)
        val customItem = CustomItem(item, urlValue)

        onComplete(itemModel.addCustom(customItem))
        return true
    }
}