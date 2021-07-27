package com.example.arproj

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlin.math.pow
import kotlin.math.sqrt

class SensorViewModel(application: Application): AndroidViewModel(application) {

    val sensorLiveData = SensorLiveData()

    inner class SensorLiveData: LiveData<SensorData>(), SensorEventListener{
        private val sensorManager
            get() = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private lateinit var accelerometer: Sensor
        private lateinit var gyroscope: Sensor
        private lateinit var magnetField: Sensor

        private var accValues = FloatArray(3)
        private var gyroValues = FloatArray(3)
        private var magnetValues = FloatArray(3)

        private var isAccRunning = false
        private var isGyroRunning = false
        private var isMagnetRunning = false

        override fun onSensorChanged(event: SensorEvent) {

            when(event.sensor){
                accelerometer -> {
                    System.arraycopy(event.values, 0, accValues, 0, event.values.size)
                }
                gyroscope -> {
                    System.arraycopy(event.values, 0, gyroValues, 0, event.values.size)
                }
                magnetField -> {
                    System.arraycopy(event.values, 0, magnetValues, 0, event.values.size)
                }
            }

            if(accValues.isNotEmpty() && gyroValues.isNotEmpty() && magnetValues.isNotEmpty()) {
                val sensorData = SensorData(
                    accValues[0],
                    accValues[1],
                    accValues[2],
                    gyroValues[0],
                    gyroValues[1],
                    gyroValues[2],
                    magnetValues[0],
                    magnetValues[1],
                    magnetValues[2]
                )
                postValue(sensorData)
            }
        }

        override fun onActive() {
            sensorManager.let { sm ->
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).let { acc ->
                    this.accelerometer = acc
                }
                sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE).let { gyro ->
                    this.gyroscope = gyro
                }
                sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).let { mag ->
                    this.magnetField = mag
                }
                sm.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
                sm.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_FASTEST)
                sm.registerListener(this, this.magnetField, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }

        override fun onInactive() {
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.e(TAG, "onAccuracyChanged")
        }
    }

    companion object{
        val TAG: String = SensorViewModel::class.java.simpleName
    }

    data class SensorData(
        val ax: Float,
        val ay: Float,
        val az: Float,
        val gx: Float,
        val gy: Float,
        val gz: Float,
        val mx: Float,
        val my: Float,
        val mz: Float,
    )

}