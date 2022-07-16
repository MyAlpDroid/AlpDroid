package com.alpdroid.huGen10

import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ClusterInfo (val alpineServices : VehicleServices)
{
    var albumName : String = "Phil"
    var trackName : String = "Alpdroid"
    var artistName: String = "2022(c)"
    var trackId: Int = 0
    var trackLengthInSec: Int = 0

    var startIndexAlbum:Int=0
    var startIndexTrack:Int=0
    var startIndexArtist:Int=0

    var prevalbumName:String = albumName
    var prevtrackName:String = trackName
    var prevartistName:String = artistName

    var north_Params:Int=0

    var panelLuminosity:Int=0

    private var executor = Executors.newScheduledThreadPool(1)

    var rightNow = Calendar.getInstance()

    var clusterStarted:Boolean

    init {

        clusterStarted=true
        // Setting audio Info to Internet Source
        alpineServices.addFrame(
            CanFrame(
                0,
                CanMCUAddrs.Audio_Info.idcan,
                byteArrayOf(
                    0x90.toByte(),
                    0xE0.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x7F.toByte(),
                    0x7F.toByte(),
                    0x7F.toByte()
                )
            )
        )

        // Creating first Clock Frame
        alpineServices.addFrame(
            CanFrame(
                0, CanMCUAddrs.CustomerClockSync.idcan, byteArrayOf(
                    0xE0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte(),
                    0xC0.toByte()
                )
            )
        )

        // Creating first Navigation Frame
        alpineServices.addFrame(
            CanFrame(
                0,
                CanMCUAddrs.RoadNavigation.idcan,
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
            ))

        // Creating first Compass Frame
        alpineServices.addFrame(
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
            ))

        // Creating first Luminosity Frame
        alpineServices.addFrame(
            CanFrame(
                0,
                CanMCUAddrs.UserSetPrefs2_MM.idcan,
                byteArrayOf(
                    0x02.toByte(),
                    0x95.toByte(),
                    0xC1.toByte(),
                    0x00.toByte(),
                    0x20.toByte(),
                    0x81.toByte(),
                    0x00.toByte(),
                    0x18.toByte()
                )
            ))
    // Init Source Album & trackname info

        executor.scheduleAtFixedRate(
            {
                try {
                    clusterStarted=true

                    clusterInfoUpdate()

                    alpineServices.pushFifoFrame(CanMCUAddrs.CustomerClockSync.idcan+0)

                    alpineServices.pushFifoFrame(CanMCUAddrs.RoadNavigation.idcan+0)

                    alpineServices.pushFifoFrame(CanMCUAddrs.Compass_Info.idcan+0)

                    alpineServices.pushFifoFrame(CanMCUAddrs.Audio_Info.idcan+0)

                    for (i in 0..10) {
                        alpineServices.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + i)
                    }



                }
                catch (e: Exception) {
                    Log.d("Cluster Info : ","Exception")
                    clusterStarted=false
                }

            }, 0, 2500, TimeUnit.MILLISECONDS
        )

    }

    fun increasePanel() {

        panelLuminosity+=5
        if (panelLuminosity>100)
            panelLuminosity=0
        alpineServices.set_CPanelDisplayDayLuminosityReques(panelLuminosity)

    }

    fun onDestroy()
    {
        clusterStarted=false
        executor.shutdown()
    }

    fun String.rotate(index:Int):String
    {
        var endIndex:Int=index+20
        var padding:Int=0
        var finalResult:String


        if (this.length<endIndex) {
            endIndex = this.length

            if (index>0)
                finalResult=this.substring(index, endIndex)+" "+this.substring(0,index)
            else
                finalResult=this.substring(0, endIndex)
        } else {

            finalResult=this.substring(index, endIndex)
        }

        padding=20-finalResult.length
        if (padding>0)
            finalResult=finalResult.padEnd(padding)

        return finalResult

    }

    fun clusterInfoUpdate()
    {

        prevalbumName = albumName.rotate(startIndexAlbum)
        startIndexAlbum+=1
        if (startIndexAlbum>albumName.length)
            startIndexAlbum=0


        prevtrackName = trackName.rotate(startIndexTrack)
        startIndexTrack+=1

        if (startIndexTrack>trackName.length)
            startIndexTrack=0


        prevartistName = artistName.rotate(startIndexArtist)
        startIndexArtist+=1
        if (startIndexArtist>artistName.length)
            startIndexArtist=0


        for (i in 0..4) {
            alpineServices.addFrame(
                CanFrame(
                    0,
                    CanMCUAddrs.Audio_Display.idcan + i,
                    getStringLine(prevartistName, i + 1)
                )
            )

            alpineServices.addFrame(
                CanFrame(
                    0,
                    CanMCUAddrs.Audio_Display.idcan + i + 5,
                    getStringLine(prevtrackName, i + 1)
                )
            )
/*
            if (i<3)
            alpineServices.addFrame(
                CanFrame(0,CanMCUAddrs.Audio_Display.idcan+i+8,getStringLine(prevalbumName,i+1))
            )*/
        }


        alpineServices.setFrameParams(CanMCUAddrs.Compass_Info.idcan+0,0,8,north_Params)
        north_Params+=1
        if (north_Params>180)
            north_Params=0

        rightNow = Calendar.getInstance()
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


    }

    fun getStringLine (line : String, longueur : Int ) : ByteArray
    {
        var tableau:ByteArray=byteArrayOf(0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte())
        var pas = 4*(longueur-1)

        if (line.length>pas) {
            for (i in 0..7 step 2) {
                if (pas<line.length) {
                    tableau[i + 1] = line[pas].code.toByte()
                    pas += 1
                }
            }
        }
        return tableau

    }



}