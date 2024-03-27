package com.pragmo.kyeootomi.viewmodel.item.read

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.repository.ItemModel

class ReadHitomiViewModel(application: Application) : AndroidViewModel(application) {
    private val itemModel = ItemModel(application)

    private val _hitomiItem = MutableLiveData<HitomiItem>()
    val countPages = MutableLiveData<Int>()
    val numPage = MutableLiveData<Int>()

    val hitomiItem : LiveData<HitomiItem> = _hitomiItem

    fun loadItem(_no : Int) {
        _hitomiItem.value = itemModel.getHitomi(_no)
        numPage.value = 1
        countPages.value = _hitomiItem.value?.filesDir?.list()?.size?.let {
            if (it < 2) 2 else it
        } ?: 2
    }
    fun pageNext() {

        /* 작품 이미지 파일 개수 확인 후 반영(작품 다운로드 중 감상하는 경우를 고려) */

        val hitomiItemValue = _hitomiItem.value ?: return
        val countHitomiItemFiles = hitomiItemValue.filesDir.list()?.size ?: return
        if ((countPages.value ?: 2) < countHitomiItemFiles)
            countPages.value = countHitomiItemFiles

        /* 다음 이미지가 있는지 확인 후 페이지 넘기기 */

        val numPageValue = numPage.value?:return
        val cover = hitomiItemValue.getFile(numPageValue + 1)
        if (cover != null)
            numPage.value = numPageValue + 1
    }
    fun pagePrevious() {
        val numPageValue = numPage.value?:return
        if (numPageValue > 1)
            numPage.value = numPageValue - 1
    }
}