package com.example.expressionphotomanager

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddDeleteActivity : AppCompatActivity() {

    private lateinit var add:TextView
    private lateinit var delete:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_delete)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        add= findViewById(R.id.button_add)
        delete= findViewById(R.id.button_delete)

        add.setOnClickListener(){
            val intent = Intent(this@AddDeleteActivity, CameraActivity::class.java).apply {
            }
            startActivity(intent)
        }

        delete.setOnClickListener(){
            val intent = Intent(this@AddDeleteActivity, DisplayImagesActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }
}