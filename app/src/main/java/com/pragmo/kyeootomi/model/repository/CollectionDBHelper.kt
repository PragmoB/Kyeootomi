package com.pragmo.kyeootomi.model.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CollectionDBHelper(context : Context) : SQLiteOpenHelper(context, "collection_db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE Collection (" +
                    "_no INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "collection INTEGER," +
                    "name TEXT NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}