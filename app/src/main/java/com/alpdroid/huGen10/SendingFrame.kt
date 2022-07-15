package com.alpdroid.huGen10

import android.os.Build
import android.util.Log
import com.alpdroid.huGen10.CanFrame
import com.alpdroid.huGen10.CanMCUAddrs
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SendingFrame (val alpineServices : VehicleServices) {

    private val streamtext = 0
    private var executor = Executors.newScheduledThreadPool(1)

    var rightNow = Calendar.getInstance()

    // var musicFrame : CanFrame



    init {


        alpineServices.addFrame(CanFrame(0,CanMCUAddrs.Audio_Info.idcan,byteArrayOf(0x90.toByte(),0xE0.toByte(),0x00.toByte(),0x00.toByte(),0x00.toByte(),0x7F.toByte(),0x7F.toByte(),0x7F.toByte())))
        alpineServices.addFrame(CanFrame(0, CanMCUAddrs.CustomerClockSync.idcan,byteArrayOf(
            0xE0.toByte(),
            0xC0.toByte(),
            0xC0.toByte(),
            0xC0.toByte(),
            0xC0.toByte(),
            0xC0.toByte(),
            0xC0.toByte(),
            0xC0.toByte()
        )))


        executor.scheduleAtFixedRate(
            {
                try {
                    rightNow = Calendar.getInstance()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        alpineServices.setFrameParams(
                            CanMCUAddrs.CustomerClockSync.idcan,
                            3,
                            5,
                            rightNow.get(Calendar.HOUR_OF_DAY)
                        )
                        alpineServices.setFrameParams(
                            CanMCUAddrs.CustomerClockSync.idcan,
                            10,
                            6,
                            rightNow.get(Calendar.MINUTE)
                        )
                        alpineServices.setFrameParams(
                            CanMCUAddrs.CustomerClockSync.idcan,
                            18,
                            6,
                            rightNow.get(Calendar.SECOND)

                        )


                        alpineServices.sendFrame(CanMCUAddrs.CustomerClockSync.idcan)

                        alpineServices.sendFrame(CanMCUAddrs.Audio_Info.idcan)
                    }


/*
                        if (!isDriftMode) {
                            spin_walker = 0
                            var NavigateFrame = CanFrame(
                                0x0399,
                                'B',
                                byteArrayOf(
                                    0x10.toByte(),
                                    0x00.toByte(),
                                    action_a.toByte(),
                                    action_b.toByte(),
                                    action_c.toByte(),
                                    action_d.toByte(),
                                    0x3F.toByte(),
                                    0xFF.toByte()
                                )
                            )
                            CarComm.sendFrame(NavigateFrame)
                            NavigateFrame.clear()
                        }

                        if (isDriftMode)
                        {
                            action_a=0
                            action_b=0
                            action_c=0
                            action_d=0
                            var NavigateFrame = CanFrame(
                                0x0399,
                                'B',
                                byteArrayOf(
                                    0x00.toByte(),
                                    0x00.toByte(),
                                    0xFF.toByte(),
                                    0xFF.toByte(),
                                    0xFF.toByte(),
                                    0xFF.toByte(),
                                    0x3F.toByte(),
                                    0xFF.toByte()
                                )
                            )
                            CarComm.sendFrame(NavigateFrame)
                            NavigateFrame.clear()

                        var CompassFrame = CanFrame(0x0405, 'B', byteArrayOf(spin_walker.toByte(),0x7F.toByte()))
                        CompassFrame.setBitRange(0, 8, spin_walker)
                        if (spin_walker<=180)
                            CompassFrame.setBitRange(8, 1, 0)
                        else
                            CompassFrame.setBitRange(8, 1, 1)
                        if (spin_walker>180)
                            spin_walker=0
                        CarComm.sendFrame(CompassFrame)
                        CompassFrame.clear()
                        }*/



                } catch (e: Exception) {

                }
            }, 0, 1000, TimeUnit.MILLISECONDS
        )

    }

}


