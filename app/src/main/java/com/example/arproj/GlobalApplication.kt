package com.example.arproj

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}