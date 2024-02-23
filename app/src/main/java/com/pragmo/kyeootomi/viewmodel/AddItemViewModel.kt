package com.pragmo.kyeootomi.viewmodel

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.repository.AddItemModel

/* 뷰모델에서의 Context참조는 application과 AndroidViewModel을 이용한다고 함
 * 이유는 뷰모델과 액티비티의 lifecycle이 달라서 */

class AddItemViewModel(application: Application): AndroidViewModel(application) {

    private val addItemModel = AddItemModel(application)

    val contentsProvider = MutableLiveData<String>(null)
    val title = MutableLiveData<String>(null)

    // hitomi
    val useTitle = MutableLiveData<Boolean>(null)
    val number = MutableLiveData<Int>(null)
    val downloaded = MutableLiveData<Boolean>(null)

    // custom
    val url = MutableLiveData<String>(null)

    fun onContentsProviderSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this.contentsProvider.value =
            getApplication<Application>().
            resources.getStringArray(R.array.contents_providers)[position]
    }
    fun setUseTitle(useTitle : Boolean) {
        this.useTitle.value = useTitle
    }
    fun onTitleChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this.title.value = s.toString()
    }
    fun onNumberChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this.number.value = s.toString().toInt()
    }
    fun onUrlChanged(s : CharSequence, start : Int, before : Int, count : Int) {
        this.url.value = s.toString()
    }
    fun setDownloaded(downloaded : Boolean) {
        this.downloaded.value = downloaded
    }

    fun commit() : Boolean {
        when (contentsProvider.value) {
            "hitomi" -> {
                val hitomiItem = HitomiItem(
                    "",
                    if(useTitle.value?:return false)
                        title.value?:return false
                    else
                        null,
                    number.value?:return false,
                    downloaded.value?:return false)
                return addItemModel.commitHitomi(hitomiItem)
            }
            "custom" -> {
                val customItem = CustomItem(
                    "",
                    title.value?:return false,
                    url.value?:return false)
                return addItemModel.commitCustom(customItem)
            }
            else -> return false
        }
    }
}