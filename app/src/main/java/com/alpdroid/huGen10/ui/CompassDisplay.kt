package main.java.com.alpdroid.huGen10.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.CanFrame
import com.alpdroid.huGen10.CanMCUAddrs
import com.alpdroid.huGen10.databinding.FragmentCompassBinding
import com.alpdroid.huGen10.ui.UIFragment

class CompassDisplay : UIFragment(350){

        private  var fragmentBlankBinding: FragmentCompassBinding?=null

        private val startValue = 0
        private val endValue = 360
        private val increment = 5
        lateinit var displayvalue: TextView
        private var app: AlpdroidApplication = AlpdroidApplication.app

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCompassBinding.inflate(inflater, container, false)
        fragmentBlankBinding = binding

        app.alpineCanFrame.addFrame(
            CanFrame(
                0,
                CanMCUAddrs.Compass_Info.idcan,
                byteArrayOf(
                    0x00.toByte(),
                    0x00.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte(),
                    0xFF.toByte()
                )
            )
        )
        return binding.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            var value:Int=0
            super.onViewCreated(view, savedInstanceState)
            displayvalue= fragmentBlankBinding?.textviewvalue!!
            // Display values and send frames

            timerTask = {
                activity?.runOnUiThread {
                    if (AlpdroidApplication.app.isBound) {


                            value+=2

                            displayValue(value)

                            pushFifoFrame(value/2)

                            if (value==180) value=0
                    }
                }
            }
        }

        private fun displayValue(value: Int) {
            // Update UI to display the value
            // For example, if you have a TextView with id 'textViewValue':
            if (value>256)
                displayvalue.setTextColor(Color.GREEN)
            else
                displayvalue.setTextColor(Color.WHITE)
            displayvalue.text = value.toString()
        }

        private fun pushFifoFrame(value: Int) {
            // Implement your logic here to send the frame with the value
            // For example:
            // YourClass.pushFifoFrame(value)
            app.alpdroidData.setCompassOrientation(value)


        }
    }