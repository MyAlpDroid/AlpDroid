package com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.alpdroid.huGen10.CanECUAddrs
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.ui.MainActivity.alpineServices
import com.alpdroid.huGen10.ui.MainActivity.application


@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class ConfortDisplay : UIFragment(250) {
   var isInPage = false
    lateinit var textLum:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.confort_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        isInPage = true
        super.onViewCreated(view, savedInstanceState)

        val sportMode = view.findViewById<SwitchCompat>(R.id.sportMode)
        val raceMode = view.findViewById<CheckBox>(R.id.driftBox)

        val luminosity = view.findViewById<Button>(R.id.lumibutton)

        textLum=view.findViewById(R.id.luminy)

        textLum = view.findViewById<CheckBox>(R.id.luminy)

            luminosity.setOnClickListener {
            alpineServices?.alpine2Cluster?.increasePanel()
           textLum.text=String.format("%d %%",alpineServices?.alpine2Cluster?.panelLuminosity)

        }
        raceMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                application.startVehicleServices()
            else
                application.stopVehicleServices()
        }

        timerTask = {
            activity?.runOnUiThread {
                // to be used

                if (sportMode.isChecked && alpineServices.checkFrame(CanECUAddrs.ECM_CANHS_RNr_01.idcan))
                {
                    alpineServices.set_RST_VehicleMode(2)
                    alpineServices.sendFrame(CanECUAddrs.ECM_CANHS_RNr_01.idcan)
                }
                else
                    if (alpineServices.checkFrame(CanECUAddrs.ECM_CANHS_RNr_01.idcan))
                {
                    alpineServices.set_RST_VehicleMode(1)
                    alpineServices.sendFrame(CanECUAddrs.ECM_CANHS_RNr_01.idcan)
                }
            }
        }
   }
}