package com.pragmo.kyeootomi.model.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ItemDBHelper(context: Context) : SQLiteOpenHelper(context, "item_db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE HitomiItem (" +
                    "_no INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "collection INT," +
                    "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "title TEXT NOT NULL," +
                    "number INTEGER NOT NULL," +
                    "downloaded INTEGER NOT NULL)"
        )
        db?.execSQL(
            "CREATE TABLE CustomItem (" +
                    "_no INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "collection INT," +
                    "date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "title TEXT NOT NULL," +
                    "URL TEXT NOT NULL)"
        )
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}