package com.alpdroid.huGen10.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.alpdroid.huGen10.AlpdroidApplication
import com.alpdroid.huGen10.CanMCUAddrs
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
    lateinit var testIcon:Switch
    lateinit var icontoTest:EditText

    var nextTurnToTest : Int = 0
    var distancetoTest : Int = 0
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
        testIcon = fragmentBlankBinding!!.testicons
        icontoTest = fragmentBlankBinding!!.idicons

        timerTask = {
                activity?.runOnUiThread {
                    if (AlpdroidApplication.app.isBound) {
                        if (AlpdroidApplication.app.alpdroidServices.isServiceStarted)
                            ac_header.text = "Service Is Working"
                        else ac_header.text = "Service Stopping for some weird reason"

                        trackShow.text=AlpdroidApplication.app.alpdroidServices.alpine2Cluster.trackName
                        trackPrev.text=AlpdroidApplication.app.alpdroidServices.alpine2Cluster.prevtrackName
                        countCluster.text= AlpdroidApplication.app.alpdroidServices.alpine2Cluster.nextTurnTypee.toString()
                                //AlpdroidApplication.app.alpdroidServices.tx.toString()
                        appState.text=AlpdroidApplication.app.alpdroidServices.alpine2Cluster.frameFlowTurn.toString()


                        try {
                            nextTurnToTest = icontoTest.text.toString().toInt()
                        }
                        catch (e:Exception)
                        {
                            nextTurnToTest=0
                        }

                        Log.d("ComputerDisplay","val :"+nextTurnToTest.toString()+" et idcon:"+icontoTest.text.toString())

                     if (testIcon.isChecked)
                     {
                         AlpdroidApplication.app.alpdroidData.setFrameParams(
                             CanMCUAddrs.RoadNavigation.idcan + 0,
                             0,
                             12,
                             nextTurnToTest
                         )


                         AlpdroidApplication.app.alpdroidData.setFrameParams(CanMCUAddrs.RoadNavigation.idcan + 0, 12, 4, 0)
                         AlpdroidApplication.app.alpdroidData.setFrameParams(
                            CanMCUAddrs.RoadNavigation.idcan + 0,
                            16,
                            8,
                             nextTurnToTest
                        )
                         AlpdroidApplication.app.alpdroidData.setFrameParams(
                            CanMCUAddrs.RoadNavigation.idcan + 0,
                            24,
                            8,
                             nextTurnToTest+1
                        )
                         AlpdroidApplication.app.alpdroidData.setFrameParams(
                            CanMCUAddrs.RoadNavigation.idcan + 0,
                            32,
                            8,
                             nextTurnToTest+2
                        )
                         AlpdroidApplication.app.alpdroidData.setFrameParams(
                            CanMCUAddrs.RoadNavigation.idcan + 0,
                            40,
                            8,
                             nextTurnToTest+3
                        )

                         try {
                         AlpdroidApplication.app.alpineCanFrame.pushFifoFrame(CanMCUAddrs.RoadNavigation.idcan+0)
                         AlpdroidApplication.app.alpineCanFrame.setSending()

                             } catch (e: Exception) {

                             }
                     }

                     if (AlpdroidApplication.app.alpdroidServices.isArduinoWorking())
                            arduinostate.text = "Arduino Serial Port Null"
                        else arduinostate.text = "Arduino transmitting"


                        framestring1= canid.text.toString()

                        if (framestring1.isNotEmpty()) {
                            framestring2=AlpdroidApplication.app.alpineCanFrame.getFrame(framestring1.toInt(16))
                                .toString()

                            canframeText.text =framestring2
                        }

                    }
                    else
                        appState.text="Application is not bound"
                }
            }
    }

}