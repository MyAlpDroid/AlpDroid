package com.alpdroid.huGen10.ui


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.CustomDialogBinding
import com.alpdroid.huGen10.databinding.EngineDisplayBinding
import com.github.anastr.speedviewlib.ImageSpeedometer
import com.github.anastr.speedviewlib.ProgressiveGauge
import com.github.anastr.speedviewlib.components.indicators.ImageIndicator
import com.github.anastr.speedviewlib.components.note.Note
import com.github.anastr.speedviewlib.components.note.TextNote
import com.google.android.material.slider.RangeSlider


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class EngineDisplay : UIFragment(250)
{

    private lateinit var alpineServices : VehicleServices
    private lateinit var fragmentBlankBinding: EngineDisplayBinding


    private lateinit var press_FL: TextView
    private lateinit var press_RL: TextView
    private lateinit var press_FR: TextView
    private lateinit var press_RR: TextView

    private lateinit var temp_FL2: TextView
    private lateinit var temp_FR2: TextView
    private lateinit var temp_RL2: TextView
    private lateinit var temp_RR2: TextView

    private lateinit var frontleftrow : LinearLayout
    private lateinit var frontrightrow : LinearLayout
    private lateinit var frontleftrowpress : LinearLayout
    private lateinit var frontrighttrowpress : LinearLayout

    private lateinit var rearleftrow : LinearLayout
    private lateinit var rearrightrow : LinearLayout
    private lateinit var rearleftrowpress : LinearLayout
    private lateinit var rearrighttrowpress : LinearLayout

    private lateinit var tyrelayout: TableLayout

    lateinit var fuel_level: TextView
    lateinit var gear_active: ImageView
    lateinit var speed : TextView

    lateinit var oil_temp : ImageSpeedometer
    lateinit var cool_temp : ImageSpeedometer
    lateinit var intake_temp : ImageSpeedometer
    lateinit var gear_temp : ImageSpeedometer
    lateinit var otherJauge3: ImageSpeedometer
    lateinit var speedthrottle : ProgressiveGauge
    lateinit var brakethrottle : ProgressiveGauge

    var switchLevelOn : Boolean = true

    var speed_100:Int = 0
    var fuelgauge:Int =0

    // offset b2r1S
    var offset_tyretemp: Int = 0
    var offset_tyrepress: Float = 0.0f
    var toggle_braketemp: Boolean = false
    var offset_tpms:Int = 0

    private var alert_set = false
    private var blink_switch = false
    private var alert_ack = false

    lateinit var tanklevel:ImageView

    private var tempLimitLow = 10
    private var pressLimitHigh = 2100
    private var tempLimitHigh = 70
    private var pressLimitLow = 1900

    private var selectpressLimitLow=0.0f
    private var selectpressLimitHigh=0.0f
    private var selecttempLimitHigh=0.0f
    private var selecttempLimitLow=0.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = EngineDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun showPopupDialog()
    {

        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        val tempOffsetSeekBar = dialogBinding.tempOffsetDoubleSeekBar
        val pressOffsetSeekBar = dialogBinding.pressOffsetDoubleSeekBar
        val tempOffsetValue = dialogBinding.tempOffsetValue
        val pressOffsetValue = dialogBinding.pressOffsetValue


        loadPreferences()

        dialogBinding.switchLevelIndicator.isChecked=switchLevelOn

        selectpressLimitHigh = pressLimitHigh/1000f
        selectpressLimitLow = pressLimitLow/1000f
        selecttempLimitHigh = tempLimitHigh.toFloat()
        selecttempLimitLow  = tempLimitLow.toFloat()

        tempOffsetSeekBar.tickInactiveRadius=3

        tempOffsetSeekBar.trackActiveTintList= ColorStateList.valueOf((this.requireContext().getResources().getColor(R.color.vert)))

        tempOffsetSeekBar.trackHeight=12

        pressOffsetSeekBar.tickInactiveRadius=3

        pressOffsetSeekBar.trackActiveTintList= ColorStateList.valueOf((this.requireContext().getResources().getColor(R.color.vert)))

        pressOffsetSeekBar.trackHeight=12

        tempOffsetSeekBar.setValues(selecttempLimitLow,selecttempLimitHigh)

        pressOffsetSeekBar.setValues(selectpressLimitLow,selectpressLimitHigh)

        pressOffsetValue.text =
            "Zone de fonctionnement : ${String.format("%.2f", selectpressLimitLow)} bar - ${String.format("%.2f", selectpressLimitHigh)} bar"

        tempOffsetValue.text =
            "Zone de fonctionnement : ${String.format("%d", selecttempLimitLow.toInt())} °C - ${String.format("%d", selecttempLimitHigh.toInt())} °C"

        // Configurer les SeekBars

        tempOffsetSeekBar.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being stopped
            }


        })

        pressOffsetSeekBar.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed
            selectpressLimitLow = rangeSlider.values.min()
            selectpressLimitHigh = rangeSlider.values.max()

            pressOffsetValue.text =
                "Zone de fonctionnement : ${String.format("%.2f", selectpressLimitLow)} bar - ${String.format("%.2f", selectpressLimitHigh)} bar"

        }

        tempOffsetSeekBar.addOnChangeListener { slider, value, fromUser ->

            // Responds to when slider's value is changed

            selecttempLimitLow = slider.values.min()
            selecttempLimitHigh = slider.values.max()

            tempOffsetValue.text =
                "Zone de fonctionnement : ${String.format("%d", selecttempLimitLow.toInt())} °C - ${String.format("%d", selecttempLimitHigh.toInt())} °C"


        }


        // Créer la boîte de dialogue
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("OK") { _, _ ->

                pressLimitLow=(selectpressLimitLow*1000f).toInt()
                pressLimitHigh=(selectpressLimitHigh*1000f).toInt()
                tempLimitLow=selecttempLimitLow.toInt()
                tempLimitHigh=selecttempLimitHigh.toInt()

                switchLevelOn=dialogBinding.switchLevelIndicator.isChecked

                saveToSharedPreferences()

            }
            .setNegativeButton("Annuler", null)
            .create()

        alertDialog.show()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var press_color:Int
        var press_val:Int
        var temp_color:Int
        var tyre_check: Int

        var tyre_calul4alert:Boolean



        press_FL = fragmentBlankBinding.textPressFL
        press_RL = fragmentBlankBinding.textPressRL
        press_FR = fragmentBlankBinding.textPressFR
        press_RR = fragmentBlankBinding.textPressRR



        temp_FL2 = fragmentBlankBinding.textTempFL2
        temp_FR2 = fragmentBlankBinding.textTempFR2
        temp_RL2 = fragmentBlankBinding.textTempRL2
        temp_RR2 = fragmentBlankBinding.textTempRR2


        frontleftrow=fragmentBlankBinding.frontleftrow
        frontleftrowpress=fragmentBlankBinding.frontlefttrowpress

        frontrightrow=fragmentBlankBinding.frontrightrow
        frontrighttrowpress=fragmentBlankBinding.frontrightrowpress

        rearleftrow=fragmentBlankBinding.rearleftrow
        rearleftrowpress=fragmentBlankBinding.rearlefttrowpress

        rearrightrow=fragmentBlankBinding.rearrightrow
        rearrighttrowpress=fragmentBlankBinding.rearrightrowpress

        tyrelayout=fragmentBlankBinding.tableLayout

        // Ajout d'un listener sur layout pneus/temp.
        tyrelayout.setOnClickListener {

            showPopupDialog()
        }

        speed = fragmentBlankBinding.textSpeed


        oil_temp = fragmentBlankBinding.OILJauge
        cool_temp = fragmentBlankBinding.CooLJauge
        intake_temp = fragmentBlankBinding.IntakeJauge
        gear_temp = fragmentBlankBinding.GearJauge

        fuel_level = fragmentBlankBinding.textFueLevel

        gear_active = fragmentBlankBinding.gearActive


        speedthrottle = fragmentBlankBinding.throttlePress
        brakethrottle = fragmentBlankBinding.brakePress

        otherJauge3 = fragmentBlankBinding.OilPressure

        tanklevel= fragmentBlankBinding.gastankLevel


        fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.ps43)
        fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.ps43)

        fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.ps43)
        fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.ps43)

        fragmentBlankBinding.imageViewTempFL2.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageViewPressFL.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageViewTempRL2.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageViewPressRL.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageViewTempFR2.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageViewPressFR.setImageResource(R.drawable.unite_bar)

        fragmentBlankBinding.imageViewTempRR2.setImageResource(R.drawable.degre_c)
        fragmentBlankBinding.imageViewPressRR.setImageResource(R.drawable.unite_bar)

        loadPreferences()

        val imageIndicatorBlue = getDrawable(AlpdroidApplication.app, R.drawable.image_indicator2)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorBlue != null) {
            oil_temp.indicator = imageIndicatorBlue
            intake_temp.indicator = imageIndicatorBlue
        }

        val imageIndicatorWhite = getDrawable(AlpdroidApplication.app,R.drawable.image_indicator_white)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorWhite != null) {
            cool_temp.indicator = imageIndicatorWhite
        }

        val imageIndicatorRed = getDrawable(AlpdroidApplication.app,R.drawable.image_indicator_red)?.let {
            ImageIndicator(
                AlpdroidApplication.app,
                it
            )
        }

        if (imageIndicatorRed != null) {
            gear_temp.indicator = imageIndicatorRed
            otherJauge3.indicator = imageIndicatorRed
        }


        val snackbar_oilalert = AlertDialog.Builder(this.context)
            .setTitle(R.string.engine_alert_title)
            .setMessage(R.string.engine_alert_text)
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int ->

                fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                alert_ack=true
            }



        val snackbar_tyrealert = AlertDialog.Builder(this.context)
            .setTitle(R.string.engine_alert_title)
            .setMessage(R.string.tyre_alert_text)
            .setPositiveButton(
                android.R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int ->

                fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                alert_ack=true
                tyre_calul4alert=false

            }

        alert_ack=false
        alert_set=false


        timerTask = {
            activity?.runOnUiThread {
                if (AlpdroidApplication.app.isBound)
                {

                    alpineServices = AlpdroidApplication.app.alpdroidData


                    val flbrake_press:Int = alpineServices.get_FrontLeftWheelPressure_V2() * 30
                    val frbrake_press:Int = alpineServices.get_FrontRightWheelPressure_V2() * 30
                    val rlbrake_press:Int = alpineServices.get_RearLeftWheelPressure_V2() * 30
                    val rrbrake_press:Int = alpineServices.get_RearRightWheelPressure_V2() * 30

                    val tyretemp_fl2:Int =alpineServices.get_TyreTemperature1()
                    val tyretemp_fr2:Int =alpineServices.get_TyreTemperature2()
                    val tyretemp_rl2:Int =alpineServices.get_TyreTemperature3()
                    val tyretemp_rr2:Int =alpineServices.get_TyreTemperature4()

                    // temp
                    temp_FL2.text = if (tyretemp_fl2 != -30 && tyretemp_fl2 != 97) {
                        String.format("%d",tyretemp_fl2 + offset_tyretemp)
                    } else {
                        "--"
                    }

                    temp_FR2.text= if (tyretemp_fr2 != -30 && tyretemp_fr2 != 97) {
                        String.format("%d",tyretemp_fr2 + offset_tyretemp)
                    } else {
                        "--"
                    }

                    temp_RL2.text= if (tyretemp_rl2 != -30 && tyretemp_rl2 != 97) {
                        String.format("%d",tyretemp_rl2 + offset_tyretemp)
                    } else {
                        "--"
                    }
                    temp_RR2.text= if (tyretemp_rr2 != -30 && tyretemp_rr2 != 97) {
                        String.format("%d",tyretemp_rr2 + offset_tyretemp)
                    } else {
                        "--"
                    }



                    // Mode
                    val rst_vehicleMode=alpineServices.get_RST_VehicleMode()

                    var psi_limit_low_AVAR=pressLimitLow
                    var psi_limit_mid_AV=(pressLimitHigh+pressLimitLow)/2
                    var psi_limit_mid_AR=(pressLimitHigh+pressLimitLow)/2
                    var psi_limit_high_AV=pressLimitHigh
                    var psi_limit_high_AR=pressLimitHigh

                    var tyretemp_limit_low=tempLimitLow
                    var tyretemp_limit_mid_step1=tempLimitLow+((tempLimitHigh-tempLimitLow)/3)
                    var tyretemp_limit_mid_step2=tyretemp_limit_mid_step1+((tempLimitHigh-tempLimitLow)/3)
                    var tyretemp_limit_high=tempLimitHigh

                    tyretemp_limit_low-=offset_tpms
                    tyretemp_limit_mid_step1-=offset_tpms
                    tyretemp_limit_mid_step2-=offset_tpms
                    tyretemp_limit_high-=offset_tpms

                    var oil_alert=125

                    tyre_calul4alert=false

                    tyre_check=0

                    if (switchLevelOn)
                        if (rst_vehicleMode!=3)
                        {
                            // affectation des couleurs pressions & temp pour conduite normal / sport

                            when {
                                flbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
                                    tyre_check = 3
                                }
                                flbrake_press < psi_limit_mid_AV -> {
                                    press_color = R.color.vert
                                    tyre_check = 1
                                }
                                flbrake_press < psi_limit_high_AV -> {
                                    press_color = R.color.orange
                                    tyre_check = 2
                                }
                                else -> {
                                    press_color = R.color.rouge // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                                    tyre_check = 3
                                }
                            }

                            frontleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            tyre_check=(tyre_check shl 3)

                            when {
                                frbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                frbrake_press < psi_limit_mid_AV -> {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                frbrake_press < psi_limit_high_AV -> {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                else -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }     // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            tyre_check=(tyre_check shl 3)

                            when {
                                rlbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rlbrake_press < psi_limit_mid_AV -> {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                rlbrake_press < psi_limit_high_AV -> {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                else -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }     // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            tyre_check=(tyre_check shl 3)


                            when {
                                rrbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rrbrake_press < psi_limit_mid_AV -> {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                rrbrake_press < psi_limit_high_AV -> {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                else -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }  // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            when {
                                tyretemp_fl2 < tyretemp_limit_low -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check  or (2 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check  or (1 shl 9)
                                }
                                else -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 9)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }


                            frontleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_fr2 < tyretemp_limit_low ->  {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check  or (2 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check  or (1 shl 6)
                                }
                                else -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 6)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            frontrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rl2 < tyretemp_limit_low -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check  or (2 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step2 ->{
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check  or (1 shl 3)
                                }
                                else -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 3)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            rearleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rr2 < tyretemp_limit_low -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check  or 2
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check  or 1
                                }
                                else -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            rearrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))


                        }
                        else
                        {

                            // affectation des couleurs pressions & temp pour conduite track

                            when {
                                flbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                flbrake_press < psi_limit_mid_AV ->  {
                                    press_color =R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                flbrake_press < psi_limit_high_AV ->  {
                                    press_color =R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (flbrake_press > psi_limit_high_AV  )
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                frbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                frbrake_press < psi_limit_mid_AV ->  {
                                    press_color =R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                frbrake_press < psi_limit_high_AV ->  {
                                    press_color =R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (frbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                rlbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rlbrake_press < psi_limit_mid_AV ->  {
                                    press_color =R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                rlbrake_press < psi_limit_high_AV ->  {
                                    press_color =R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }
                            rearleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rlbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                rrbrake_press < psi_limit_low_AVAR -> {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rrbrake_press < psi_limit_mid_AV ->  {
                                    press_color =R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                rrbrake_press < psi_limit_high_AV ->  {
                                    press_color =R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color =R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rrbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            when {
                                tyretemp_fl2 < tyretemp_limit_low -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step1 -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check or (1 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_high -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 9)
                                }
                                else ->{
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 9)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            frontleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            if (tyretemp_fl2 > tyretemp_limit_high)
                                tyre_calul4alert=true


                            when {
                                tyretemp_fr2 < tyretemp_limit_low -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step1 -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check or (1 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_high -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 6)
                                }
                                else ->{
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 6)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_fr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            frontrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rl2 < tyretemp_limit_low -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step1 -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check or (1 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_high -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or (2 shl 3)
                                }
                                else ->{
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or (3 shl 3)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_rl2 > tyretemp_limit_high)
                                tyre_calul4alert=true

                            rearleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rr2 < tyretemp_limit_low -> {
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check  or 3
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step1 -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color=R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                tyretemp_rr2 < tyretemp_limit_high -> {
                                    temp_color=R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                else ->{
                                    temp_color=R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }


                            if (tyretemp_rr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            rearrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                        }
                    else
                    {
                        frontrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        frontleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        rearleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        rearrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        frontleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        frontrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        rearleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        rearrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.fui_transparent, null))
                        tyre_check=0

                    }

                    press_FL.text = if (flbrake_press<4500) String.format(
                        "%.2f",
                        offset_tyrepress +  (flbrake_press.toFloat()/1000)
                    ) else "--"

                    press_RL.text = if (rlbrake_press<4500) String.format(
                        "%.2f",
                        offset_tyrepress + (rlbrake_press.toFloat()/1000)
                    ) else "--"


                    press_FR.text = if (frbrake_press<4500) String.format(
                        "%.2f",
                        offset_tyrepress + (frbrake_press.toFloat()/1000)
                    ) else "--"


                    press_RR.text = if (rrbrake_press<4500) String.format(
                        "%.2f",
                        offset_tyrepress + (rrbrake_press.toFloat()/1000)
                    ) else "--"





                    when ((tyre_check shr 9) and 0b111)
                    {
                        1->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.ps43vert)
                        2->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.ps43jaune)
                        3->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.ps43rouge)
                        else
                        -> fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.ps43)
                    }

                    when ((tyre_check shr 6) and 0b111)
                    {
                        1->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.ps43vert)
                        2->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.ps43jaune)
                        3->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.ps43rouge)
                        else
                        -> fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.ps43)
                    }


                    when ((tyre_check shr 3) and 0b111)
                    {
                        1->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.ps43vert)
                        2->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.ps43jaune)
                        3->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.ps43rouge)
                        else
                        -> fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.ps43)
                    }

                    when ((tyre_check) and 0b111)
                    {
                        1->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.ps43vert)
                        2->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.ps43jaune)
                        3->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.ps43rouge)
                        else
                        -> fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.ps43)
                    }

                    if (!alert_ack && !alert_set && tyre_calul4alert) {
                        alert_set = true
                        snackbar_tyrealert.show()

                    }
                    else
                    {
                        if (!tyre_calul4alert)
                        {
                            alert_set=false
                            alert_ack=false
                        }
                    }

                    oil_temp.speedTo((alpineServices.get_OilTemperature() - 40).toFloat())


                    if ((alpineServices.get_OilTemperature() - 40)<85)
                    {
                        val note = TextNote(AlpdroidApplication.app, (alpineServices.get_OilTemperature() - 40).toString()+"°C")
                            .setPosition(Note.Position.CenterSpeedometer) // position of Note.
                            .setAlign(Note.Align.Bottom) // Note Align.
                            .setTextTypeFace(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)) // style, or font.
                            .setBackgroundColor(Color.parseColor("#5BA0B2")) // change dialog color.
                            .setCornersRound(90f) // dialog's rectangle Corners Round.
                            .setTextSize(oil_temp.dpTOpx(16f))

                        oil_temp.addNote(note,400)

                    }
                    else
                        oil_temp.removeAllNotes()

                    if ((alpineServices.get_OilTemperature() - 40)>oil_alert)
                    {
                        if (!alert_ack && !alert_set) {
                            alert_set = true
                            snackbar_oilalert.show()
                        }
                    }
                    else
                    {
                        if (alert_ack && !alert_set)
                        {alert_set=false
                            alert_ack=false
                        }
                    }


                    if (!alert_ack && alert_set)
                    {
                        if (blink_switch) {

                            fragmentBlankBinding.engineLayout.setBackgroundResource(R.drawable.background_alert)
                            blink_switch = false
                        }
                        else {
                            fragmentBlankBinding.engineLayout.setBackgroundColor(0)
                            blink_switch = true
                        }
                    }

                    cool_temp.speedTo((alpineServices.get_EngineCoolantTemp() - 40).toFloat())

                    if ((alpineServices.get_EngineCoolantTemp() - 40)<85)
                    {
                        val note = TextNote(AlpdroidApplication.app, (alpineServices.get_EngineCoolantTemp() - 40).toString()+"°C")
                            .setPosition(Note.Position.CenterSpeedometer) // position of Note.
                            .setAlign(Note.Align.Bottom) // Note Align.
                            .setTextTypeFace(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)) // style, or font.
                            .setBackgroundColor(Color.parseColor("#5BA0B2")) // change dialog color.
                            .setCornersRound(90f) // dialog's rectangle Corners Round.
                            .setTextSize(cool_temp.dpTOpx(16f))

                        cool_temp.addNote(note,400)
                    }
                    else
                        cool_temp.removeAllNotes()

                    intake_temp.speedTo((alpineServices.get_IntakeAirTemperature() - 40).toFloat())

                    gear_temp.speedTo((alpineServices.get_RST_ATClutchTemperature() + 60).toFloat())

                    if ((alpineServices.get_RST_ATClutchTemperature() + 60)<85)
                    {
                        val note = TextNote(AlpdroidApplication.app, (alpineServices.get_RST_ATClutchTemperature() + 60).toString()+"°C")
                            .setPosition(Note.Position.CenterSpeedometer) // position of Note.
                            .setAlign(Note.Align.Bottom) // Note Align.
                            .setTextTypeFace(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)) // style, or font.
                            .setBackgroundColor(Color.parseColor("#5BA0B2")) // change dialog color.
                            .setCornersRound(90f) // dialog's rectangle Corners Round.
                            .setTextSize(gear_temp.dpTOpx(16f))

                        gear_temp.addNote(note,400)
                    }
                    else
                        gear_temp.removeAllNotes()

                    speed_100=(alpineServices.get_Disp_Speed()/100)
                    speed.text = String.format("%d Km/h", speed_100)

                    otherJauge3.speedTo((alpineServices.get_EngineOilPressure()).toFloat()/10)


                    fuelgauge=(alpineServices.get_FuelLevelDisplayed())
                    fuel_level.text =
                        String.format("%2d l", fuelgauge)


                    if (fuelgauge>18)
                        fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, null))
                    else if (fuelgauge>9)
                        fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null))
                    else
                        fuel_level.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.rouge, null))


                    tanklevel.setImageResource(R.drawable.gastank_levelfull)


                    if (fuelgauge<5)
                        tanklevel.setImageResource(R.drawable.gastank_levellow)
                    else if (fuelgauge<15)
                        tanklevel.setImageResource(R.drawable.gastank_levelmed)

                    brakethrottle.speedTo((alpineServices.get_BrakingPressure()).toFloat()/2)

                    speedthrottle.speedTo(alpineServices.get_RawSensor().toFloat()/8)


                    val id =
                        resources.getIdentifier(
                            "shift_${(alpineServices.get_CurrentGear()+1)}",
                            "drawable",
                            context?.packageName
                        )
                    gear_active.setImageResource(id)


                }
            }
        }
    }

    private fun loadPreferences() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("EnginePrefs", Context.MODE_PRIVATE)

        offset_tyretemp = sharedPreferences.getInt("offset_tyretemp", 0)
        offset_tyrepress = sharedPreferences.getFloat("offset_tyrepress", 0.0f)
        toggle_braketemp = sharedPreferences.getBoolean("toggle_braketemp", false)
        pressLimitLow =  sharedPreferences.getInt("press_limit_low", 1900)
        pressLimitHigh =  sharedPreferences.getInt("press_limit_high", 2100)
        tempLimitLow =  sharedPreferences.getInt("temp_limit_low", 20)
        tempLimitHigh =  sharedPreferences.getInt("temp_limit_high", 40)
        switchLevelOn = sharedPreferences.getBoolean("switchIndicator", true)

    }

    private fun saveToSharedPreferences() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("EnginePrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putInt("offset_tyretemp", offset_tyretemp)
        editor.putFloat("offset_tyrepress", offset_tyrepress)
        editor.putBoolean("toggle_braketemp", toggle_braketemp)
        editor.putInt("press_limit_low", pressLimitLow)
        editor.putInt("press_limit_high", pressLimitHigh)
        editor.putInt("temp_limit_low", tempLimitLow)
        editor.putInt("temp_limit_high", tempLimitHigh)
        editor.putBoolean("switchIndicator", switchLevelOn)

        editor.apply()
    }


}