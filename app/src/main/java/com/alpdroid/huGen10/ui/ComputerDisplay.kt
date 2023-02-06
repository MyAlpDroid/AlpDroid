package com.alpdroid.huGen10.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.databinding.ComputerDisplayBinding
import kotlinx.coroutines.sync.Mutex


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(500) {

    private  var fragmentBlankBinding: ComputerDisplayBinding?=null
    lateinit var ac_header : TextView
    lateinit var canframeText: TextView
    lateinit var canid: EditText
    lateinit var arduinostate : TextView
    lateinit var transmitstate : TextView
    lateinit var appState : TextView
    lateinit var trackShow:TextView
    lateinit var trackPrev:TextView
    lateinit var countCluster:TextView
    lateinit var testFrame:Switch
    lateinit var frametoTest: EditText
    lateinit var framedata: TextView


    var rtxTimer:Long=0

    var frametotestString1 : String=""
    var framedataString1 : String = ""
    var framestring1 : String=""
    var framestring2 : String=""

    lateinit var mutex_push:Mutex

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = ComputerDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }
    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
    //    fragmentBlankBinding = null
        super.onDestroyView()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        canframeText = fragmentBlankBinding!!.canFrameView
        canid = fragmentBlankBinding!!.idcanframe

        ac_header = fragmentBlankBinding!!.acHeader
        arduinostate = fragmentBlankBinding!!.arduinoState
        transmitstate = fragmentBlankBinding!!.transmitState
        appState = fragmentBlankBinding!!.appstate
        trackShow = fragmentBlankBinding!!.showtrack
        trackPrev = fragmentBlankBinding!!.showprev
        countCluster = fragmentBlankBinding!!.countcluster
        testFrame = fragmentBlankBinding!!.testicons
        frametoTest = fragmentBlankBinding!!.idframe
        framedata = fragmentBlankBinding!!.idframe2

        rtxTimer = System.currentTimeMillis()

        timerTask = {
            activity?.runOnUiThread {
                if (AlpdroidApplication.app.isBound) {
                    if (AlpdroidApplication.app.alpdroidServices.isServiceStarted)
                        ac_header.text = "Service Is Working"
                    else ac_header.text = "Service Stopping for some weird reason"

                    countCluster.text = String.format(
                        "RX: %.1f Kb/s",
                        (AlpdroidApplication.app.alpdroidServices.tx / (System.currentTimeMillis() - rtxTimer)).toFloat()
                    )

                    appState.text = String.format(
                        "TX: %.1f Kb/s",
                        (AlpdroidApplication.app.alpdroidServices.rx / (System.currentTimeMillis() - rtxTimer)).toFloat()
                    )

                    if (testFrame.isChecked)

                    /*                    A few comments:

                         Each CAN transmit frame is delayed by 30 ms to avoid overlap in the message queries
                         The frequency is set to 500 ms to avoid excessive requests vs. the data resolution (which is low for OBD2)
                         The "id" field is 7DF, which for OBD2 reflects a "request" message (while e.g. 7E8 reflects a "response" message)
                         The "data" field shows the OBD2 request frame structure, including in particular the HEX PID being requested in the 3rd byte

                         Example: If you wish to query Engine RPM data in an OBD2 context, you'll need to set the "data" field to 02 01 0C 55 55 55 55 55.

                         Here, the first byte is 02 and corresponds to the number of additional bytes (in this case 2)
                         The second byte is 01 and corresponds to the OBD2 "mode", cf. Wikipedia for details
                         The third byte is 0C, which under Mode 01 reflects the parameter ID of RPM
                         Finally, the remaining bytes of the data field are set to 55 ('dummy loads') and ignored

                         7DF, length, 22 80 11 FF
                         7E8, length, 62 80 11 00

                         */

                    /*      frametotestString1= frametoTest.text.toString()
                         framedataString1=framedata.text.toString()

                         for (i in framedataString1.indices) {
                             AlpdroidApplication.app.alpdroidData.setFrameParams(
                                 framestring1.toInt(16),
                                 0+(i*8),
                                 4,
                                 framedataString1[i].digitToInt(16)
                             )
                         }

                         AlpdroidApplication.app.alpineCanFrame.addFrame(
                             CanFrame(
                             1,
                             0x07DF,
                             byteArrayOf(
                                 0x03.toByte(),
                                 0x22.toByte(),
                                 0x80.toByte(),
                                 0x11.toByte(),
                                 0xFF.toByte(),
                                 0xFF.toByte(),
                                 0xFF.toByte(),
                                 0xFF.toByte()
                             )))
*/


                        if (AlpdroidApplication.app.alpdroidServices.isArduinoWorking())
                            arduinostate.text = "Arduino Serial Port Null"
                        else arduinostate.text = "Arduino transmitting"


                    framestring1 = canid.text.toString()

                    if (framestring1.isNotEmpty()) {
                        framestring2 = AlpdroidApplication.app.alpineCanFrame.getFrame(
                            framestring1.toInt(16)
                        )
                            .toString()

                        canframeText.text = framestring2
                    }

                    frametotestString1 = frametoTest.text.toString()

                    if (frametotestString1.isNotEmpty()) {
                        framedataString1 = AlpdroidApplication.app.alpineOBDFrame.getFrame(
                            frametotestString1.toInt(16)
                        )
                            .toString()

                        framedata.text = framedataString1
                    }

                else
                        appState.text = "Application is not bound"


                }
            }
        }
    }
}