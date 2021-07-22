package com.example.arproj.view

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.arproj.R
import com.example.arproj.SensorViewModel
import com.example.arproj.databinding.FragmentStreamDataBinding
import com.google.ar.core.*
import com.google.ar.core.exceptions.RecordingFailedException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import java.io.File
import java.lang.reflect.InvocationTargetException


class StreamDataFragment : Fragment() {

    private lateinit var bind: FragmentStreamDataBinding
    private lateinit var viewModel: SensorViewModel
    private lateinit var arFragment: CustomArFragment
    private lateinit var node: AnchorNode
    private lateinit var imageDatabase: AugmentedImageDatabase
    private lateinit var config: Config
    private lateinit var session: Session

    private val dataArray = arrayListOf<Anchor>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_stream_data, container, false)
        init(bind)

        arFragment = childFragmentManager.findFragmentById(R.id.stream_ar_fragment) as CustomArFragment

//        val session = arFragment.arSceneView.session
//        val config = Config(session)
//        imageDatabase = requireContext().assets.open("images.imgdb").use {
//            AugmentedImageDatabase.deserialize(session, it)
//        }
//        config.augmentedImageDatabase = imageDatabase
//        session?.configure(config)
//
//        val frame = session?.update()
//        val updatedAugmentedImages = frame?.getUpdatedTrackables(AugmentedImage::class.java)
//
//        for(img in updatedAugmentedImages!!){
//            when(img.trackingState){
//                TrackingState.PAUSED -> {
//                    bind.trackingStateView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
//                }
//                TrackingState.TRACKING -> {
//                    bind.trackingStateView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
//                }
//                TrackingState.STOPPED -> {
//
//                }
//            }
//        }

        if(session != null){

        }

        arFragment.setOnTapArPlaneListener{ hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            node = AnchorNode(anchor)
            node.setParent(arFragment.arSceneView.scene)

            dataArray.add(anchor)
            if(dataArray.isNotEmpty()) showToast(dataArray[dataArray.size-1].pose.toString())

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

        bind.btnConnect.setOnClickListener{

        }

        bind.btnStream.setOnClickListener {

        }

        return bind.root
    }

    private fun init(binding: FragmentStreamDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(content: String){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }

}