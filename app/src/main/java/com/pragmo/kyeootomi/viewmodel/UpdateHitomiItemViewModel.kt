package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.ItemModel

class UpdateHitomiItemViewModel(application: Application) : UpdateItemViewModel(application) {

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

    override fun commit() : Boolean {
        val hitomiItemValue = _hitomiItem.value ?: return false
        val titleOptValue = titleOpt.value ?: return false
        val titleValue = if (titleOptValue == R.id.radioSetAutoTitle)
            ""
        else
            title.value ?: return false
        val reloadInfoValue = reloadInfo.value ?: return false
        val downloadOptValue = downloadOpt.value ?: return false

        val itemValues = Item(Item.ItemType.HITOMI, hitomiItemValue._no, hitomiItemValue.collection, titleValue)
        val hitomiValues = HitomiItem(itemValues, hitomiItemValue.number, downloadOptValue == R.id.radioRetryDownload)
        val onComplete: (Boolean) -> Unit = { isSucceed ->
            if (isSucceed)
                Toast.makeText(getApplication(), "변경되었습니다", Toast.LENGTH_SHORT).show()
        }
        itemModel.updateHitomi(hitomiItemValue._no, hitomiValues, titleOptValue == R.id.radioSetCustomTitle,
            reloadInfoValue, downloadOptValue != R.id.radioNoActionDownload, onComplete)

        return true
    }
}