package com.example.arproj.view

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Session

class CustomAugmentedImageArFragment: CustomArFragment() {
    override fun getSessionConfiguration(session: Session): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = Config(session)

        // Depth Setting
        val isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
        if (isDepthSupported) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        }

        // Camera Setting
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        if(config.focusMode == Config.FocusMode.FIXED)  config.focusMode = Config.FocusMode.AUTO

        session.configure(config)

        arSceneView.setupSession(session)
        session.let { safeSession ->
            if ((requireActivity() as ArActivity).setAugmentedImageDb(config, safeSession)) {
                Log.d("SetUpAugImageDb", "Success")
            } else {
                Log.e("SetUpAugImageDb", "Failure")
            }
        }
        return config
    }
}