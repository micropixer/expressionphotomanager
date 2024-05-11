package com.example.expressionphotomanager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "emotions.db"
        const val DATABASE_VERSION = 1
        const val TABLE_IMAGES = "images"
        const val COLUMN_ID = "_id"
        const val COLUMN_IMAGE = "image"  // Blob data type to store image bytes
        const val COLUMN_EMOTION = "emotion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_IMAGES_TABLE = """
            CREATE TABLE $TABLE_IMAGES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IMAGE BLOB,
                $COLUMN_EMOTION TEXT
            )
        """.trimIndent()
        db.execSQL(CREATE_IMAGES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGES")
        onCreate(db)
    }

    fun addImage(image: ByteArray, emotion: String) {
        val db = this.writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_IMAGE, image)
                put(COLUMN_EMOTION, emotion)
            }
            db.insert(TABLE_IMAGES, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error adding image", e)
        } finally {
            db.close()
        }
    }


    @SuppressLint("Range")
    fun getImagesByEmotion(): Map<String, MutableList<Pair<Int, ByteArray>>> {
        val imagesByEmotion = mutableMapOf<String, MutableList<Pair<Int, ByteArray>>>()
        val db = this.readableDatabase

        db.use { database ->
            val cursor = database.query(
                TABLE_IMAGES,
                arrayOf(COLUMN_ID, COLUMN_EMOTION, COLUMN_IMAGE),
                null, null, null, null, null
            )
            cursor.use { cur ->
                while (cur.moveToNext()) {
                    val id = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ID))
                    val emotion = cur.getString(cur.getColumnIndexOrThrow(COLUMN_EMOTION))
                    val image = cur.getBlob(cur.getColumnIndexOrThrow(COLUMN_IMAGE))
                    imagesByEmotion.getOrPut(emotion) { mutableListOf() }.add(Pair(id, image))
                }
            }
        }
        return imagesByEmotion
    }



    fun deleteImageById(imageId: Any): Boolean {
        val db = this.writableDatabase
        val success = db.delete(TABLE_IMAGES, "$COLUMN_ID = ?", arrayOf(imageId.toString()))
        db.close()
        return success > 0
    }

}
