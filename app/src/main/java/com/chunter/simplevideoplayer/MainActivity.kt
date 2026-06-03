package com.chunter.simplevideoplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.cast.framework.CastContext
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CastContext.getSharedInstance(applicationContext, Executors.newSingleThreadExecutor())
            .addOnSuccessListener { _ ->
                Log.d("CastContext", "CastContext initialised")
            }
            .addOnFailureListener { exception ->
                Log.e("CastContext", "CastContext unable to be initialised", exception)
            }

        findViewById<Button>(R.id.playButton)
            .setOnClickListener {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                startActivity(intent)
            }
    }
}