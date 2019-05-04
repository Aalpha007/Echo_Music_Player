package com.project.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.project.echo.Songs
import com.project.echo.databases.EchoDatabase.Staticated.COLUMN_ID
import com.project.echo.databases.EchoDatabase.Staticated.COLUMN_SONG_ARTIST
import com.project.echo.databases.EchoDatabase.Staticated.COLUMN_SONG_PATH
import com.project.echo.databases.EchoDatabase.Staticated.COLUMN_SONG_TITLE
import com.project.echo.databases.EchoDatabase.Staticated.DB_NAME
import com.project.echo.databases.EchoDatabase.Staticated.DB_VERSION
import com.project.echo.databases.EchoDatabase.Staticated.TABLE_NAME
import java.lang.Exception

class EchoDatabase: SQLiteOpenHelper{

    var _songlist = ArrayList<Songs>()

    object Staticated{
        var DB_VERSION = 1
        val DB_NAME = "FavoriteDatabase"
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
    }
    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL("CREATE TABLE " + TABLE_NAME + "( " + COLUMN_ID + " INTEGER, " + COLUMN_SONG_ARTIST + " STRING, " +
                COLUMN_SONG_TITLE + " STRING, " + COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(
        context,
        name,
        factory,
        version
    )

    constructor(context: Context?):super(context, DB_NAME, null, DB_VERSION)

    fun storeAsFavorite(id: Int?, artist: String?, songTitle: String?, path: String?){
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(COLUMN_ID, id)
        contentValues.put(COLUMN_SONG_ARTIST, artist)
        contentValues.put(COLUMN_SONG_TITLE, songTitle)
        contentValues.put(COLUMN_SONG_PATH, path)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>?{
        try {
            val db= this.readableDatabase
            val query_params = "SELECT * FROM " + TABLE_NAME
            val cSor = db.rawQuery(query_params, null)
            if (cSor.moveToFirst()){
                do {
                    var  _id = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
                    var  _artist = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    var  _title = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))
                    var  _songPath = cSor.getString(cSor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    _songlist.add(Songs(_id.toLong(), _title, _artist, _songPath,0))
                }while (cSor.moveToNext())
            }else{
                return null
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return _songlist
    }

    fun checkifIdExists(_id: Int): Boolean{
        var storeId = -1090
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + TABLE_NAME + " WHERE SongId = '$_id'"
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()){
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(COLUMN_ID))
            }while (cSor.moveToNext())
        }else{
            return false
        }
        return storeId != -1090
    }

    fun deleteFavourite(_id: Int){
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COLUMN_ID+ "="+_id, null)
        db.close()
    }

    fun checkSize(): Int{
        var counter = 0
        val db = this.readableDatabase
        var query_params = "SELECT * FROM "+ Staticated.TABLE_NAME
        val cSor = db.rawQuery(query_params, null)
        if (cSor.moveToFirst()){
            do {
                counter = counter +1
            }while (cSor.moveToNext())
        }else{
            return 0
        }
        return counter
    }
}