package com.alpdroid.huGen10.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alpdroid.huGen10.*

import java.util.*

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class StatusBar : UIFragment(250) {

    var alpineServices : VehicleServices? = MainActivity.application.alpdroidService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.status_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val trackName = view.findViewById<TextView>(R.id.trackName)
        val trackNext = view.findViewById<ImageView>(R.id.track_next)
        val trackPrev = view.findViewById<ImageView>(R.id.track_prev)
        val rx_metric = view.findViewById<TextView>(R.id.bytes_rx)
        val tx_metric = view.findViewById<TextView>(R.id.bytes_tx)

        val batt_img = view.findViewById<ImageView>(R.id.bat_img)
        val batt_text = view.findViewById<TextView>(R.id.batt_text)

        trackName.text = alpineServices?.getalbumName()

  //      var rx = String.format("%.1f", CarComm.getRxRate().toDouble() * 4 / 1000.0)
   //     val tx = String.format("%.1f", CarComm.getTxRate().toDouble() * 4 / 1000.0)

        val currGear = (alpineServices?.get_CurrentGear()?.plus(1)).toString()
        val targGear = (alpineServices?.get_RST_ATPreSelectedRange()?.plus(1)).toString()

 //       CarData.selectedDriveMode = AS1_ECM_CANHS_RNr_01.get_RST_VehicleMode()

        val engineModeText: String

  /*      when (CarData.selectedDriveMode) {
            1 -> engineModeText = "Normal"
            2 -> engineModeText = "Sport"
            3 -> engineModeText = "Track"
            else -> engineModeText = "Unknown"
        }
*/

                val cc_spd = alpineServices?.get_VehicleSpeed()


                activity?.runOnUiThread {
     //               rx_metric.text = "Rx: $rx kb/s"
       //             tx_metric.text = "Tx: $tx kb/s"

      //              batt_text.text = String.format("%.1f V", CarData.batt_voltage)
                    batt_img.setImageResource(
                        when {
        //                    CarData.batt_voltage < 11.8 -> R.drawable.bat_red
          //                  CarData.batt_voltage < 12.5 -> R.drawable.bat_white
                            else ->R.drawable.bat_green
                        }
                    )

     /*              var spd_txt = String.format("%d ", CarData.get_speed().toInt())
                    if (cc_spd != 0) {
                        spd_txt += String.format("| %d ", cc_spd)
                    }
                    spd_txt += if (CarData.isMetric) { "kmh" } else { "mph" }
*/
                }
            }

}