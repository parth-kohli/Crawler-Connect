package com.example.arcadecrawler

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

class GyroController(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var xOffset by mutableStateOf(0f)
    var yOffset by mutableStateOf(0f)
    private var lastUpdateTime = 0L
    private val shakeThreshold = 0.5f
    private var isActive by mutableStateOf(false)

    fun start() {
        if (!isActive) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
            isActive = true
            xOffset = 0f
            yOffset = 0f
        }
    }

    fun stop() {
        if (isActive) {
            sensorManager.unregisterListener(this)
            isActive = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE && isActive) {
                val currentTime = System.currentTimeMillis()
                if (lastUpdateTime == 0L) {
                    lastUpdateTime = currentTime
                    return
                }

                val timeDiff = currentTime - lastUpdateTime
                if (timeDiff > 50) { // More frequent updates
                    val x = it.values[0]
                    val y = it.values[1]

                    // Apply smoothing and threshold
                    xOffset = when {
                        abs(x) > shakeThreshold -> (xOffset + x * 5f).coerceIn(-1f, 1f)
                        abs(xOffset) > 0.01f -> xOffset * 0.8f // Stronger dampening
                        else -> 0f
                    }

                    yOffset = when {
                        abs(y) > shakeThreshold -> (yOffset + y * 5f).coerceIn(-1f, 1f)
                        abs(yOffset) > 0.01f -> yOffset * 0.8f
                        else -> 0f
                    }

                    lastUpdateTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}