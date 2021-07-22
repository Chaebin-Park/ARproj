package com.example.arproj.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.Anchor
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment: ArFragment() {

//    override fun getSessionConfiguration(session: Session?): Config {
//
//        planeDiscoveryController.setInstructionView(null)
//        val config = Config(session)
//        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
//        session?.configure(config)
//
//        arSceneView.setupSession(session)
//        session?.let { safeSession ->
//            if ((requireActivity() as ArActivity).setAugmentedImageDb(config, safeSession)) {
//                Log.d("SetUpAugImageDb", "Success")
//            } else {
//                Log.e("SetUpAugImageDb", "Failure")
//            }
//        }
//        return config
//    }

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