package com.alpdroid.huGen10.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.OBDframe
import com.alpdroid.huGen10.databinding.ComputerDisplayBinding
import com.alpdroid.huGen10.obdUtil.DtcBody
import com.alpdroid.huGen10.obdUtil.DtcChassis
import com.alpdroid.huGen10.obdUtil.DtcNetwork
import com.alpdroid.huGen10.obdUtil.DtcPowertrain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(1500) {

    private  var fragmentBlankBinding: ComputerDisplayBinding?=null
    lateinit var ac_header : TextView
    lateinit var canframeText: TextView
    lateinit var canid: EditText
    lateinit var arduinostate : TextView
    lateinit var transmitstate : TextView
    lateinit var appState : TextView
    lateinit var trackShow:TextView
    lateinit var trackPrev:TextView
    lateinit var testFrame:Switch
    lateinit var frametoTest: EditText
    lateinit var framedatadisplay : TextView

    lateinit var keys: Set<Int>
    lateinit var iterator:Iterator<Int>
    lateinit var key2fifo: OBDframe

    var ptc_see:Boolean = false


    var rtxTimer:Long=0

    var frametotestString1 : String=""
    var framedataString1 : String = ""
    var framestring1 : String=""
    var framestring2 : String=""

    lateinit var mutex_push:Mutex

    var ptc_index:Int=0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = ComputerDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        fragmentBlankBinding!!.ptcbutton!!.setOnClickListener {
               obdptclaunch()
            }

        return binding.root
    }

    private fun obdptclaunch() {
        GlobalScope.launch {


        AlpdroidApplication.app.alpdroidData.ask_ptclist()
        ptc_see=true

            /*
        if (ptc_index==0) {
            val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[10,2B,59,02,CF,40,74,87]}"

            Log.d("obdptclaunch","frame send One: "+bytearray)

            AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

        }


            Log.d("obdptclaunch","frame getFrame One: "+ ptc_index.toString())
            if (AlpdroidApplication.app.alpineOBDFrame.getFrame(2,0x59,0x7e9)!=null)
                ptc_index=1

            if (ptc_index==1) {

                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[21,08,D1,2D,87,88,D1,20]}"


                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send 2: "+ bytearray)
                ptc_index++
            }

            if (ptc_index==2) {
                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[2${ptc_index.toByte().toString(16)},87,88,D1,2C,87,88,50]}"

                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send "+ ptc_index.toString()+" - "+bytearray)
                ptc_index++
            }
            if (ptc_index==3) {
                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[2${ptc_index.toByte().toString(16)},00,87,88,D1,21,87,88]}"

                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send "+ ptc_index.toString()+" - "+bytearray)
                ptc_index++
            }
            if (ptc_index==4) {
                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[2${ptc_index.toByte().toString(16)},18,15,16,D8,50,02,87]}"

                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send "+ ptc_index.toString()+" - "+bytearray)
                ptc_index++
            }

            if (ptc_index==5) {
                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[2${ptc_index.toByte().toString(16)},08,D1,24,87,08,50,01]}"

                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send "+ ptc_index.toString()+" - "+bytearray)
                ptc_index++
            }
            if (ptc_index==6) {
                val bytearray = "{\"bus\":1,\"id\":07E9,\"data\":[2${ptc_index.toByte().toString(16)},87,08,24,87,08,50,01]}"

                AlpdroidApplication.app.alpdroidServices.onArduinoMessage(bytearray.toByteArray())

                Log.d("obdptclaunch","frame send "+ ptc_index.toString()+" - "+bytearray)
                ptc_index++
            }  */

        }

    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
    //    fragmentBlankBinding = null
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

                            var key2build: String = ""
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

                                            ptc2show += key2build + "-" +String.format("%2X",ptc2decode[loop + 2])+ " = " + DtcPowertrain.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2]) + " = " + "This DTC code is not documented"
                                        }
                                    }
                                    1 -> {
                                        key2build = "C"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build C:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + DtcChassis.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    2 -> {
                                        key2build = "B"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build B:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + DtcBody.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    3 -> {
                                        key2build = "U"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build U:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + DtcNetwork.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    else -> {
                                        key2build = "X"
                                        key2build += calculatePTCode
                                        ptc2show += key2build + "-" + String.format("%2X",ptc2decode[loop + 2])+ " = " + "This is bad DTC "
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

                            var key2build: String = ""
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
                                    "%2X",
                                    (ptc2decode2[loop + 1])
                                )


                                when ((ptc2decode2[loop].toUByte().toUInt() shr 6).toInt()) {
                                    0 -> {
                                        key2build = "P"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build P:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + DtcPowertrain.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    1 -> {
                                        key2build = "C"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build C:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + DtcChassis.valueOf(
                                                key2build
                                            ).dtc + "-" + ptc2decode2[loop + 2]
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    2 -> {
                                        key2build = "B"
                                        key2build += calculatePTCode
                                        //                      Log.d("Key2Build B:",key2build)
                                        try {

                                            ptc2show += key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + DtcBody.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    3 -> {
                                        key2build = "U"
                                        key2build += calculatePTCode
                                        //                   Log.d("Key2Build U:",key2build)
                                        try {

                                            ptc2show +=  key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + DtcNetwork.valueOf(
                                                key2build
                                            ).dtc
                                        } catch (ex: Exception) {
                                            ptc2show +=  key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + "This DTC code is not documented"
                                        }
                                    }
                                    else -> {
                                        key2build = "X"
                                        key2build += calculatePTCode
                                        ptc2show +=  key2build + "-" + String.format("%2X",ptc2decode2[loop + 2])+ " = " + "This is bad DTC "
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