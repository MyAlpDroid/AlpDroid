package com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.alpdroid.huGen10.databinding.ComputerDisplayBinding
import com.alpdroid.huGen10.ui.MainActivity.application


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: ComputerDisplayBinding?=null
    lateinit var ac_header : TextView
    lateinit var canframeText: TextView
    lateinit var canid: EditText
    lateinit var arduinostate : TextView
    lateinit var transmitstate : TextView
    var framestring1 : String=""
    var framestring2 : String=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val binding = ComputerDisplayBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding
        return binding.root
    }
    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        fragmentBlankBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        canframeText = fragmentBlankBinding!!.canFrameView
        canid = fragmentBlankBinding!!.idcanframe

        ac_header = fragmentBlankBinding!!.acHeader
        arduinostate = fragmentBlankBinding!!.arduinoState
        transmitstate = fragmentBlankBinding!!.transmitState


        timerTask = {
                activity?.runOnUiThread {
                    if (application.isBound) {
                        if (application.alpdroidData.alpine2Cluster.clusterStarted)
                            ac_header.text = "Cluster Is Working"
                        else ac_header.text = "Cluster Disconnected"

/**                        if (application.alpdroidServices.isCanFrameEnabled())
                            arduinostate.text = "Arduino Is Working"
                        else arduinostate.text = "Arduino Disconnected"

                        if (application.alpdroidServices.isBad == false)
                            transmitstate.text = ".....Ok"
                        else transmitstate.text = "......Bad"*/

                        framestring1= canid.text.toString()
                        if (framestring1.isNotEmpty()) {
                            framestring2=application.alpineCanFrame.getFrame(framestring1.toInt(16))
                                .toString()

                            canframeText.text =framestring2
                        }
            /**            GlobalScope.launch(Dispatchers.Default) {
                            val keyItem: MutableSet<Int> = application.alpineCanFrame.getMapKeys()
                            for (key in keyItem) {
                                if (application.alpineCanFrame.getFrame(key)?.bus != 1) {
                                    framestring1 += application.alpineCanFrame.getFrame(key)
                                        .toString()

                                    framestring1 += System.getProperty("line.separator")
                                } else {
                                    framestring2 +=
                                        application.alpineCanFrame.getFrame(key).toString()

                                    framestring2 += System.getProperty("line.separator")
                                }
                            }
                       }
                        canframeText.append(framestring1) */


                    }
                }
            }
    }

}