package com.example.expressionphotomanager

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DisplayImagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var home : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_images)
        databaseHelper = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerViewEmotions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadImages()

        home = findViewById(R.id.home)
        home.setOnClickListener(){
            val intent = Intent(this@DisplayImagesActivity, AddDeleteActivity::class.java).apply {}
            startActivity(intent)
        }
    }

    private fun loadImages() {
        val imagesByEmotion = databaseHelper.getImagesByEmotion() // Adjust this method to return Map<String, MutableList<Pair<Int, ByteArray>>>
        recyclerView.adapter = EmotionAdapter(this, imagesByEmotion) { imageId ->
            showDeleteConfirmation(imageId)
        }
    }



    private fun showDeleteConfirmation(imageId: Int) {
        val fragment = DeleteConfirmationFragment.newInstance(
            onDeleteConfirmed = {
                databaseHelper.deleteImageById(imageId)
                loadImages() // Reload or refresh your data after deletion
            },
            onCancel = {
                // Handle the cancellation if necessary
            }
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Ensure you have a container to add your fragment to
            .addToBackStack(null) // This allows users to return back by pressing back
            .commit()
    }

}
