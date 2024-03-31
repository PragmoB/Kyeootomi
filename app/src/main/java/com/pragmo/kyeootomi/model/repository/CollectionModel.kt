package com.pragmo.kyeootomi.model.repository

import android.content.ContentValues
import android.content.Context
import com.pragmo.kyeootomi.model.data.Collection
import java.io.File

class CollectionModel(private val context : Context) {

    /*  numCollection번 컬렉션에 collectionName인 하위 컬렉션 생성 */

    fun add(collection: Collection) {

        /* DB에 등록 */

        val db = CollectionDBHelper(context).writableDatabase
        val values = ContentValues()
        collection.numParentCollection?.let {
            values.put("collection", it)
        } ?: values.putNull("collection")
        values.put("name", collection.name)
        db.insert("Collection", null, values)
    }

    /* numCollection번 컬렉션의 모든 하위 컬렉션 조회 */

    fun getSubCollections(numCollection : Int?) : List<Collection> {
        val db = CollectionDBHelper(context).readableDatabase
        val listCollection = mutableListOf<Collection>()
        val cursor = db.query("Collection", arrayOf("_no", "name"),
                if (numCollection == null) "collection IS NULL"
                else "collection=?",
                if (numCollection == null) null
                else arrayOf(numCollection.toString()),
            null, null, null)

        while (cursor.moveToNext())
            listCollection.add(Collection(cursor.getInt(0), numCollection,
                cursor.getString(1)))

        cursor.close()
        return listCollection.toList()
    }

    /* numCollection번 컬렉션 정보 조회 */

    fun get(numCollection: Int?) : Collection? {
        if (numCollection == null)
            return Collection(null, null, "내 컬렉션")

        val db = CollectionDBHelper(context).readableDatabase
        val cursor = db.query("Collection", arrayOf("collection", "name"),
            "_no=?", arrayOf(numCollection.toString()), null, null, null)

        val ret = if (cursor.moveToNext())
            Collection(numCollection,
                if (cursor.isNull(0)) null else cursor.getInt(0),
                cursor.getString(1))
        else
            null

        cursor.close()
        return ret
    }

    /* numCollection번 컬렉션의 경로 조회 */

    fun getPath(numCollection: Int?) : String {
        val db = CollectionDBHelper(context).readableDatabase
        var result = ""
        var num = numCollection

        while (num != null) {
            val cursor = db.query("Collection", arrayOf("collection", "name"), "_no=?",
                arrayOf(num.toString()), null, null, null)
            if (!cursor.moveToNext())
                return "error loading path"

            result = "/" + cursor.getString(1) + result
            num = if (cursor.isNull(0))
                null
            else
                cursor.getInt(0)
            cursor.close()
        }
        return "내 컬렉션$result"
    }

    /* numCollection번 컬렉션의 이름 name로 변경 */

    fun update(collection : Collection, name : String) {
        collection.num ?: return

        val db = CollectionDBHelper(context).writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.update("Collection", values, "_no=?", arrayOf(collection.num.toString()))
    }

    /* numCollection번 컬렉션 및 하위 컬렉션, 하위 작품 삭제 */

    fun delete(collection: Collection) {
        // numCollection번 컬렉션 및 numCollection번 컬렉션의 작품 db 데이터 삭제
        val db = CollectionDBHelper(context).writableDatabase
        val itemModel = ItemModel(context)
        if (collection.num != null)
            db.delete("Collection", "_no=?", arrayOf(collection.num.toString()))
        itemModel.deleteByCollection(collection.num)

        // numCollection번 컬렉션의 하위 컬렉션 모두 삭제
        val subCollections = getSubCollections(collection.num)
        for (subCollection in subCollections)
            delete(subCollection)

        return
    }
}