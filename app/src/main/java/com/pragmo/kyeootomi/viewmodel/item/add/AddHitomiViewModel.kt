package com.pragmo.kyeootomi.viewmodel.item.add

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item

class AddHitomiViewModel(application: Application): AddItemViewModel(application) {

    val titleOpt = MutableLiveData<Int>()
    val number = MutableLiveData<String>()
    val downloadOpt = MutableLiveData<Int>()

    override fun commit(onComplete: (Boolean) -> Unit) : Boolean {
        val collectionValue = collection.value ?: return false
        val titleOptValue = titleOpt.value ?: return false
        if (titleOptValue == 0)
            return false
        val useTitle = titleOptValue == R.id.radioSetCustomTitle
        val titleValue = if (useTitle)
            title.value ?: return false
        else
            "..."
        if (titleValue.isEmpty())
            return false
        val numberValue = number.value ?: return false
        if (numberValue.isEmpty())
            return false
        val downloadOpt = downloadOpt.value ?: return false
        if (downloadOpt == 0)
            return false
        val downloaded = downloadOpt == R.id.radioLocalDownload

        val item = Item(Item.ItemType.HITOMI, 0, collectionValue, titleValue)
        val hitomiItem = HitomiItem(item, numberValue.toInt(), downloaded)
        itemModel.addHitomi(hitomiItem, useTitle, onComplete)
        return true
    }
}