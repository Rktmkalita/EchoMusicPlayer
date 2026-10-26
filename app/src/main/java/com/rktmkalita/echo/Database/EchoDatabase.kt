package com.rktmkalita.echo.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.rktmkalita.echo.Songs

class EchoDatabase: SQLiteOpenHelper{
    var _songList = ArrayList<Songs>()

    object Staticated{
        var DB_VERSION = 1
        val DB_NAME = "FavouriteDatabase"
        val TABLE_NAME = "FavouriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
    }
    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
        sqliteDatabase?.execSQL("CREATE TABLE " + Staticated.TABLE_NAME + "( " + Staticated.COLUMN_ID + " INTEGER," + Staticated.COLUMN_SONG_ARTIST + " STRING,"
                + Staticated.COLUMN_SONG_TITLE + " STRING," + Staticated.COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("DROP TABLE IF EXISTS " + Staticated.TABLE_NAME)
        onCreate(p0)
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, Staticated.DB_NAME, null, Staticated.DB_VERSION)

    fun storeAsFavourite(id: Int?, artist: String?, songTitle: String?, path: String?){
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH, path)
        db.insert(Staticated.TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>?{
        try {
            _songList.clear()
            val db = this.readableDatabase
            val queryparams = "SELECT * FROM " + Staticated.TABLE_NAME
            var cSor = db.rawQuery(queryparams, null)
            if (cSor.moveToFirst()){
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cSor.getString(cSor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _artist, _songPath,0))
                }while (cSor.moveToNext())
            }else{
                return null
            }
            cSor.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
        return _songList
    }

    fun checkifIdExists(_id: Int): Boolean{
        val db = this.readableDatabase
        val queryparams = "SELECT 1 FROM " + Staticated.TABLE_NAME + " WHERE " + Staticated.COLUMN_ID + " = ? LIMIT 1"
        val cSor = db.rawQuery(queryparams, arrayOf(_id.toString()))
        val exists = cSor.moveToFirst()
        cSor.close()
        return exists
    }

    fun deleteFavourite(_id: Int){
        val db = this.writableDatabase
        db.delete(Staticated.TABLE_NAME, Staticated.COLUMN_ID + "=" + _id,null)
        db.close()
    }

    fun chechSize(): Int{
        var counter = 0
        val db = this.readableDatabase
        val queryparams = "SELECT * FROM " + Staticated.TABLE_NAME
        val cSor = db.rawQuery(queryparams, null)
        if (cSor.moveToFirst()){
            do {
                counter = counter + 1
            }while (cSor.moveToNext())
        }else{
            return 0
        }
        cSor.close()
        db.close()
        return counter
    }
}
