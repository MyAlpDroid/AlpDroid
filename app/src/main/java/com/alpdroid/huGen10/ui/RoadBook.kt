package com.alpdroid.huGen10.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.RoadInfo
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.RoadbookDisplayBinding

class RoadBook:UIFragment(500) {

    private  var fragmentBlankBinding: RoadbookDisplayBinding?=null
    private lateinit var alpdroidservices: VehicleServices // Assurez-vous d'avoir initialisé cette instance correctement
    private var isStarted: Boolean = false
    private var startTime: Long = 0
     var distanceTraveled: Float =0.0f

    lateinit var startButton : Button
    lateinit var razButton : Button
    lateinit var compassImageView: ImageView
    lateinit var dureeTextView: TextView
    lateinit var moyenneTextView: TextView
    lateinit var distanceTextView: TextView

    private lateinit var roadListView: ListView
    private val roadInfoList: MutableList<RoadInfo> = ArrayList()
    private lateinit var adapter: CustomListAdapter

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
        razButton = fragmentBlankBinding!!.razButton
        compassImageView = fragmentBlankBinding!!.compassdir
        dureeTextView = fragmentBlankBinding!!.roadduree
        moyenneTextView = fragmentBlankBinding!!.roadmoyenne
        distanceTextView = fragmentBlankBinding!!.roaddistance

        compassImageView.setImageResource(R.drawable.compas_road)

        roadListView = fragmentBlankBinding!!.roadList

        adapter = CustomListAdapter(AlpdroidApplication.app, R.layout.list_item, roadInfoList)
        roadListView.adapter = adapter

        var kmDepart =0.0f // Récupérez ces valeurs depuis votre source de données
        var kmStep = 0.0f
        var timeLastStep:Long = 0
        var timePrevStep:Long = 0


        razButton.setOnClickListener {
            if (isStarted) {
                isStarted = false
                startButton.text = "Stop"
                distanceTraveled=0f
                kmDepart=0f
                kmStep=0f
                timeLastStep = 0
                timePrevStep = 0
                startTime=0
                roadInfoList.clear()
                adapter.notifyDataSetChanged()

            }


        }

        startButton.setOnClickListener {

            if (!isStarted) {
                isStarted = true
                startTime = System.currentTimeMillis()
                if (AlpdroidApplication.app.isBound) {
                     kmDepart =0.0f // Récupérez ces valeurs depuis votre source de données
                     kmStep = 0.0f
                    timePrevStep=startTime
                    alpdroidservices = AlpdroidApplication.app.alpdroidData
                    distanceTraveled = alpdroidservices.get_DistanceTotalizer_MM().toFloat()
                    startButton.text = "Next"
                }
            } else {
                 kmDepart = kmStep // Récupérez ces valeurs depuis votre source de données
                 kmStep = alpdroidservices.get_DistanceTotalizer_MM().toFloat()-distanceTraveled
                val currentTime = System.currentTimeMillis()
                timeLastStep = currentTime - timePrevStep
                timePrevStep = currentTime

                // Ajoutez un nouvel élément à la liste
                val roadInfo = RoadInfo(kmDepart, kmStep, timeLastStep)
                roadInfoList.add(roadInfo)

                // Mettez à jour la ListView
                adapter.notifyDataSetChanged()


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

        val rotationAngle = alpdroidservices.getCompassOrientation()*2
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

    class CustomListAdapter(
        context: Context,
        private val resource: Int,
        private val roadInfoList: List<RoadInfo>
    ) : ArrayAdapter<RoadInfo>(context, resource, roadInfoList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val roadInfo = getItem(position)

            val itemView = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

            // Trouver les vues dans le layout list_item.xml (ajustez ces ID en fonction de votre layout)
            val kmDepartTextView:TextView = itemView.findViewById(R.id.km_depart_textview)
            val kmStepTextView: TextView = itemView.findViewById(R.id.km_step_textview)
            val timeLastStepTextView: TextView = itemView.findViewById(R.id.time_last_step_textview)

            // Mettre à jour les vues avec les données de l'élément actuel
            kmDepartTextView.text = "Km Départ: ${roadInfo?.kmDepart}"
            kmStepTextView.text = "Km Step: ${roadInfo?.kmStep}"

            val hours = roadInfo?.timeLastStep?.div(3600000)
            val minutes = (roadInfo?.timeLastStep?.rem(3600000))?.div(60000)
            val seconds = (roadInfo?.timeLastStep?.rem(60000))?.div(1000)
             String.format("%02d:%02d:%02d", hours, minutes, seconds)

            timeLastStepTextView.text = "Time Last Step: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}"

            return itemView
        }
    }


}