package com.example.arproj.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
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
import com.example.arproj.databinding.FragmentSaveDataBinding
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory

class SaveDataFragment : Fragment() {

    private lateinit var bind: FragmentSaveDataBinding
    private lateinit var viewModel: SensorViewModel
    private lateinit var arFragment: CustomArFragment
    private lateinit var node: AnchorNode
    private var isRecord = false
    private val dataArray = arrayListOf<Anchor>()
    private var poseList = arrayListOf<Array<String>>()
    private var accList = arrayListOf<Array<String>>()
    private var gyroList = arrayListOf<Array<String>>()
    private var magnetList = arrayListOf<Array<String>>()
    private var anchorList = arrayListOf<Array<String>>()

    private var test = arrayListOf<Any>()

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_save_data, container, false)
        init(bind)
        arFragment = childFragmentManager.findFragmentById(R.id.save_ar_fragment) as CustomArFragment
        arFragment.setOnTapArPlaneListener{ hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            node = AnchorNode(anchor)
            node.setParent(arFragment.arSceneView.scene)

            dataArray.add(anchor)
            if(dataArray.isNotEmpty()) bind.tvStartPos.text = dataArray[0].pose.toString()

            MaterialFactory.makeOpaqueWithColor(context, Color(0.3f, 0.9f, 0.0f))
                .thenAccept{ material ->
                    val sphere = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material)
                    Node().apply {
                        setParent(node)
                        localPosition = Vector3.zero()
                        renderable = sphere
                    }
                }
        }

        viewModel.accLiveData.observe(viewLifecycleOwner, {
            val timeStamp = System.currentTimeMillis().toString()

            val poseX = arFragment.arSceneView.scene.camera.worldPosition.normalized().x.toString()
            val poseY = arFragment.arSceneView.scene.camera.worldPosition.normalized().y.toString()
            val poseZ = arFragment.arSceneView.scene.camera.worldPosition.normalized().z.toString()

            val data = it.split(",")
            val accData = data[0].split(" ")
            val gyroData = data[1].split(" ")
            val magnetData = data[2].split(" ")
            val poseData = arrayOf(timeStamp, poseX, poseY, poseZ)

            bind.tvAcc.text     = accData.toString()
            bind.tvGyro.text    = gyroData.toString()
            bind.tvMagnet.text  = magnetData.toString()
            bind.tvCurPos.text  = "$poseX $poseY $poseZ"

            // Record 버튼 눌렀고, 앵커가 하나 이상일 때
            if(isRecord && dataArray.isNotEmpty()) {
                val temp = dataArray[0].pose
                val anchorData = arrayOf(
                    timeStamp,
                    String.format("%.6f", temp.tx()),
                    String.format("%.6f", temp.ty()),
                    String.format("%.6f", temp.tz()),
                    String.format("%.6f", temp.qx()),
                    String.format("%.6f", temp.qy()),
                    String.format("%.6f", temp.qz()),
                    String.format("%.6f", temp.qw())
                )

                // 가속도, 자이로, 카메라 위치, 앵커 위치 저장
                accList.add(accData.toTypedArray())
                gyroList.add(gyroData.toTypedArray())
                magnetList.add(magnetData.toTypedArray())
                poseList.add(poseData)
                anchorList.add(anchorData)
            }
        })

        bind.btnRecord.setOnClickListener{
            if(!isRecord)   isRecord = true
            bind.btnSave.isEnabled = true
            it.isEnabled = false
        }

        bind.btnSave.setOnClickListener{
            isRecord = false
            val arActivity = activity as ArActivity

            bind.btnRecord.isEnabled = true
            it.isEnabled = false

            arActivity.saveData("Acc.csv", accList)
            arActivity.saveData("Gyro.csv", gyroList)
            arActivity.saveData("Pose.csv", poseList)
            arActivity.saveData("Mag.csv", magnetList)
            arActivity.saveData("Anchor1.csv", anchorList)
            clearData()
        }

        return bind.root
    }

    private fun clearData(){
        poseList.clear()
        accList.clear()
        gyroList.clear()
        anchorList.clear()
    }

    private fun init(binding: FragmentSaveDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(content: String){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }
}