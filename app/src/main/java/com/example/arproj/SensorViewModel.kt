package com.example.arproj

import android.app.Application
import android.app.UiAutomation
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.Math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class SensorViewModel(application: Application): AndroidViewModel(application) {

    val accLiveData = AccLiveData()

    inner class AccLiveData: LiveData<String>(), SensorEventListener{
        private val sensorManager
            get() = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private lateinit var accelerometer: Sensor
        private lateinit var gyroscope: Sensor
        private lateinit var magnetField: Sensor

        private var accValues = FloatArray(3)
        private var gyroValues = FloatArray(3)
        private var magnetValues = FloatArray(3)

        private var accX = 0.0f
        private var accY = 0.0f
        private var accZ = 0.0f
        private var accTotal = 0.0f
        private var temp = 0.0
        private var tempX = 0.0f
        private var tempY = 0.0f
        private var tempZ = 0.0f
        private var coe = 0.2f
        private var alpha = 0.8f
        private var accPitch = 0.0
        private var accRoll = 0.0
        private var pitch = 0.0
        private var roll = 0.0
        private var yaw = 0.0
        private var timeStamp = 0.0
        private var dt = 0.0

        private var isAccRunning = false
        private var isGyroRuning = false
        private var isMagnetRunning = false

        private val NS2S = 1.0f/1000000000.0f

        override fun onSensorChanged(event: SensorEvent) {

            when(event.sensor){
                accelerometer -> {
                    System.arraycopy(event.values, 0, accValues, 0, event.values.size)
                    if(!isAccRunning)   isAccRunning = true
                }
                gyroscope -> {
                    System.arraycopy(event.values, 0, gyroValues, 0, event.values.size)
                    if(!isGyroRuning)   isGyroRuning = true
                }
                magnetField -> {
                    System.arraycopy(event.values, 0, magnetValues, 0, event.values.size)
                    if(!isMagnetRunning)    isMagnetRunning = true
                }
            }

            if(isAccRunning || isGyroRuning){
//                complementary(event.timestamp.toDouble())
//                val accText = "${String.format("%.6f", accX)} ${String.format("%.6f", accY)} ${String.format("%.6f", accZ)} ${String.format("%.6f", accTotal)}"
//                val gyroText = "${String.format("%.6f", roll)} ${String.format("%.6f", pitch)} ${String.format("%.6f", yaw)}"

                val accText = "${String.format("%.6f", accValues[0])} ${String.format("%.6f", accValues[1])} ${String.format("%.6f", accValues[2])}"
                val gyroText = "${String.format("%.6f", gyroValues[0])} ${String.format("%.6f", gyroValues[1])} ${String.format("%.6f", gyroValues[2])}"
                val magnetText = "${String.format("%.6f", magnetValues[0])} ${String.format("%.6f", magnetValues[1])} ${String.format("%.6f", magnetValues[2])}"

                postValue("$accText,$gyroText,$magnetText")
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
                sm.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                sm.registerListener(this, this.gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
                sm.registerListener(this, this.magnetField, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        override fun onInactive() {
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        private fun complementary(newTimeStamp: Double){
            isAccRunning = false
            isGyroRuning = false
            isMagnetRunning = false

            if(timeStamp == 0.0){
                timeStamp = newTimeStamp
                return
            }
            dt = (newTimeStamp - timeStamp) * NS2S
            timeStamp = newTimeStamp

            accPitch = -kotlin.math.atan2(accValues[0].toDouble(), accValues[2].toDouble()) * 180.0/Math.PI
            accRoll = kotlin.math.atan2(accValues[1].toDouble(), accValues[2].toDouble()) * 180/Math.PI

            temp = (1/coe) * (accPitch - pitch) + gyroValues[1]
            pitch += temp * dt
            temp = (1/coe) * (accRoll - roll)+ gyroValues[0]
            roll += temp * dt
            yaw = gyroValues[2].toDouble()

            tempX = alpha * tempX + (1-alpha) * accValues[0]
            tempY = alpha * tempY + (1-alpha) * accValues[1]
            tempZ = alpha * tempZ + (1-alpha) * accValues[2]

            accX = accValues[0] - tempX
            accY = accValues[1] - tempY
            accZ = accValues[2] - tempZ
            accTotal = sqrt(accX.pow(2) + accY.pow(2) + accZ.pow(2))

        }
    }

}