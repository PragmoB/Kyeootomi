package com.pragmo.kyeootomi.viewmodel.item.add

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.model.data.Collection
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.CollectionModel
import com.pragmo.kyeootomi.model.repository.ItemModel

/* 뷰모델에서의 Context참조는 application과 AndroidViewModel을 이용한다고 함
 * 이유는 뷰모델과 액티비티의 lifecycle이 달라서 */

open class AddItemViewModel(application: Application): AndroidViewModel(application) {

    private val collectionModel = CollectionModel(application)
    protected val itemModel = ItemModel(application)

    private val _collection = MutableLiveData<Collection>()
    val collection : LiveData<Collection> = _collection

    val contentsProviderOrdinal = MutableLiveData<Int>()
    val title = MutableLiveData<String>()

    fun setCollection(numCollection: Int?) {
        _collection.value = collectionModel.get(numCollection)
    }
    open fun commit(onComplete: (Boolean) -> Unit) : Boolean {
        _collection.value ?: return false
        contentsProviderOrdinal.value ?: return false
        val titleValue = title.value ?: return false
        if (titleValue.isEmpty())
            return false

        return true
    }
}