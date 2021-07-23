package com.example.arproj.view

import android.widget.Toast
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ux.ArFragment

open class CustomArFragment: ArFragment() {

    override fun handleSessionException(sessionException: UnavailableException?) {
        val msg: String
        when(sessionException){
            is UnavailableArcoreNotInstalledException -> {
                msg = "Install ARCore"
            }
            is UnavailableApkTooOldException -> {
                msg = "Upgrade ARCore"
            }
            is UnavailableSdkTooOldException -> {
                msg = "Upgrade Application"
            }
            is UnavailableDeviceNotCompatibleException -> {
                msg = "Upgrade Application"
            }
            else -> {
                msg = "Can't create AR Session"
            }
        }
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

}