package com.example.sensorapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class SensorActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: MaterialToolbar

    // Wybierz 2 czujniki do obsługi w SensorDetailsActivity
    private val sensorTypeA = Sensor.TYPE_LIGHT
    private val sensorTypeB = Sensor.TYPE_PROXIMITY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_activity)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.sensor_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)

        recyclerView.adapter = SensorAdapter(sensorList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sensor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_count -> {
                supportActionBar?.title = "SensorApp (${sensorList.size})"
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class SensorHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.sensor_icon)
        private val nameText: TextView = view.findViewById(R.id.sensor_name)

        private lateinit var sensor: Sensor

        fun bind(sensor: Sensor) {
            this.sensor = sensor
            nameText.text = sensor.name

            val isChosen = sensor.type == sensorTypeA || sensor.type == sensorTypeB
            nameText.alpha = if (isChosen) 1.0f else 0.6f

            // Kliknięcie:
            // - magnetometr => LocationActivity
            // - wyróżnione 2 czujniki => SensorDetailsActivity
            // - reszta => komunikat
            itemView.setOnClickListener {
                if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    startActivity(Intent(this@SensorActivity, LocationActivity::class.java))
                    return@setOnClickListener
                }

                if (isChosen) {
                    val i = Intent(this@SensorActivity, SensorDetailsActivity::class.java)
                    i.putExtra(SensorDetailsActivity.EXTRA_SENSOR_TYPE, sensor.type)
                    startActivity(i)
                } else {
                    Toast.makeText(
                        this@SensorActivity,
                        "Kliknij czujnik LIGHT/PROXIMITY albo MAGNETIC_FIELD.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Long click => AlertDialog ze szczegółami
            itemView.setOnLongClickListener {
                AlertDialog.Builder(this@SensorActivity)
                    .setTitle(sensor.name)
                    .setMessage(
                        "Producent: ${sensor.vendor}\n" +
                                "Maks. zakres: ${sensor.maximumRange}\n" +
                                "Typ: ${sensor.type}"
                    )
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
        }
    }

    private inner class SensorAdapter(private val sensors: List<Sensor>) :
        RecyclerView.Adapter<SensorHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.sensor_list_item, parent, false)
            return SensorHolder(view)
        }

        override fun onBindViewHolder(holder: SensorHolder, position: Int) {
            holder.bind(sensors[position])
        }

        override fun getItemCount(): Int = sensors.size
    }
}
