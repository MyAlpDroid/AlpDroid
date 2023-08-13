package main.java.com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.RoadbookDisplayBinding
import com.alpdroid.huGen10.ui.UIFragment

class RoadBook:UIFragment(500) {

    private  var fragmentBlankBinding: RoadbookDisplayBinding?=null
    private lateinit var alpdroidservices: VehicleServices // Assurez-vous d'avoir initialisé cette instance correctement
    private var isStarted: Boolean = false
    private var startTime: Long = 0
    private var distanceTraveled: Float =0.0f

    lateinit var startButton : Button
    lateinit var compassImageView: ImageView
    lateinit var dureeTextView: TextView
    lateinit var moyenneTextView: TextView
    lateinit var distanceTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = RoadbookDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = fragmentBlankBinding!!.startRoad
        compassImageView = fragmentBlankBinding!!.compassdir
        dureeTextView = fragmentBlankBinding!!.roadduree
        moyenneTextView = fragmentBlankBinding!!.roadmoyenne
        distanceTextView = fragmentBlankBinding!!.roaddistance

        compassImageView.setImageResource(R.drawable.compas_road)

        startButton.setOnClickListener {
            if (!isStarted) {
                isStarted = true
                startTime = System.currentTimeMillis()
                if (AlpdroidApplication.app.isBound) {
                    alpdroidservices = AlpdroidApplication.app.alpdroidData
                    distanceTraveled = alpdroidservices.get_DistanceTotalizer_MM().toFloat()
                    startButton.text = "Stop"
                }
            } else {
                isStarted = false
                startButton.text = "Start"
            }
        }

        timerTask = {
            activity?.runOnUiThread {
                if (isStarted) {
                    alpdroidservices = AlpdroidApplication.app.alpdroidData
                    updateUI()
                }
            }

        }
    }


        fun updateUI() {

        val rotationAngle = alpdroidservices.getCompassOrientation()
        compassImageView.rotation = rotationAngle.toFloat()

        val currentTime = System.currentTimeMillis() - startTime
        val hours = currentTime / 3600000
        val minutes = (currentTime % 3600000) / 60000
        val seconds = (currentTime % 60000) / 1000
        dureeTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        if (isStarted) {
            val currentSpeed = alpdroidservices.get_VehicleSpeed()
            val distance = (alpdroidservices.get_DistanceTotalizer_MM().toFloat() - distanceTraveled)/100
            val averageSpeed = distance / (currentTime / 1000.0) // en km/s

            moyenneTextView.text = String.format("%.2f km/h", averageSpeed * 3600) // convertir en km/h
            distanceTextView.text = String.format("%.3f km", distance)
        } else {
            moyenneTextView.text = "N/A"
            distanceTextView.text = "N/A"
        }




        }




}