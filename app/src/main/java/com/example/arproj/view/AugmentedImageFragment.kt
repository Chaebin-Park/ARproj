package com.example.arproj.view

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.example.arproj.databinding.FragmentAugmentedImageDataBinding
import com.google.ar.core.*
import com.google.ar.sceneform.FrameTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class AugmentedImageFragment : Fragment() {

    private lateinit var bind: FragmentAugmentedImageDataBinding
    private lateinit var viewModel: SensorViewModel
    private lateinit var arFragment: CustomAugmentedImageArFragment

    private lateinit var arActivity: ArActivity
    private var trackingStateNumber = 0
    private var sessionNumber = 0
    private var isRecord = false

    private var accArrayList = arrayListOf<Array<*>>()
    private var gyroArrayList = arrayListOf<Array<*>>()
    private var magnetArrayList = arrayListOf<Array<*>>()
    private var cameraPoseArrayList = arrayListOf<Array<*>>()
    private var imagePoseArrayList = arrayListOf<Array<*>>()

    private var timeStamp: Long = 0L

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_augmented_image_data, container, false)
        init(bind)

        arActivity = activity as ArActivity
        arFragment = childFragmentManager.findFragmentById(bind.augmentedImageFragment.id) as CustomAugmentedImageArFragment

        bind.btnSave.setOnClickListener {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdfNow = SimpleDateFormat("yyMMdd_HHmmss")
            val formatDate = sdfNow.format(date)

            isRecord = false
            it.isEnabled = isRecord

            arActivity.saveArrayToCSV(formatDate, "A_I_Acc.csv", accArrayList)
            arActivity.saveArrayToCSV(formatDate, "A_I_Gyro.csv", gyroArrayList)
            arActivity.saveArrayToCSV(formatDate, "A_I_CameraPose.csv", cameraPoseArrayList)
            arActivity.saveArrayToCSV(formatDate, "A_I_Mag.csv", magnetArrayList)
            arActivity.saveArrayToCSV(formatDate, "A_I_ImagePose.csv", imagePoseArrayList)

            clearData()
            showToast("Save Data")
        }

        bind.btnAgain.setOnClickListener {
            sessionNumber += 1
            showToast("Session $sessionNumber")
        }

        bind.fabReset.setOnClickListener{
            clearData()
            bind.btnSave.isEnabled = false
        }

        return bind.root
    }

    override fun onResume() {
        super.onResume()

        arFragment.planeDiscoveryController.hide()

        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        viewModel.sensorLiveData.observe(viewLifecycleOwner, {
            timeStamp = it.timeStamp

            val camera = arFragment.arSceneView.scene.camera

            val poseTx = round(camera.worldPosition.normalized().x * DEC) / DEC
            val poseTy = round(camera.worldPosition.normalized().y * DEC) / DEC
            val poseTz = round(camera.worldPosition.normalized().z * DEC) / DEC
            val poseQx = round(camera.worldRotation.normalized().x * DEC) / DEC
            val poseQy = round(camera.worldRotation.normalized().y * DEC) / DEC
            val poseQz = round(camera.worldRotation.normalized().z * DEC) / DEC
            val poseQw = round(camera.worldRotation.normalized().w * DEC) / DEC

            val poseData = arrayOf(it.timeStamp, sessionNumber, trackingStateNumber, poseTx, poseTy, poseTz, poseQx, poseQy, poseQz, poseQw)

            val accData = arrayOf(it.timeStamp, sessionNumber, trackingStateNumber, it.ax, it.ay, it.az)
            val gyroData = arrayOf(it.timeStamp, sessionNumber, trackingStateNumber, it.gx, it.gy, it.gz)
            val magnetData = arrayOf(it.timeStamp,sessionNumber, trackingStateNumber, it.mx, it.my, it.mz)

            if(isRecord){
                accArrayList.add(accData)
                gyroArrayList.add(gyroData)
                magnetArrayList.add(magnetData)
                cameraPoseArrayList.add(poseData)
            }
        })
    }

    private fun clearData(){
        isRecord = false
        cameraPoseArrayList.clear()
        accArrayList.clear()
        gyroArrayList.clear()
        magnetArrayList.clear()
        imagePoseArrayList.clear()
    }

    private fun onUpdateFrame(frameTime: FrameTime){
        val arFrame: Frame? = arFragment.arSceneView.arFrame

        arFrame?.let { frame ->
            val augmentedImages: Collection<AugmentedImage> = frame.getUpdatedTrackables(AugmentedImage::class.java)

            for(image in augmentedImages){
                manageImageTrackingState(image)
            }
        }
    }

    private fun manageImageTrackingState(image: AugmentedImage){
        printLog("Method: ${image.trackingMethod.name} | State: ${image.trackingState.name} | ${image.index}")

        if(isRecord){
            imagePoseArrayList.add(arrayOf(
                timeStamp,
                sessionNumber,
                image.index,
                image.centerPose.tx(),
                image.centerPose.ty(),
                image.centerPose.tz(),
                image.centerPose.qx(),
                image.centerPose.qy(),
                image.centerPose.qz(),
                image.centerPose.qw(),
            ))
        }

        when(image.trackingState){
            TrackingState.TRACKING -> {
                bind.tvPausedStateNotTracking.setBackgroundColor(Color.BLACK)
                when(image.trackingMethod){
                    AugmentedImage.TrackingMethod.FULL_TRACKING -> {
                        ifImageRecognized(image)
                        viewImagePose(image)
                        bind.tvTrackingStateFullTracking.setBackgroundColor(Color.GREEN)
                        bind.tvTrackingStateLastKnownPose.setBackgroundColor(Color.BLACK)
                    }
                    AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> {
                        viewImagePose(image)
                        bind.tvTrackingStateFullTracking.setBackgroundColor(Color.BLACK)
                        bind.tvTrackingStateLastKnownPose.setBackgroundColor(Color.GREEN)
                    }
                    else -> {
                        showToast("It cant be...")
                    }
                }
            }
            TrackingState.PAUSED -> {
                when(image.trackingMethod){
                    AugmentedImage.TrackingMethod.NOT_TRACKING -> {
                        if(!isRecord)   startRecord()
                        ifImageRecognized(image)
                        viewImagePose(image)
                        bind.tvPausedStateNotTracking.setBackgroundColor(Color.GREEN)
                    }
                    else -> {
                        showToast("It cant be...")
                    }
                }
            }
            TrackingState.STOPPED -> {
                showToast("STOPPED")
            }
            else -> {
                showToast("It cant be...")
            }
        }
    }

    private fun ifImageRecognized(image: AugmentedImage){
        showToast("${image.name} recognized ")
        bind.tvImageNumber.text = "${image.index}"
        trackingStateNumber = image.index
    }

    @SuppressLint("SetTextI18n")
    private fun viewImagePose(image: AugmentedImage){
        bind.tvImageXPose.text = String.format("%.6f", image.centerPose.tx())
        bind.tvImageYPose.text = String.format("%.6f", image.centerPose.ty())
        bind.tvImageZPose.text = String.format("%.6f", image.centerPose.tz())
        bind.tvImageXRotation.text = String.format("%.6f", image.centerPose.qx())
        bind.tvImageYRotation.text = String.format("%.6f", image.centerPose.qy())
        bind.tvImageZRotation.text = String.format("%.6f", image.centerPose.qz())
        bind.tvImageWRotation.text = String.format("%.6f", image.centerPose.qw())
    }

    private fun startRecord(){
        printLog("Start Record")
        isRecord = true
        bind.btnSave.isEnabled = true
    }

    private fun init(binding: FragmentAugmentedImageDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun printLog(message: String){
        Log.e(TAG, message)
    }

    companion object{
        val TAG: String = AugmentedImageFragment::class.java.simpleName
        const val DEC: Int = 100000
    }
}