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
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import org.koin.core.qualifier.named
import java.io.File
import java.lang.reflect.InvocationTargetException


class StreamDataFragment : Fragment() {

    private lateinit var bind: FragmentStreamDataBinding
    private lateinit var viewModel: SensorViewModel
    private lateinit var arFragment: CustomAugmentedImageArFragment
    private lateinit var node: AnchorNode
//    private lateinit var imageDatabase: AugmentedImageDatabase
//    private lateinit var config: Config
//    private lateinit var session: Session

    private val dataArray = arrayListOf<Anchor>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_stream_data, container, false)
        init(bind)

        arFragment = childFragmentManager.findFragmentById(bind.streamArFragment.id) as CustomAugmentedImageArFragment

        bind.btnConnect.setOnClickListener{

        }

        bind.btnStream.setOnClickListener {

        }

        return bind.root
    }

    override fun onResume() {
        super.onResume()
        arFragment.planeDiscoveryController.hide()
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
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
                                showToast("img1")
                            }
                            "img2.jpg" -> {
                                showToast("img2")
                            }
                            "img3.jpg" -> {
                                showToast("img3")
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

    private fun init(binding: FragmentStreamDataBinding){
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        binding.sensorViewModel = viewModel
        binding.lifecycleOwner = this
    }

    private fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}