package com.alpdroid.huGen10.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.databinding.ConfortDisplayBinding
import com.github.anastr.speedviewlib.TubeSpeedometer


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ConfortDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: ConfortDisplayBinding?=null
    lateinit var battstate:ImageView
    lateinit var batttext:TextView
    var battvalue:Float = 0.0f
    lateinit var enginestate:ImageView
    var enginevalue:Int =0
    var tankvalue:Float = 0.0f
    lateinit var tanklevel:ImageView
    lateinit var tanktext:TextView
    lateinit var washerlevel:ImageView
    lateinit var externaltemp:TextView
    lateinit var internaltemp:TextView
    lateinit var fanspeedstate:ImageView
    lateinit var nextoverhaul:TextView
    lateinit var opendoorFront:ImageView
    lateinit var opendoorRear:ImageView
    lateinit var opendoorLeft:ImageView
    lateinit var opendoorRight:ImageView

    lateinit var climfanspeed:TubeSpeedometer

    lateinit var humidityvalue:TextView

    lateinit var humiditypicture:ImageView

    lateinit var startstopstate : ImageView
    lateinit var escstate : ImageView
    lateinit var absstate:ImageView


    lateinit var press_FL : TextView
    lateinit var press_RL: TextView
    lateinit var press_FR: TextView
    lateinit var press_RR: TextView
    lateinit var temp_FL: TextView
    lateinit var temp_FR: TextView
    lateinit var temp_RL: TextView
    lateinit var temp_RR: TextView
    lateinit var temp_FL2: TextView
    lateinit var temp_FR2: TextView
    lateinit var temp_RL2: TextView
    lateinit var temp_RR2: TextView
    lateinit var oddo_Rate: TextView
    lateinit var fuel_inst: TextView
    lateinit var fuel_level: TextView


    lateinit var cardessin : ImageView

    // offset b2r1S
    var offset_tyretemp: Int = 0
    var offset_tyrepress: Float = 0.0f
    var toggle_braketemp: Boolean = false
    var offset_tpms:Int = 0 // si différence entre connect & tpms Alpine de 15 ?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: ConfortDisplayBinding = ConfortDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding

        try {
            if (this.context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this.context as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    101
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return binding.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        fragmentBlankBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        cardessin = fragmentBlankBinding!!.dessinCar
        temp_FL = fragmentBlankBinding!!.textTempFL
        temp_FR = fragmentBlankBinding!!.textTempFR
        temp_RL = fragmentBlankBinding!!.textTempRL
        temp_RR = fragmentBlankBinding!!.textTempRR

        oddo_Rate = fragmentBlankBinding!!.textOddoRate
        fuel_inst = fragmentBlankBinding!!.textFuelInst


        externaltemp=fragmentBlankBinding!!.externalTemp
        internaltemp=fragmentBlankBinding!!.internalTemp

        battstate=fragmentBlankBinding!!.batterieState
        batttext=fragmentBlankBinding!!.batterieValue


        tanklevel=fragmentBlankBinding!!.gastankLevel
        tanktext=fragmentBlankBinding!!.tankValue

        enginestate=fragmentBlankBinding!!.engineState

        washerlevel=fragmentBlankBinding!!.washerLevel!!

        fanspeedstate=fragmentBlankBinding!!.fanSpeedstate

        nextoverhaul=fragmentBlankBinding!!.nextOverhaulKM

        opendoorFront=fragmentBlankBinding!!.cardoorFront
        opendoorRear=fragmentBlankBinding!!.cardoorRear
        opendoorLeft=fragmentBlankBinding!!.cardoorLeft
        opendoorRight=fragmentBlankBinding!!.cardoorRight

        startstopstate=fragmentBlankBinding!!.startstopState!!
        escstate=fragmentBlankBinding!!.escState!!
        absstate=fragmentBlankBinding!!.absstate!!

        climfanspeed=fragmentBlankBinding!!.fanspeedGauge!!

        humidityvalue=fragmentBlankBinding!!.humidity!!
        humiditypicture=fragmentBlankBinding!!.humidityState!!

        humiditypicture.setImageResource(R.drawable.humid_clim)


        loadPreferences()

        // Ajout d'un listener sur cardessin
        cardessin.setOnClickListener {
            showPopupDialog()
        }

        press_FL = fragmentBlankBinding!!.textPressFL
        press_RL = fragmentBlankBinding!!.textPressRL
        press_FR = fragmentBlankBinding!!.textPressFR
        press_RR = fragmentBlankBinding!!.textPressRR



        temp_FL2 = fragmentBlankBinding!!.textTempFL2
        temp_FR2 = fragmentBlankBinding!!.textTempFR2
        temp_RL2 = fragmentBlankBinding!!.textTempRL2
        temp_RR2 = fragmentBlankBinding!!.textTempRR2

        fragmentBlankBinding!!.imageTempFl2!!.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding!!.imagePressFl!!.setImageResource(R.drawable.unite_bar)
        fragmentBlankBinding!!.imageTempFl!!.setImageResource(R.drawable.degre_c)

        fragmentBlankBinding!!.imageTempRl2!!.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding!!.imagePressRl!!.setImageResource(R.drawable.unite_bar)
        fragmentBlankBinding!!.imageTempRl!!.setImageResource(R.drawable.degre_c)

        fragmentBlankBinding!!.imageTempFR2!!.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding!!.imagePressFR!!.setImageResource(R.drawable.unite_bar)
        fragmentBlankBinding!!.imageTempFr!!.setImageResource(R.drawable.degre_c)

        fragmentBlankBinding!!.imageTempRR2!!.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding!!.imagePressRR!!.setImageResource(R.drawable.unite_bar)
        fragmentBlankBinding!!.imageTempRr!!.setImageResource(R.drawable.degre_c)

        timerTask = {
            activity?.runOnUiThread {
            if (AlpdroidApplication.app.isBound) {

                val alpineServices= AlpdroidApplication.app.alpdroidData

                val flbrake_temp:Int = (alpineServices.get_FrontLeftBrakeTemperature() * 5) - 50
                val frbrake_temp:Int = (alpineServices.get_FrontRightBrakeTemperature() * 5) - 50
                val rlbrake_temp:Int = (alpineServices.get_RearLeftBrakeTemperature() * 5) - 50
                val rrbrake_temp:Int = (alpineServices.get_RearRightBrakeTemperature() * 5) - 50

                val flbrake_press:Int = alpineServices.get_FrontLeftWheelPressure_V2() * 30
                val frbrake_press:Int = alpineServices.get_FrontRightWheelPressure_V2() * 30
                val rlbrake_press:Int = alpineServices.get_RearLeftWheelPressure_V2() * 30
                val rrbrake_press:Int = alpineServices.get_RearRightWheelPressure_V2() * 30

                val tyretemp_fl2:Int =alpineServices.get_TyreTemperature1()
                val tyretemp_fr2:Int =alpineServices.get_TyreTemperature2()
                val tyretemp_rl2:Int =alpineServices.get_TyreTemperature3()
                val tyretemp_rr2:Int =alpineServices.get_TyreTemperature4()

                if (toggle_braketemp) {
                    temp_FL.visibility=View.INVISIBLE
                    temp_RL.visibility=View.INVISIBLE
                    temp_FR.visibility=View.INVISIBLE
                    temp_RR.visibility=View.INVISIBLE
                    fragmentBlankBinding!!.imageTempRr.visibility=View.INVISIBLE
                    fragmentBlankBinding!!.imageTempFl.visibility=View.INVISIBLE
                    fragmentBlankBinding!!.imageTempRl.visibility=View.INVISIBLE
                    fragmentBlankBinding!!.imageTempFr.visibility=View.INVISIBLE

                }
                else
                {
                    temp_FL.visibility=View.VISIBLE
                    temp_RL.visibility=View.VISIBLE
                    temp_FR.visibility=View.VISIBLE
                    temp_RR.visibility=View.VISIBLE
                    fragmentBlankBinding!!.imageTempRr.visibility=View.VISIBLE
                    fragmentBlankBinding!!.imageTempFl.visibility=View.VISIBLE
                    fragmentBlankBinding!!.imageTempRl.visibility=View.VISIBLE
                    fragmentBlankBinding!!.imageTempFr.visibility=View.VISIBLE
                }

                // temp
                temp_FL2.text= String.format("%d", tyretemp_fl2+offset_tyretemp)
                temp_FR2.text= String.format("%d",tyretemp_fr2+offset_tyretemp)
                temp_RL2.text= String.format("%d",tyretemp_rl2+offset_tyretemp)
                temp_RR2.text= String.format("%d",tyretemp_rr2+offset_tyretemp)

                temp_FL.text = String.format(
                    "%d",
                    flbrake_temp
                )

                if (flbrake_press<2000)
                    press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else if (flbrake_press<2400)
                    press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                else
                    press_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                if (rlbrake_press<2000)
                    press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else if (rlbrake_press<2400)
                    press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                else
                    press_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                if (rrbrake_press<2000)
                    press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else if (rrbrake_press<2400)
                    press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                else
                    press_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                if (frbrake_press<2000)
                    press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else if (frbrake_press<2400)
                    press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                else
                    press_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                if (tyretemp_fr2<35)
                    temp_FR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else if (tyretemp_fr2<90)
                    temp_FR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.vert, null))
                else
                    temp_FR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                press_RL.text = String.format(
                    "%.2f",
                    offset_tyrepress + (rlbrake_press.toFloat()/1000)
                )

                if (tyretemp_fl2<35)
                    temp_FL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (tyretemp_fl2<90)
                    temp_FL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_FL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                if (tyretemp_rr2<35)
                    temp_RR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (tyretemp_rr2<90)
                    temp_RR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_RR2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                press_FL.text = String.format(
                    "%.2f",
                    offset_tyrepress + (flbrake_press.toFloat()/1000)
                )


                press_RR.text = String.format(
                    "%.2f",
                    offset_tyrepress + (rrbrake_press.toFloat()/1000)
                )

                if (tyretemp_rl2<35)
                    temp_RL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (tyretemp_rl2<90)
                    temp_RL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_RL2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                press_FR.text = String.format(
                    "%.2f",
                    offset_tyrepress +  (frbrake_press.toFloat()/1000)
                )

                if (flbrake_temp<120)
                    temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (flbrake_temp<250)
                    temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_FL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                if (rlbrake_temp<120)
                    temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (rlbrake_temp<250)
                    temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_RL.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                temp_RL.text = String.format(
                    "%d",
                    rlbrake_temp
                )

                temp_FR.text = String.format(
                    "%d",
                    frbrake_temp
                )

                if (frbrake_temp<120)
                    temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (frbrake_temp<250)
                    temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_FR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                temp_RR.text = String.format(
                    "%d",
                    rrbrake_temp
                )

                if (rrbrake_temp<120)
                    temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                else if (rrbrake_temp<250)
                    temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                else
                    temp_RR.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))

                oddo_Rate.text = String.format(" %.2f km", (alpineServices.get_DistanceTotalizer_MM()).toFloat()/100)

                val fuel_instant = (alpineServices.get_FuelConsumption().toFloat()/0.0125/1000).toFloat()

                fuel_inst.text = String.format(
                    " %.1f l/100",
                    fuel_instant
                )

                climfanspeed.speedTo(alpineServices.get_IH_CoolingFanSpeed(),2)

                humidityvalue.text=String.format("%.1f %%", alpineServices.get_IH_humidity())

                externaltemp.text=String.format(
                    "%d °C",
                    alpineServices.get_MM_ExternalTemp()-40)

                internaltemp.text=String.format(
                    "%.1f °C",
                    alpineServices.get_internalTemp())

                battvalue= 6+((alpineServices.get_BatteryVoltage_V2())/16.67).toFloat()

                tankvalue= alpineServices.get_FuelLevelDisplayed().toFloat()


                nextoverhaul.text= String.format(" %d Km ou %d Jours", alpineServices.get_MilageMinBeforeOverhaul()*250,alpineServices.get_TimeBeforeOverhaul())

                battstate.setImageResource(R.drawable.batterie_ok)
                batttext.text=String.format(
                    "%.2f V",
                    battvalue)

                if (battvalue<9.5)
                    battstate.setImageResource(R.drawable.batterie_ko)
                else if (battvalue<13.5)
                    battstate.setImageResource(R.drawable.batterie_norm)

                tanklevel.setImageResource(R.drawable.gastank_levelfull)
                tanktext.text=String.format(
                    "%.2f l",
                   tankvalue)

                if (tankvalue<5)
                    tanklevel.setImageResource(R.drawable.gastank_levellow)
                else if (tankvalue<15)
                    tanklevel.setImageResource(R.drawable.gastank_levelmed)

                enginestate.setImageResource(R.drawable.engine_ok)
                enginevalue= alpineServices.get_GlobalVehicleWarningState()
                if (enginevalue!=0)
                    enginestate.setImageResource(R.drawable.engine_check)

                washerlevel.setImageResource(R.drawable.washerlevel_norm)

                if (alpineServices.get_WasherLevelWarningState())
                    washerlevel.setImageResource(R.drawable.washerlevel_low)

                val id =
                    resources.getIdentifier(
                        "enginefanspeed_on${alpineServices.get_CoolingFanSpeedStatus()}",
                        "drawable",
                        context?.packageName
                    )

                fanspeedstate.setImageResource(id)

                if (alpineServices.get_FrontLeftDoorOpenWarning()>0)
                    opendoorLeft.setImageResource((R.drawable.cardoor_leftopen))
                else
                    opendoorLeft.setImageResource((R.drawable.cardoor_left))
                if (alpineServices.get_FrontRightDoorOpenWarning()>0)
                    opendoorRight.setImageResource(R.drawable.cardoor_rightopen)
                else
                    opendoorRight.setImageResource(R.drawable.cardoor_right)
                if (alpineServices.get_BootOpenWarning()>0)
                    opendoorRear.setImageResource(R.drawable.cardoor_rearopen)
                else
                    opendoorRear.setImageResource(R.drawable.cardoor_rear)

                opendoorFront.setImageResource((R.drawable.cardoor_front))

                when (alpineServices.get_StartAutoAuthorization()) {
                    0 -> startstopstate.setImageResource(R.drawable.sas_available)
                    1 -> startstopstate.setImageResource(R.drawable.sas_on)
                    2 -> startstopstate.setImageResource(R.drawable.sas_off)
                    3 -> startstopstate.setImageResource(R.drawable.sas_off)
                }

                if (alpineServices.get_ESPDeactivatedByDriverForDisplay())
                    escstate.setImageResource(R.drawable.esc_off)
                else
                    escstate.setImageResource(R.drawable.esc_on)

            }

            }
        }
   }

    private fun showPopupDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Paramètres")

        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.parampopup_layout, null)
        builder.setView(dialogView)

        val offsetTyrePressureEditText: EditText = dialogView.findViewById(R.id.offsetTyrePressureEditText)
        val offsetTyreTempEditText: EditText = dialogView.findViewById(R.id.offsetTyreTempEditText)
        val offsetTpmsEditText:EditText = dialogView.findViewById(R.id.offsetTpmsEditText)
        val switchBrakeTemp: Switch = dialogView.findViewById(R.id.switchBrakeTemp)


        // Pré-remplir les champs avec les valeurs actuelles
        offsetTyrePressureEditText.setText(offset_tyrepress.toString())
        offsetTyreTempEditText.setText(offset_tyretemp.toString())
        offsetTpmsEditText.setText(offset_tpms.toString())
        switchBrakeTemp.isChecked = toggle_braketemp

        builder.setPositiveButton("OK") { _, _ ->
            // Capturer les valeurs des champs
            offset_tyrepress = offsetTyrePressureEditText.text.toString().toFloat()
            offset_tyretemp = (offsetTyreTempEditText.text).toString().toInt()
            offset_tpms = (offsetTpmsEditText.text).toString().toInt()
            toggle_braketemp = switchBrakeTemp.isChecked

            // Stocker les valeurs dans SharedPreferences
            saveToSharedPreferences()

            // Mettre à jour vos variables globales si nécessaire

            // Afficher un message Toast pour informer l'utilisateur
            Toast.makeText(requireContext(), "Paramètres sauvegardés", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Annuler", null)

        builder.show()
    }
    private fun loadPreferences() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("EnginePrefs", Context.MODE_PRIVATE)

        offset_tyretemp = sharedPreferences.getInt("offset_tyretemp", 0)
        offset_tyrepress = sharedPreferences.getFloat("offset_tyrepress", 0.0f)
        toggle_braketemp = sharedPreferences.getBoolean("toggle_braketemp", false)
        offset_tpms = sharedPreferences.getInt("offset_tpms", 0)

    }

    private fun saveToSharedPreferences() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("EnginePrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putInt("offset_tyretemp", offset_tyretemp)
        editor.putFloat("offset_tyrepress", offset_tyrepress)
        editor.putBoolean("toggle_braketemp", toggle_braketemp)
        editor.putInt("offset_tpms", offset_tpms)

        editor.apply()
    }
}