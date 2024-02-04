package com.alpdroid.huGen10.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.VehicleServices
import com.alpdroid.huGen10.databinding.SimpleCounterBinding

class SimpleCounterDisplay : UIFragment(250)
{

        private lateinit var alpineServices : VehicleServices
        private lateinit var fragmentBlankBinding: SimpleCounterBinding

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


        lateinit var fuel_level: TextView
        lateinit var gear_active: ImageView
        lateinit var speed : TextView

        lateinit var oil_temp : TextView
        lateinit var cool_temp : TextView
        lateinit var intake_temp : TextView
        lateinit var gear_temp : TextView
        lateinit var otherJauge3: TextView
        lateinit var speedthrottle : TextView
        lateinit var brakethrottle : TextView


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

        lateinit var tanklevel: ImageView


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val binding = SimpleCounterBinding.inflate(inflater, container, false)
            fragmentBlankBinding = binding
            return binding.root
        }

        override fun onDestroyView() {
            super.onDestroyView()
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

        //    speed = fragmentBlankBinding.textSpeed


            oil_temp = fragmentBlankBinding.oilvalue
            cool_temp = fragmentBlankBinding.coolvalue

            gear_temp = fragmentBlankBinding.gearvalue
            otherJauge3 = fragmentBlankBinding.clutchvalue
/*
            fuel_level = fragmentBlankBinding.textFueLevel

            gear_active = fragmentBlankBinding.gearActive


            speedthrottle = fragmentBlankBinding.throttlePress
            brakethrottle = fragmentBlankBinding.brakePress

            otherJauge3 = fragmentBlankBinding.OilPressure

            tanklevel= fragmentBlankBinding.gastankLevel */

            fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.tpms_checks)
            fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.tpms_checks_right)

            fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.tpms_checks)
            fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.tpms_checks_right)

            fragmentBlankBinding.imageViewTempFL2.setImageResource(R.drawable.degre_c)
            fragmentBlankBinding.imageViewPressFL.setImageResource(R.drawable.unite_bar)

            fragmentBlankBinding.imageViewTempRL2.setImageResource(R.drawable.degre_c)
            fragmentBlankBinding.imageViewPressRL.setImageResource(R.drawable.unite_bar)

            fragmentBlankBinding.imageViewTempFR2.setImageResource(R.drawable.degre_c)
            fragmentBlankBinding.imageViewPressFR.setImageResource(R.drawable.unite_bar)

            fragmentBlankBinding.imageViewTempRR2.setImageResource(R.drawable.degre_c)
            fragmentBlankBinding.imageViewPressRR.setImageResource(R.drawable.unite_bar)

            loadPreferences()

            val snackbar_oilalert = AlertDialog.Builder(this.context)
                .setTitle(R.string.engine_alert_title)
                .setMessage(R.string.engine_alert_text)
                .setPositiveButton(
                    android.R.string.ok
                ) { dialogInterface: DialogInterface?, i: Int ->

                    fragmentBlankBinding.simpleLayout.setBackgroundColor(0)
                    alert_ack=true
                }



            val snackbar_tyrealert = AlertDialog.Builder(this.context)
                .setTitle(R.string.engine_alert_title)
                .setMessage(R.string.tyre_alert_text)
                .setPositiveButton(
                    android.R.string.ok
                ) { dialogInterface: DialogInterface?, i: Int ->

                    fragmentBlankBinding.simpleLayout.setBackgroundColor(0)
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
                        temp_FL2.text= String.format("%d", tyretemp_fl2+offset_tyretemp)
                        temp_FR2.text= String.format("%d",tyretemp_fr2+offset_tyretemp)
                        temp_RL2.text= String.format("%d",tyretemp_rl2+offset_tyretemp)
                        temp_RR2.text= String.format("%d",tyretemp_rr2+offset_tyretemp)

                        // Mode
                        val rst_vehicleMode=alpineServices.get_RST_VehicleMode()

                        var psi_limit_low_AVAR=1990
                        var psi_limit_mid_AV=2100
                        var psi_limit_mid_AR=2100
                        var psi_limit_high_AV=2250
                        var psi_limit_high_AR=2250

                        var tyretemp_limit_low=20
                        var tyretemp_limit_mid_step1=20
                        var tyretemp_limit_mid_step2=60
                        var tyretemp_limit_high=60

                        var oil_alert=125

                        tyre_calul4alert=false


                        if (rst_vehicleMode==3)
                        {
                            psi_limit_low_AVAR=1950
                            psi_limit_mid_AV=2050
                            psi_limit_mid_AR=2100
                            psi_limit_high_AV=2250
                            psi_limit_high_AR=2300

                            tyretemp_limit_low=40
                            tyretemp_limit_mid_step1=50
                            tyretemp_limit_mid_step2=80
                            tyretemp_limit_high=85

                            oil_alert=125
                        }

                        tyretemp_limit_low-=offset_tpms
                        tyretemp_limit_mid_step1-=offset_tpms
                        tyretemp_limit_mid_step2-=offset_tpms
                        tyretemp_limit_high-=offset_tpms

                        press_FL.text = String.format(
                            "%.2f",
                            offset_tyrepress +  (flbrake_press.toFloat()/1000)
                        )


                        tyre_check=0

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
                                    press_color = R.color.rouge
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
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }     // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            tyre_check=(tyre_check shl 3)

                            when {
                                rlbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
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
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }     // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            tyre_check=(tyre_check shl 3)


                            when {
                                rrbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
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
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }  // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            when {
                                tyretemp_fl2 < tyretemp_limit_low -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check  or (2 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check  or (1 shl 9)
                                }
                                else -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 9)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }


                            frontleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_fr2 < tyretemp_limit_low ->  {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check  or (2 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check  or (1 shl 6)
                                }
                                else -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 6)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            frontrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rl2 < tyretemp_limit_low -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check  or (2 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step2 ->{
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check  or (1 shl 3)
                                }
                                else -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 3)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            rearleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rr2 < tyretemp_limit_low -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check  or 2
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check  or 1
                                }
                                else -> {
                                    temp_color= R.color.rouge
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
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                flbrake_press < psi_limit_mid_AV ->  {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                flbrake_press < psi_limit_high_AV ->  {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (flbrake_press > psi_limit_high_AV  )
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                frbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                frbrake_press < psi_limit_mid_AV ->  {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                frbrake_press < psi_limit_high_AV ->  {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            frontrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (frbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                rlbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rlbrake_press < psi_limit_mid_AV ->  {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                rlbrake_press < psi_limit_high_AV ->  {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }
                            rearleftrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rlbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            tyre_check=(tyre_check shl 3)

                            when {
                                rrbrake_press < psi_limit_low_AVAR -> {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                }
                                rrbrake_press < psi_limit_mid_AV ->  {
                                    press_color = R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                rrbrake_press < psi_limit_high_AV ->  {
                                    press_color = R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                else ->  {
                                    press_color = R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite
                            }

                            rearrighttrowpress.setBackgroundColor(ResourcesCompat.getColor(getResources(), press_color, null))

                            if (rrbrake_press > psi_limit_high_AV)
                                tyre_calul4alert=true

                            when {
                                tyretemp_fl2 < tyretemp_limit_low -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step1 -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check or (1 shl 9)
                                }
                                tyretemp_fl2 < tyretemp_limit_high -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 9)
                                }
                                else ->{
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 9)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            frontleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            if (tyretemp_fl2 > tyretemp_limit_high)
                                tyre_calul4alert=true


                            when {
                                tyretemp_fr2 < tyretemp_limit_low -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step1 -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check or (1 shl 6)
                                }
                                tyretemp_fr2 < tyretemp_limit_high -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 6)
                                }
                                else ->{
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 6)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_fr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            frontrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rl2 < tyretemp_limit_low -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check  or (3 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step1 -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check or (1 shl 3)
                                }
                                tyretemp_rl2 < tyretemp_limit_high -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or (2 shl 3)
                                }
                                else ->{
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or (3 shl 3)
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }

                            if (tyretemp_rl2 > tyretemp_limit_high)
                                tyre_calul4alert=true

                            rearleftrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                            when {
                                tyretemp_rr2 < tyretemp_limit_low -> {
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check  or 3
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step1 -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                tyretemp_rr2 < tyretemp_limit_mid_step2 -> {
                                    temp_color= R.color.vert
                                    tyre_check = tyre_check or 1
                                }
                                tyretemp_rr2 < tyretemp_limit_high -> {
                                    temp_color= R.color.orange
                                    tyre_check = tyre_check or 2
                                }
                                else ->{
                                    temp_color= R.color.rouge
                                    tyre_check = tyre_check or 3
                                } // Valeur par défaut si aucune des conditions ci-dessus n'est satisfaite

                            }


                            if (tyretemp_rr2> tyretemp_limit_high)
                                tyre_calul4alert=true

                            rearrightrow.setBackgroundColor(ResourcesCompat.getColor(getResources(), temp_color, null))

                        }

                        press_RL.text = String.format(
                            "%.2f",
                            offset_tyrepress + (rlbrake_press.toFloat()/1000)
                        )


                        press_FR.text = String.format(
                            "%.2f",
                            offset_tyrepress + (frbrake_press.toFloat()/1000)
                        )


                        press_RR.text = String.format(
                            "%.2f",
                            offset_tyrepress + (rrbrake_press.toFloat()/1000)
                        )


                        press_FR.text = String.format(
                            "%.2f",
                            offset_tyrepress +  (frbrake_press.toFloat()/1000)
                        )


                        when ((tyre_check shr 9) and 0b111)
                        {
                            1->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.tpms_checks_green)
                            2->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.tpms_checks_orange)
                            3->   fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.tpms_checks_red)
                            else
                            -> fragmentBlankBinding.imagetpmsFL.setImageResource(R.drawable.tpms_checks)
                        }

                        when ((tyre_check shr 6) and 0b111)
                        {
                            1->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.tpms_checks_right_green)
                            2->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.tpms_checks_right_orange)
                            3->   fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.tpms_checks_right_red)
                            else
                            -> fragmentBlankBinding.imagetpmsFR.setImageResource(R.drawable.tpms_checks_right)
                        }


                        when ((tyre_check shr 3) and 0b111)
                        {
                            1->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.tpms_checks_green)
                            2->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.tpms_checks_orange)
                            3->   fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.tpms_checks_red)
                            else
                            -> fragmentBlankBinding.imagetpmsRL.setImageResource(R.drawable.tpms_checks)
                        }

                        when ((tyre_check) and 0b111)
                        {
                            1->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.tpms_checks_right_green)
                            2->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.tpms_checks_right_orange)
                            3->   fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.tpms_checks_right_red)
                            else
                            -> fragmentBlankBinding.imagetpmsRR.setImageResource(R.drawable.tpms_checks_right)
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

                        oil_temp.text=(alpineServices.get_OilTemperature() - 40).toString()


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

                                fragmentBlankBinding.simpleLayout.setBackgroundResource(R.drawable.background_alert)
                                blink_switch = false
                            }
                            else {
                                fragmentBlankBinding.simpleLayout.setBackgroundColor(0)
                                blink_switch = true
                            }
                        }

                        cool_temp.text=(alpineServices.get_EngineCoolantTemp() - 40).toString()


                        gear_temp.text=(alpineServices.get_RST_ATClutchTemperature() + 60).toString()


                        otherJauge3.text=((alpineServices.get_RST_ATOilTemperature()-40)).toString()



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
            offset_tpms = sharedPreferences.getInt("offset_tpms", 0)
        }


    }
