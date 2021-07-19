package com.example.arproj.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.arproj.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mainBinding: ActivityMainBinding? = null
    private val binding get() = mainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener{
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
        }
    }
}