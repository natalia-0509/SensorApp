package com.example.sensorapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SensorDetailsActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        const val EXTRA_SENSOR_TYPE = "extraSensorType"
    }

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    private lateinit var labelTextView: TextView
    private lateinit var valuesTextView: TextView

    private var sensorType: Int = Sensor.TYPE_LIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)

        labelTextView = findViewById(R.id.sensor_label)
        valuesTextView = findViewById(R.id.sensor_values)

        sensorType = intent.getIntExtra(EXTRA_SENSOR_TYPE, Sensor.TYPE_LIGHT)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(sensorType)

        if (sensor == null) {
            labelTextView.text = getString(R.string.missing_sensor)
        } else {
            labelTextView.text = "Sensor: ${sensor!!.name} (type=$sensorType)"
        }
    }

    override fun onStart() {
        super.onStart()
        sensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != sensorType) return
        val values = event.values.joinToString(prefix = "[", postfix = "]") { v -> "%.3f".format(v) }
        valuesTextView.text = "Wartości: $values"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // możesz dodać Log.d(...) jeśli wymagane w sprawozdaniu
    }
}
