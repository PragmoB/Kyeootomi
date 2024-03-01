package com.pragmo.kyeootomi.model.repository

import android.content.ContentValues
import android.content.Context
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item

class ItemModel(private val context : Context) {
    fun addHitomi(item : HitomiItem, useTitle : Boolean) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        if (item.numCollection == null)
            values.putNull("collection")
        else
            values.put("collection", item.numCollection)
        values.put("title", if (useTitle) item.title else "auto")
        values.put("number", item.number)
        values.put("downloaded", item.downloaded)
        db.insert("HitomiItem", null, values)
        return true
    }
    fun addCustom(item : CustomItem) : Boolean {
        val db = ItemDBHelper(context).writableDatabase
        val values = ContentValues()
        if (item.numCollection == null)
            values.putNull("collection")
        else
            values.put("collection", item.numCollection)
        values.put("title", item.title?:"auto")
        values.put("URL", item.url)
        db.insert("CustomItem", null, values)
        return true
    }
    fun getHitomi(numCollection : Int?) : List<HitomiItem> {
        val db = ItemDBHelper(context).readableDatabase
        val listHitomi = mutableListOf<HitomiItem>()
        val cursor = db.query("HitomiItem", arrayOf<String>("title", "number", "downloaded", "date"),
            if (numCollection == null)
                "collection IS NULL"
            else
                "collection=?",
            if (numCollection == null)
                null
            else
                arrayOf(numCollection?.toString()),
            null, null, null)

        while (cursor.moveToNext()) {
            val item = Item("hitomi", numCollection, cursor.getString(0))
            val hitomiItem = HitomiItem(item, cursor.getInt(1), cursor.getInt(2) != 0)
            listHitomi.add(hitomiItem)
        }

        cursor.close()
        return listHitomi
    }
    fun getCustom(numCollection : Int?) : List<CustomItem> {
        val db = ItemDBHelper(context).readableDatabase
        val listCustom = mutableListOf<CustomItem>()
        val cursor = db.query("CustomItem", arrayOf<String>("title", "URL", "date"),
            if (numCollection == null)
                "collection IS NULL"
            else
                "collection=?",
            if (numCollection == null)
                null
            else
                arrayOf(numCollection?.toString()),
            null, null, null)

        while (cursor.moveToNext()) {
            val item = Item("custom", numCollection, cursor.getString(0))
            val customItem = CustomItem(item, cursor.getString(1))
            listCustom.add(customItem)
        }

        cursor.close()
        return listCustom
    }
    fun get(numCollection: Int?): List<Item> {
        return getHitomi(numCollection) + getCustom(numCollection)
    }
    fun delete(numItem: Int) {
        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem", "_no=?", arrayOf(numItem.toString()))
        db.delete("CustomItem", "_no=?", arrayOf(numItem.toString()))
    }
    fun deleteByCollection(numCollection: Int?) {
        val db = ItemDBHelper(context).writableDatabase
        db.delete("HitomiItem",
            if (numCollection == null)
                "collection IS NULL"
            else
                "collection=?",
            if (numCollection == null)
                null
            else
                arrayOf(numCollection?.toString()))
        db.delete("CustomItem",
            if (numCollection == null)
                "collection IS NULL"
            else
                "collection=?",
            if (numCollection == null)
                null
            else
                arrayOf(numCollection?.toString()))
    }
}