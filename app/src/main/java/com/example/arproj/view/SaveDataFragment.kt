
package com.example.arproj.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

class SaveDataFragment : Fragment() {

    private lateinit var arActivity: ArActivity
    private lateinit var arFragment: CustomArFragment
    private lateinit var bind: FragmentSaveDataBinding
    private lateinit var viewModel: SensorViewModel

    private lateinit var node: AnchorNode
    private var isRecord = false

    private val anchorList  = arrayListOf<Anchor>()
    private var poseList    = arrayListOf<Array<*>>()
    private var accList     = arrayListOf<Array<*>>()
    private var gyroList    = arrayListOf<Array<*>>()
    private var magnetList  = arrayListOf<Array<*>>()
    private var anchorDataList  = arrayListOf<Array<String>>()
    private var testList    = arrayListOf<Array<Anchor>>()
    private var nodeList    = arrayListOf<Node>()

    private lateinit var accData: Array<*>
    private lateinit var gyroData: Array<*>
    private lateinit var magnetData: Array<*>


    private lateinit var formatDate: String
    private var sessionNumber: Int = -1

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_save_data, container, false)
        init(bind)

        arActivity = activity as ArActivity

        arFragment = childFragmentManager.findFragmentById(R.id.save_ar_fragment) as CustomArFragment
        arFragment.setOnTapArPlaneListener{ hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            node = AnchorNode(anchor)
            node.setParent(arFragment.arSceneView.scene)
            nodeList.add(node)

            anchorList.add(anchor)
            if(anchorList.isNotEmpty())  bind.tvStartPos.text = anchorList[0].pose.toString()
            else    bind.tvStartPos.text = "No Anchor"

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

            accData = arrayOf(timeStamp, it.ax, it.ay, it.az)
            gyroData = arrayOf(timeStamp, it.gx, it.gy, it.gz)
            magnetData = arrayOf(timeStamp, it.mx, it.my, it.mz)

            Log.e("POSE", "$poseTx, $poseTy, $poseTz, $poseQx, $poseQy, $poseQz, $poseQw")
            Log.e("ACC", "${it.ax} ${it.ay} ${it.az}")
            Log.e("GYRO", "${it.gx} ${it.gy} ${it.gz}")
            Log.e("MAGNET", "${it.mx} ${it.my} ${it.mz}")

            arFragment.arSceneView.arFrame?.timestamp
            bind.tvCurPos.text  = "$poseTx $poseTy $poseTz $poseQx $poseQy $poseQz $poseQw"

            // Record 버튼 눌렀고, 앵커가 하나 이상일 때
            if(isRecord && anchorList.isNotEmpty()) {
                testList.add(anchorList.toTypedArray())
                // 0~n번 앵커 순서대로 데이터 저장
                for(i in anchorList.indices) {
                    val temp = anchorList[i].pose
                    val anchorData = arrayOf(
                        sessionNumber.toString(),
                        timeStamp,
                        String.format("%.6f", temp.tx()),
                        String.format("%.6f", temp.ty()),
                        String.format("%.6f", temp.tz()),
                        String.format("%.6f", temp.qx()),
                        String.format("%.6f", temp.qy()),
                        String.format("%.6f", temp.qz()),
                        String.format("%.6f", temp.qw())
                    )
                    anchorDataList.add(anchorData)
                }

                // 가속도, 자이로, 카메라 위치, 앵커 위치 저장

                if(accData.isNotEmpty())    accList.add(accData)
                if(gyroData.isNotEmpty())   gyroList.add(gyroData)
                if(magnetData.isNotEmpty()) magnetList.add(magnetData)
                if(poseData.isNotEmpty())   poseList.add(poseData)
            }
        })

        // 기록 시작
        bind.btnRecord.setOnClickListener{
            sessionNumber += 1

            showToast("Session: $sessionNumber")

            if(!isRecord)   isRecord = true
            bind.btnSave.isEnabled = true
        }

        // 저장
        bind.btnSave.setOnClickListener{
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdfNow = SimpleDateFormat("yyMMdd_HHmmss")
            formatDate = sdfNow.format(date)

            val tmp = ArrayList<Array<*>>()
            isRecord = false

            it.isEnabled = false

            arActivity.saveArrayToCSV(formatDate, "Acc.csv", accList)
            arActivity.saveArrayToCSV(formatDate, "Gyro.csv", gyroList)
            arActivity.saveArrayToCSV(formatDate, "Pose.csv", poseList)
            arActivity.saveArrayToCSV(formatDate, "Mag.csv", magnetList)

            // 설치된 앵커 번호별로 파일 분리하여 데이터 저장
            for(i in anchorList.indices){
                for(j in anchorDataList.indices){
                    if(anchorDataList[j][0] == i.toString()){
                        tmp.add(anchorDataList[j])
                    }
                }
                arActivity.saveArrayToCSV(formatDate, "Anchor${i}.csv", tmp)
                tmp.clear()
            }
            clearData()
            showToast("Save Data")
        }

        // 초기화
        bind.fabRestore.setOnClickListener{
            clearData()
            anchorList.clear()
            if(nodeList.isNotEmpty()){
                for(node in nodeList){
                    arFragment.arSceneView.scene.removeChild(node)
                }
            }

            bind.btnSave.isEnabled = false
            bind.btnRecord.isEnabled = true

            showToast("Clear Data")
        }

        return bind.root
    }

    private fun clearData(){
        isRecord = false
        poseList.clear()
        accList.clear()
        magnetList.clear()
        gyroList.clear()
        anchorDataList.clear()
    }

    private fun init(binding: FragmentSaveDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(content: String){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val DEC: Int = 100000
    }
}