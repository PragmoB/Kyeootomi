package com.pragmo.kyeootomi.viewmodel.item.update

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.model.repository.ItemModel

abstract class UpdateItemViewModel(application: Application) : AndroidViewModel(application) {

    protected val itemModel = ItemModel(application)

    val title = MutableLiveData<String>()

    abstract fun setItem(numItem : Int)
    abstract fun commit(onComplete: (Boolean) -> Unit) : Boolean
}