package com.example.arproj.view

import android.annotation.SuppressLint
import android.graphics.Color
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
    private var sessionNumber = 0
    private var isRecord = false

    private var accList = arrayListOf<Array<*>>()
    private var gyroList = arrayListOf<Array<*>>()
    private var magnetList = arrayListOf<Array<*>>()
    private var poseList = arrayListOf<Array<*>>()

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

            arActivity.saveArrayToCSV(formatDate, "A_I_Acc.csv", accList)
            arActivity.saveArrayToCSV(formatDate, "A_I_Gyro.csv", gyroList)
            arActivity.saveArrayToCSV(formatDate, "A_I_Pose.csv", poseList)
            arActivity.saveArrayToCSV(formatDate, "A_I_Mag.csv", magnetList)

            clearData()
            showToast("Save Data")
        }

        bind.fabReset.setOnClickListener{
            clearData()
            bind.btnSave.isEnabled = false
            bind.trackingStateView.setImageResource(0)
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

            val poseTx = round(camera.worldPosition.normalized().x * DEC) / DEC
            val poseTy = round(camera.worldPosition.normalized().y * DEC) / DEC
            val poseTz = round(camera.worldPosition.normalized().z * DEC) / DEC
            val poseQx = round(camera.worldRotation.normalized().x * DEC) / DEC
            val poseQy = round(camera.worldRotation.normalized().y * DEC) / DEC
            val poseQz = round(camera.worldRotation.normalized().z * DEC) / DEC
            val poseQw = round(camera.worldRotation.normalized().w * DEC) / DEC

            val poseData = arrayOf(sessionNumber, timeStamp, poseTx, poseTy, poseTz, poseQx, poseQy, poseQz, poseQw)

            val accData = arrayOf(timeStamp, it.ax, it.ay, it.az)
            val gyroData = arrayOf(timeStamp, it.gx, it.gy, it.gz)
            val magnetData = arrayOf(timeStamp, it.mx, it.my, it.mz)

            if(isRecord){
                accList.add(accData)
                gyroList.add(gyroData)
                magnetList.add(magnetData)
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
            printLog("${frameTime.deltaSeconds}")

            for(image in augmentedImages){
                controlState(image)
            }
        }
    }

    private fun controlState(image: AugmentedImage){
        when(image.trackingState){
            TrackingState.TRACKING -> {
                bind.trackingStateView.setBackgroundColor(Color.parseColor("#00FF00"))
            }
            TrackingState.PAUSED -> {
                showToast("Record: ${image.name}")
                changeImage(image.name)
                startRecord(image.index)
            }
            TrackingState.STOPPED -> {

            }
            else -> {
                showToast("It cant be...")
            }
        }
    }

    private fun startRecord(num: Int){
        isRecord = true
        bind.btnSave.isEnabled = true
        sessionNumber = num
    }

    private fun changeImage(fileName: String){
        val assetManager = resources.assets
        val inputStream = assetManager.open(fileName)
        bind.trackingStateView.setImageDrawable(Drawable.createFromStream(inputStream, null))
        inputStream.close()
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
        val TAG: String = this::class.java.simpleName
        const val DEC: Int = 100000
    }
}