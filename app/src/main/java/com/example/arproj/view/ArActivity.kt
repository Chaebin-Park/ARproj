package com.example.arproj.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arproj.helper.CsvHelper
import com.example.arproj.R
import com.example.arproj.databinding.ActivityArBinding
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.FrameTime
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


@Suppress("UNCHECKED_CAST")
class ArActivity : AppCompatActivity() {
    private var arBinding: ActivityArBinding? = null
    private val binding get() = arBinding!!

    private lateinit var saveFragment: SaveDataFragment
    private lateinit var streamFragment: StreamDataFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arBinding = ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 투명하게
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding.root.setPadding(0, statusBarHeight(this), 0, 0)

        // 액션바 설정
        setSupportActionBar(binding.toolbar)

        checkPermission()

        // 프래그먼트 정의
        saveFragment = SaveDataFragment()
        streamFragment = StreamDataFragment()

        // 최초 노출되는 프래그먼트 설정
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.ar_frame_layout, saveFragment).commit()
    }

    // statusbar 크기 설정
    private fun statusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")

        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId)
        else 0
    }

    // option menu 정의
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_list, menu)

        return true
    }

    // option menu 선택 액션 정의
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction = supportFragmentManager.beginTransaction()

        when(item.itemId){
            R.id.save_data -> {
                showToast("Save Data")
                transaction.replace(R.id.ar_frame_layout, saveFragment).commit()
            }
            R.id.stream_data -> {
                showToast("Stream Data")
                transaction.replace(R.id.ar_frame_layout, streamFragment).commit()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                AlertDialog.Builder(this)
                    .setTitle("Alert")
                    .setMessage("저장소 권한이 거부되었습니다.")
                    .setNeutralButton("설정"){ _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setPositiveButton("확인") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.R)
    fun saveData(formatDate: String ,fileName: String, dataList: ArrayList<Array<String>>){
        val filePath = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path
        val dir = File("$filePath/$formatDate")

        if(!dir.exists()){
            dir.mkdir()
        }

        val csvHelper = CsvHelper(dir.path)
        csvHelper.writeData("${formatDate}_${fileName}", dataList)
    }

    fun setAugmentedImageDb(config: Config, session: Session): Boolean {

        val imageDatabase = this.assets.open("images.imgdb").use {
            AugmentedImageDatabase.deserialize(session, it)
        }

        config.augmentedImageDatabase = imageDatabase

        return true
    }

    /***
     * Function to get the bitmap from a asset image
     */
    private fun loadAugmentedImages(imagePath: String): Bitmap {
        val assetManger = assets
        val inputStream: InputStream = assetManger.open(imagePath)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val STORAGE_PERMISSION_CODE = 1111
    }
}