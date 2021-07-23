package com.example.arproj.view

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.arproj.R
import com.example.arproj.SensorViewModel
import com.example.arproj.databinding.FragmentStreamDataBinding
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import java.text.SimpleDateFormat
import java.util.*


class StreamDataFragment : Fragment() {

    private lateinit var bind: FragmentStreamDataBinding
    private lateinit var viewModel: SensorViewModel
    private lateinit var arFragment: CustomAugmentedImageArFragment

    private lateinit var arActivity: ArActivity
    private var sessionNumber = 0
    private var isRecord = false

    private var accList = arrayListOf<Array<String>>()
    private var gyroList = arrayListOf<Array<String>>()
    private var magnetList = arrayListOf<Array<String>>()
    private var poseList = arrayListOf<Array<String>>()

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_stream_data, container, false)
        init(bind)

        arActivity = activity as ArActivity
        arFragment = childFragmentManager.findFragmentById(bind.streamArFragment.id) as CustomAugmentedImageArFragment

        bind.btnSave.setOnClickListener {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdfNow = SimpleDateFormat("yyMMdd_HHmmss")
            val formatDate = sdfNow.format(date)

            isRecord = false
            it.isEnabled = isRecord

            arActivity.saveData(formatDate, "A_I_Acc.csv", accList)
            arActivity.saveData(formatDate, "A_I_Gyro.csv", gyroList)
            arActivity.saveData(formatDate, "A_I_Pose.csv", poseList)
            arActivity.saveData(formatDate, "A_I_Mag.csv", magnetList)

            clearData()
            showToast("Save Data")
        }

        return bind.root
    }

    override fun onResume() {
        super.onResume()
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        viewModel.sensorLiveData.observe(viewLifecycleOwner, {
            val timeStamp = System.currentTimeMillis().toString()
            val camera = arFragment.arSceneView.scene.camera
            val poseTx = String.format("%.6f", camera.worldPosition.normalized().x)
            val poseTy = String.format("%.6f", camera.worldPosition.normalized().y)
            val poseTz = String.format("%.6f", camera.worldPosition.normalized().z)
            val poseQx = String.format("%.6f", camera.worldRotation.normalized().x)
            val poseQy = String.format("%.6f", camera.worldRotation.normalized().y)
            val poseQz = String.format("%.6f", camera.worldRotation.normalized().z)
            val poseQw = String.format("%.6f", camera.worldRotation.normalized().w)

            val poseData = arrayOf(sessionNumber.toString(), timeStamp, poseTx, poseTy, poseTz, poseQx, poseQy, poseQz, poseQw)
            val data = it.split(",")
            val accData = data[0].split(" ")
            val gyroData = data[1].split(" ")
            val magnetData = data[2].split(" ")

            if(isRecord){
                accList.add(accData.toTypedArray())
                gyroList.add(gyroData.toTypedArray())
                magnetList.add(magnetData.toTypedArray())
                poseList.add(poseData)
            }
        })
    }

    private fun clearData(){
        poseList.clear()
        accList.clear()
        gyroList.clear()
        magnetList.clear()
    }

    private fun onUpdateFrame(frameTime: FrameTime){
        val arFrame: Frame? = arFragment.arSceneView.arFrame

        arFrame?.let { frame ->
            val augmentedImages: Collection<AugmentedImage> = frame.getUpdatedTrackables(AugmentedImage::class.java)
            Log.e("ZZZ", "${frameTime.deltaSeconds}")

            for(image in augmentedImages){
                when(image.trackingState){
                    TrackingState.TRACKING -> {
                    }
                    TrackingState.PAUSED -> {
                        when(image.name){
                            "img1.jpg" -> {
                                showToast("Record : Image1")
                                changeImage(image.name)
                                isRecord = true
                                bind.btnSave.isEnabled = true
                                sessionNumber = 1
                            }
                            "img2.jpg" -> {
                                showToast("Record : Image2")
                                changeImage(image.name)
                                isRecord = true
                                bind.btnSave.isEnabled = true
                                sessionNumber = 2
                            }
                            "img3.jpg" -> {
                                showToast("Record : Image3")
                                changeImage(image.name)
                                isRecord = true
                                bind.btnSave.isEnabled = true
                                sessionNumber = 3
                            }
                        }
                    }
                    else -> {
                        showToast(";;")
                    }
                }
            }
        }
    }

    private fun changeImage(fileName: String){
        val assetManager = resources.assets
        val inputStream = assetManager.open(fileName)
        bind.trackingStateView.setImageDrawable(Drawable.createFromStream(inputStream, null))
        inputStream.close()
    }

    private fun init(binding: FragmentStreamDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}