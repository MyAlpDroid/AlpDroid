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
    lateinit var countCluster:TextView
    lateinit var testFrame:Switch
    lateinit var frametoTest: EditText
    lateinit var framedatadisplay : TextView

    lateinit var keys: Set<Int>
    lateinit var iterator:Iterator<Int>
    lateinit var key2fifo: OBDframe
    var ite_id=0

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
        frametoTest = fragmentBlankBinding!!.idframe
        framedatadisplay = fragmentBlankBinding!!.idframe2

        rtxTimer = System.currentTimeMillis()

        framedatadisplay.setMovementMethod(ScrollingMovementMethod())
        framedatadisplay.text="Debut --> \r\n"

        timerTask = {
            activity?.runOnUiThread {
                if (AlpdroidApplication.app.isBound) {
                    if (AlpdroidApplication.app.alpdroidServices.isServiceStarted)
                        ac_header.text = "Service Is Working"
                    else ac_header.text = "Service Stopping for some weird reason"

                    countCluster.text = String.format(
                        "RX: %.1f Kb/s",
                        (AlpdroidApplication.app.alpdroidServices.rx / (System.currentTimeMillis() - rtxTimer)).toFloat()
                    )

                    appState.text = String.format(
                        "TX: %.1f Kb/s",
                        (AlpdroidApplication.app.alpdroidServices.tx / (System.currentTimeMillis() - rtxTimer)).toFloat()
                    )


                    if (AlpdroidApplication.app.alpdroidServices.isArduinoWorking())
                            arduinostate.text = "Arduino Serial Port Null"
                        else arduinostate.text = "Arduino transmitting"





                }
            }
        }
    }
}