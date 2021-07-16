package com.example.arproj

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
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment: ArFragment() {
    private val dataArray = arrayListOf<Anchor>()

    private lateinit var viewModel: SensorViewModel
    private lateinit var node: AnchorNode
    private var isRecord = false
    private var poseList = arrayListOf<Array<String>>()
    private var accList = arrayListOf<Array<String>>()
    private var gyroList = arrayListOf<Array<String>>()
    private var magnetList = arrayListOf<Array<String>>()
    private var anchorList = arrayListOf<Array<String>>()

    private fun clearData(){
        poseList.clear()
        accList.clear()
        gyroList.clear()
        anchorList.clear()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvStartPose = view.rootView.findViewById<TextView>(R.id.tv_start_pos)
        val tvCurPose = view.rootView.findViewById<TextView>(R.id.tv_cur_pos)
        val tvAcc = view.rootView.findViewById<TextView>(R.id.tv_acc)
        val tvGyro = view.rootView.findViewById<TextView>(R.id.tv_gyro)
        val tvMagnet = view.rootView.findViewById<TextView>(R.id.tv_magnet)
        val btnRecord = view.rootView.findViewById<Button>(R.id.btn_record)
        val btnSave = view.rootView.findViewById<Button>(R.id.btn_save)

        // 센서 데이터 변화 감지
        viewModel.accLiveData.observe(viewLifecycleOwner, {
            val timeStamp = System.currentTimeMillis().toString()

            val poseX = arSceneView.scene.camera.worldPosition.normalized().x.toString()
            val poseY = arSceneView.scene.camera.worldPosition.normalized().y.toString()
            val poseZ = arSceneView.scene.camera.worldPosition.normalized().z.toString()

            val data = it.split(",")
            val accData = data[0].split(" ")
            val gyroData = data[1].split(" ")
            val magnetData = data[2].split(" ")
            val poseData = arrayOf(timeStamp, poseX, poseY, poseZ)

            tvCurPose.text = "$poseX $poseY $poseZ"
            tvAcc.text = accData.toString()
            tvGyro.text = gyroData.toString()
            tvMagnet.text = magnetData.toString()

            arSceneView.scene.camera.worldPosition

            // Record 버튼 눌렀고, 앵커가 하나 이상일 때
            if(isRecord && dataArray.isNotEmpty()) {
                val temp = dataArray[0].pose
                val anchorData = arrayOf(
                    timeStamp,
                    String.format("%.6f", temp.tx()),
                    String.format("%.6f", temp.ty()),
                    String.format("%.6f", temp.tz()),
                    temp.qx().toString(),
                    temp.qy().toString(),
                    temp.qz().toString(),
                    temp.qw().toString()
                )

                // 가속도, 자이로, 카메라 위치, 앵커 위치 저장
                accList.add(accData.toTypedArray())
                gyroList.add(gyroData.toTypedArray())
                magnetList.add(magnetData.toTypedArray())
                poseList.add(poseData)
                anchorList.add(anchorData)
            }
        })

        // Record 버튼 눌렀을 때
        btnRecord.setOnClickListener {
            if(!isRecord)   isRecord = true
            btnSave.isEnabled = true
            it.isEnabled = false
        }

        // Save 버튼 눌렀을 때
        btnSave.setOnClickListener{
            isRecord = false
            val arActivity = activity as ArActivity

            btnRecord.isEnabled = true
            it.isEnabled = false

            arActivity.saveData("Acc.csv", accList)
            arActivity.saveData("Gyro.csv", gyroList)
            arActivity.saveData("Pose.csv", poseList)
            arActivity.saveData("Mag.csv", magnetList)
            arActivity.saveData("Anchor1.csv", anchorList)
            clearData()
        }

        // 앵커 설치
        setOnTapArPlaneListener{  hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()
            node = AnchorNode(anchor)
            node.setParent(this.arSceneView.scene)

            dataArray.add(anchor)
            if(dataArray.isNotEmpty()) tvStartPose.text = dataArray[0].pose.toString()

            MaterialFactory.makeOpaqueWithColor(context, Color(0.3f, 0.9f, 0.0f))
                .thenAccept{material ->
                    val sphere = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material)
                    Node().apply {
                        setParent(node)
                        localPosition = Vector3.zero()
                        renderable = sphere
                    }
                }
        }
    }


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

    companion object{
        const val STORAGE_PERMISSION_CODE = 1111
    }
}