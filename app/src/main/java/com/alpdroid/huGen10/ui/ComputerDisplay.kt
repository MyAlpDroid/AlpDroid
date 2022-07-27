package com.alpdroid.huGen10.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alpdroid.huGen10.databinding.ComputerDisplayBinding
import com.alpdroid.huGen10.ui.MainActivity.alpineServices
import com.alpdroid.huGen10.ui.MainActivity.application


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ComputerDisplay : UIFragment(250) {

    private  var fragmentBlankBinding: ComputerDisplayBinding?=null
    lateinit var ac_header : TextView
    lateinit var canframeText: TextView
    lateinit var canframeText2: TextView


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
        val binding = ComputerDisplayBinding.bind(view)
        fragmentBlankBinding = binding

        canframeText = fragmentBlankBinding!!.canFrameView
        canframeText2 =fragmentBlankBinding!!.canFrameView2

        canframeText.movementMethod = ScrollingMovementMethod()
        canframeText2.movementMethod = ScrollingMovementMethod()

        ac_header = fragmentBlankBinding!!.acHeader

        val arduinostate = fragmentBlankBinding!!.arduinoState
        val transmitstate = fragmentBlankBinding!!.transmitState


if (application.isBound)
        timerTask = {
            activity?.runOnUiThread {

                if (alpineServices?.alpine2Cluster!!.clusterStarted)
                    ac_header.text = "Cluster Is Working"
                else ac_header.text = "Cluster Disconnected"

                if (alpineServices?.isVehicleEnabled() == true)
                    arduinostate.text = "Arduino Is Working"
                else arduinostate.text = "Arduino Disconnected"

                if (alpineServices?.isBad == false)
                    transmitstate.text = ".....Ok"
                else transmitstate.text = "......Bad"


                val keyItem: MutableSet<Int> = alpineServices.mapFrame.keys

                for (key in keyItem) {
                    if (alpineServices.getFrame(key)?.bus!=1) {
                        canframeText.append(alpineServices.getFrame(key).toString())
                        canframeText.append(System.getProperty("line.separator"))
                    }
                    else
                    {
                        canframeText2.append(alpineServices.getFrame(key).toString())
                        canframeText2.append(System.getProperty("line.separator"))
                    }
                }


            }
        }
    }


}