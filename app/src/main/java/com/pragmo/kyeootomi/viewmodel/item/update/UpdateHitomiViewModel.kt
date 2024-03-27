package com.pragmo.kyeootomi.viewmodel.item.update

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item

class UpdateHitomiViewModel(application: Application) : UpdateItemViewModel(application) {

    /* RadioGroup databinding은 RadioButton ID로 하는데 그것도 모르고 enum class 정의해서 쓰다가 버그걸렸다.
     * 지능 수준봐라..
    enum class TitleOption(val radioValue: Int) {
        SET_AUTO(1), SET_CUSTOM(2)
    }
    enum class DownloadOption(val radioValue: Int) {
        RETRY(1), DELETE_FILE(2), NO_ACTION(3)
    }*/

    private val _hitomiItem = MutableLiveData<HitomiItem>()
    val hitomiItem : LiveData<HitomiItem> = _hitomiItem

    val number = MutableLiveData<Int>()
    val titleOpt = MutableLiveData(R.id.radioSetCustomTitle)
    val reloadInfo = MutableLiveData(false)
    val downloadOpt = MutableLiveData(R.id.radioNoActionDownload)

    override fun setItem(numItem : Int) {
        _hitomiItem.value = itemModel.getHitomi(numItem)
        val hitomiItemValue = _hitomiItem.value ?: return
        title.value = hitomiItemValue.title
        number.value = hitomiItemValue.number
    }

    override fun commit(onComplete: (Boolean) -> Unit) : Boolean {
        val hitomiItemValue = _hitomiItem.value ?: return false
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
        val reloadInfoValue = reloadInfo.value ?: return false
        val downloadOptValue = downloadOpt.value ?: return false
        if (downloadOptValue == 0)
            return false

        val itemValues = Item(Item.ItemType.HITOMI, hitomiItemValue._no, hitomiItemValue.collection, titleValue)
        val hitomiValues = HitomiItem(itemValues, hitomiItemValue.number, downloadOptValue == R.id.radioRetryDownload)
        itemModel.updateHitomi(hitomiItemValue._no, hitomiValues, useTitle,
            reloadInfoValue, downloadOptValue != R.id.radioNoActionDownload, onComplete)

        return true
    }
}