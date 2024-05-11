package com.example.expressionphotomanager

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast

class ResultActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var responseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imageView = findViewById(R.id.imageView)
        responseTextView = findViewById(R.id.textView)

        val emotion = intent.getStringExtra("emotion") ?: "No response received"
        responseTextView.text = emotion

        displayImages(emotion)
    }

    @SuppressLint("Range")
    private fun displayImages(emotion: String) {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_IMAGES,
            arrayOf(DatabaseHelper.COLUMN_IMAGE),
            "${DatabaseHelper.COLUMN_EMOTION} = ?",
            arrayOf(emotion),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val imageBytes = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE))
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "No image found for emotion $emotion", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
        db.close()
    }
}

