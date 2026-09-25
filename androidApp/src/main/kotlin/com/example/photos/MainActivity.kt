package com.example.photos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.photos.data.picker.AndroidPhotoPicker
import com.example.photos.di.platformModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dependencies = platformModule(applicationContext, AndroidPhotoPicker(this))
        setContent {
            App(dependencies)
        }
    }
}
