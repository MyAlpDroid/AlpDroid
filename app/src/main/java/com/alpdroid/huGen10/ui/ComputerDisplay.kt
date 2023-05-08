package com.alpdroid.huGen10.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.OBDframe
import com.alpdroid.huGen10.databinding.ComputerDisplayBinding
import com.alpdroid.huGen10.obdUtil.DtcBody
import com.alpdroid.huGen10.obdUtil.DtcChassis
import com.alpdroid.huGen10.obdUtil.DtcNetwork
import com.alpdroid.huGen10.obdUtil.DtcPowertrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(1500) {

    private  var fragmentBlankBinding: ComputerDisplayBinding?=null
    lateinit var ac_header : TextView
    lateinit var framedatadisplay : TextView

    lateinit var keys: Set<Int>
    lateinit var iterator:Iterator<Int>
    lateinit var key2fifo: OBDframe

    var ptc_see:Boolean = false

    var rtxTimer:Long=0

    var sharedPreferences: SharedPreferences? = null

    var showDialog = true

    lateinit var mirror_switch: Switch

    lateinit var startstop_switch: Switch

    lateinit var carpark_switch: Switch

    lateinit var rearbrake_switch: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = ComputerDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        fragmentBlankBinding!!.ptcbutton.setOnClickListener {
               obdptclaunch()
            }

        fragmentBlankBinding!!.resetDtc.setOnClickListener {
            obdptcreset()
        }


        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        mirror_switch = fragmentBlankBinding!!.mirrorswitch
        val mirror_switchState = this.sharedPreferences?.getBoolean("mirror_switch", false)
        if (mirror_switchState != null) {
            mirror_switch.isChecked = mirror_switchState
        }

        startstop_switch = fragmentBlankBinding!!.startstopswitch
        val startStop_switchState = this.sharedPreferences?.getBoolean("startstop_switch", false)
        if (startStop_switchState != null) {
            startstop_switch.isChecked = startStop_switchState
        }

       carpark_switch = fragmentBlankBinding!!.carparkswitch
        val carpark_switchState = this.sharedPreferences?.getBoolean("carpark_switch", false)
        if (carpark_switchState != null) {
            carpark_switch.isChecked = carpark_switchState
        }

        rearbrake_switch = fragmentBlankBinding!!.rearbrakeswitch
        val rearbrake_switchState = this.sharedPreferences?.getBoolean("rearbrake_switch", false)
        if (rearbrake_switchState != null) {
            rearbrake_switch.isChecked = rearbrake_switchState
        }

        // Create a single listener for all the switches and set it as the listener for each switch
        val switchListener = SwitchListener(sharedPreferences!!, requireContext(),
            fragmentBlankBinding!!
        )
        mirror_switch.setOnCheckedChangeListener(switchListener)
        rearbrake_switch.setOnCheckedChangeListener(switchListener)
        carpark_switch.setOnCheckedChangeListener(switchListener)
        startstop_switch.setOnCheckedChangeListener(switchListener)


        return binding.root
    }

    class SwitchListener(
        private val sharedPreferences: SharedPreferences,
        private val context: Context,
        private val fragmentBlankBinding: ComputerDisplayBinding
    ) :
        CompoundButton.OnCheckedChangeListener {
        private var showDialog = true
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            // Get the ID of the switch that was changed
            val switchId = buttonView.id

            // Save the state of the switch in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putBoolean("$switchId", isChecked)
            editor.apply()
            if (showDialog) {
                // Create a dialog builder and set the message and buttons
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Are you sure you want to change the state of this switch?")
                builder.setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    when (switchId)
                    {
                        fragmentBlankBinding.startstopswitch.id->
                        {
                //            AlpdroidApplication.app.alpdroidData.set_startstop_switch()
                        }
                        fragmentBlankBinding.mirrorswitch.id->
                        {
                //            AlpdroidApplication.app.alpdroidData.set_mirror_switch()
                        }
                        fragmentBlankBinding.rearbrakeswitch.id->
                        {

                        }
                        fragmentBlankBinding.carparkswitch.id->
                        {
                 //           AlpdroidApplication.app.alpdroidData.set_carpark_switch()
                        }
                    }
                }
                builder.setNegativeButton(
                    "No"
                ) { dialog, which -> // If the user clicked "No", reset the switch to its previous state
                    showDialog = false
                    buttonView.isChecked = !isChecked
                    editor.putBoolean("$switchId", !isChecked)
                    editor.apply()
                    showDialog = true
                }

                // Show the dialog
                val dialog = builder.create()
                dialog.show()
            } else {
                showDialog = true
            }
        }
    }

    private fun obdptclaunch() {
        CoroutineScope(Dispatchers.Default).launch {


       AlpdroidApplication.app.alpdroidData.ask_ptclist()
       ptc_see=true


        }

    }

    private fun obdptcreset() {
        CoroutineScope(Dispatchers.Default).launch {

            AlpdroidApplication.app.alpdroidData.reset_ptclist()
            ptc_see=true

        }

    }



    override fun onPause() {
        super.onPause()

        // Save the state of switch1 in SharedPreferences
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("mirror_switch", mirror_switch.isChecked())
        editor.putBoolean("carpark_switch", carpark_switch.isChecked())
        editor.putBoolean("rearbrake_switch", rearbrake_switch.isChecked())
        editor.putBoolean("startstop_switch", startstop_switch.isChecked())
        editor.apply()
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("mirror_switch", mirror_switch.isChecked())
        editor.putBoolean("carpark_switch", carpark_switch.isChecked())
        editor.putBoolean("rearbrake_switch", rearbrake_switch.isChecked())
        editor.putBoolean("startstop_switch", startstop_switch.isChecked())
        editor.apply()

        fragmentBlankBinding = null
        super.onDestroyView()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        ac_header = fragmentBlankBinding!!.acHeader


        framedatadisplay = fragmentBlankBinding!!.ptcframe

        rtxTimer = System.currentTimeMillis()

        framedatadisplay.setMovementMethod(ScrollingMovementMethod())
        framedatadisplay.text="Show stored Diagnostic Trouble Codes --> \r\n"

        timerTask = {
            activity?.runOnUiThread {
                if (AlpdroidApplication.app.isBound) {

                    if (ptc_see) {

                        var ptc2show = String()
                        var ptc2decode:ByteArray

                        if (AlpdroidApplication.app.alpdroidData.get_ptcdtc_ECM()!=null) {

                            ptc2show+="ECM Defaults :\b\n"
                            ptc2decode =AlpdroidApplication.app.alpdroidData.get_ptcdtc_ECM()!!.copyOfRange(1,
                                AlpdroidApplication.app.alpdroidData.get_ptcdtc_ECM()!!.size
                            )

                            var key2build: String
                            var calculatePTCode: String


                            var loop = 0

                            while (loop + 3 < ptc2decode.size)
                            // because ptcdtc begin by status byte
                            {

                                calculatePTCode =
                                    String.format("%X", ((ptc2decode[loop].toInt() and 0x30) shr 4))
                                calculatePTCode += String.format(
                                    "%X",
                                    (ptc2decode[loop].toInt() and 0x0F)
                                )
                                calculatePTCode += String.format(
                                    "%02X",
                                    ptc2decode[loop + 1]
                                )


                                when ((ptc2decode[loop].toUByte().toUInt() shr 6).toInt()) {
                                    0 -> {
                                        key2build = "P"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build P:",key2build)
                                        try {

                                            ptc2show += key2build + "-" +String.format("%02X",ptc2decode[loop + 2])+ " = " + DtcPowertrain.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2]) + " = " + "This DTC code is not documented"
                                        }
                                    }
                                    1 -> {
                                        key2build = "C"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build C:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + DtcChassis.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    2 -> {
                                        key2build = "B"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build B:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + DtcBody.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    3 -> {
                                        key2build = "U"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build U:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + DtcNetwork.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    else -> {
                                        key2build = "X"
                                        key2build += calculatePTCode
                                        ptc2show += key2build + "-" + String.format("%02X",ptc2decode[loop + 2])+ " = " + "This is bad DTC "
                                    }
                                }


                                if ((ptc2decode[loop + 3].toUByte().toInt() and 0x02) shr 1 == 1)
                                    ptc2show +=" -> Not Confirmed"
                                if ((ptc2decode[loop + 3].toUByte().toInt() and 0x08) shr 3 == 1)
                                    ptc2show +=" -> Confirmed"
                                if ((ptc2decode[loop + 3].toUByte().toInt() and 0x80) shr 7 == 1)
                                    ptc2show +=" and Light"

                                ptc2show +="\b\n"

                               loop += 4
                            }
                            framedatadisplay.setText(
                                ptc2show
                            )
                        }

                        if (AlpdroidApplication.app.alpdroidData.get_ptcdtc_ETT() != null) {

                            ptc2show+="\b\nDéfauts ECU Entretien :\b\n"
                            val ptc2decode2 =
                                AlpdroidApplication.app.alpdroidData.get_ptcdtc_ETT()!!.copyOfRange(
                                    1,
                                    AlpdroidApplication.app.alpdroidData.get_ptcdtc_ETT()!!.size
                                )

                            var key2build: String
                            var calculatePTCode: String


                            var loop = 0

                            while (loop + 3 < ptc2decode2.size)
                            // because ptcdtc begin by status byte
                            {

                               calculatePTCode = String.format(
                                    "%X",
                                    ((ptc2decode2[loop].toInt() and 0x30) shr 4)
                                )
                                calculatePTCode += String.format(
                                    "%X",
                                    (ptc2decode2[loop].toInt() and 0x0F)
                                )
                                calculatePTCode += String.format(
                                    "%02X",
                                    (ptc2decode2[loop + 1])
                                )


                                when ((ptc2decode2[loop].toUByte().toUInt() shr 6).toInt()) {
                                    0 -> {
                                        key2build = "P"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build P:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + DtcPowertrain.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    1 -> {
                                        key2build = "C"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build C:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + DtcChassis.valueOf(
                                                key2build
                                            ).dtc + "-" + ptc2decode2[loop + 2]
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    2 -> {
                                        key2build = "B"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build B:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + DtcBody.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    3 -> {
                                        key2build = "U"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build U:",key2build)
                                        try {

                                            ptc2show +=  key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + DtcNetwork.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    else -> {
                                        key2build = "X"
                                        key2build += calculatePTCode
                                        ptc2show +=  key2build + "-" + String.format("%02X",ptc2decode2[loop + 2])+ " = " + "This is bad DTC "
                                    }
                                }


                                if ((ptc2decode2[loop + 3].toUByte().toInt() and 0x02) shr 1 == 1)
                                    ptc2show +=" -> Not Confirmed"
                                if ((ptc2decode2[loop + 3].toUByte().toInt() and 0x08) shr 3 == 1)
                                    ptc2show +=" -> Confirmed"
                                if ((ptc2decode2[loop + 3].toUByte().toInt() and 0x80) shr 7 == 1)
                                    ptc2show +=" and Light"

                                ptc2show +="\b\n"

                                loop += 4
                            }
                            framedatadisplay.setText(
                                ptc2show
                            )
                        }
                    }
                }
            }
        }
    }
}