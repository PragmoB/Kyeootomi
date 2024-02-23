package com.pragmo.kyeootomi.model.repository

import android.content.ContentValues
import android.content.Context
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem

class AddItemModel(val context : Context) {
    fun commitHitomi(item : HitomiItem) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        values.put("collection", item.collection)
        values.put("title", item.title?:"auto")
        values.put("number", item.number)
        values.put("downloaded", item.downloaded)
        db.insert("HitomiItem", null, values)
        return true
    }
    fun commitCustom(item : CustomItem) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        values.put("collection", item.collection)
        values.put("title", item.title?:"auto")
        values.put("URL", item.url)
        db.insert("CustomItem", null, values)
        return true
    }
}