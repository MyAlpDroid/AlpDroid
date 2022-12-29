package com.alpdroid.huGen10

import android.os.Bundle
import android.util.Log
import com.alpdroid.huGen10.OsmAndHelper.OnOsmandMissingListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.osmand.aidlapi.navigation.ADirectionInfo
import java.util.*
import kotlin.math.roundToInt


class ClusterInfo (application : AlpdroidApplication):OnOsmandMissingListener
{


    private val TAG = ClusterInfo::class.java.name

    var application: AlpdroidApplication = application

    var frameFlowTurn: Int = 0

    var albumName: String = "Phil"
    var trackName: String = "Alpdroid"
    var artistName: String = "2022(c)"
    var trackId: Int = 0
    var trackLengthInSec: Int = 0

    var startIndexAlbum: Int = 0
    var startIndexTrack: Int = 0
    var startIndexArtist: Int = 0

    var nextTurnTypee: Int = 0
    var distanceToturn: Int = 0
    var unitToKilometer:Boolean=false

    var iconTest:Boolean = false
    var distancetoTest:Int =0
    var nextTurnToTest:Int =0

    var prevtrackName: String = "prev"

    var updateMusic: Boolean = true


    var index: Int = 0

    var rightNow = Calendar.getInstance()

    var clusterStarted: Boolean


    private val mutex_push = Mutex()

    private var mAidlHelper:OsmAndAidlHelper
    private var callbackKeys:Long =-1
    private var callbackKeysUpdate:Long=-1

    private var notinit=true

    init {

        mAidlHelper = OsmAndAidlHelper(this.application, this)

        mAidlHelper.setNavigationInfoUpdateListener (object: OsmAndAidlHelper.NavigationInfoUpdateListener {
            override fun onNavigationInfoUpdate(directionInfo: ADirectionInfo) {
                Log.d(TAG,"callbakc nav init")
                nextTurnTypee = directionInfo.turnType
                distanceToturn = directionInfo.distanceTo
            }
        })

        callbackKeys = mAidlHelper.registerForNavigationUpdates(true, 0)

        clusterStarted = true

        // Setting audio Info to Internet Source
        application.alpineCanFrame.addFrame(
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
        application.alpineCanFrame.addFrame(
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
        application.alpineCanFrame.addFrame(
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
            )
        )

        // Creating first Compass Frame
        application.alpineCanFrame.addFrame(
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


        // Init Source Album & trackname info
        for (i in 0..9)
            application.alpineCanFrame.addFrame(
                CanFrame(
                    0,
                    CanMCUAddrs.Audio_Display.idcan + i,
                    byteArrayOf(
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                )
            )



        application.alpineCanFrame.unsetSending()

        Log.d(TAG, "trying to start coroutines")

        CoroutineScope(Dispatchers.IO).launch {

            while (true) {
                if (application.alpdroidServices.isServiceStarted) {
                    clusterStarted = false
                    mutex_push.withLock {
                        try {
                            // Block Frame
                            clusterInfoUpdate()
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.CustomerClockSync.idcan + 0)
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.RoadNavigation.idcan + 0)
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Compass_Info.idcan + 0)
                            if (updateMusic) {
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Info.idcan + 0)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 0)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 1)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 2)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 3)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 4)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 5)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 6)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 7)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 8)
                                application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Display.idcan + 9)
                                updateMusic = false
                            }
                            clusterStarted = true
                            application.alpineCanFrame.setSending()

                        } catch (e: Exception) {
                            clusterStarted = true
                            updateMusic = false
                            prevtrackName = "-- something wrong --"
                            trackName = "-- oups --"
                        } finally {
                            delay(3500)
                        }
                    }
                }
            }
        }
    }



    fun onDestroy()
    {
        clusterStarted=false
    }


    fun String.rotate():String
    {
        var endIndex:Int=17+index
        var padding:Int
        var finalResult:String


        if (this.length<endIndex) {
            endIndex = this.length

            if (index>0)
                finalResult=this.substring(index, endIndex)+"..."+this.substring(0,index)
            else
                finalResult=this.substring(0, endIndex)
        } else {

            finalResult=this.substring(index, endIndex)
        }

        padding=20-finalResult.length
        if (padding>0)
            finalResult=finalResult.padEnd(padding)

        index++

        if (index>this.length) index=0

        return finalResult

    }

    private fun clusterInfoUpdate()
    {

    /* OSMAND Values
        const val C = 1 //"C"; // continue (go straight) //$NON-NLS-1$
        const val TL = 2 // turn left //$NON-NLS-1$
        const val TSLL = 3 // turn slightly left //$NON-NLS-1$
        const val TSHL = 4 // turn sharply left //$NON-NLS-1$
        const val TR = 5 // turn right //$NON-NLS-1$
        const val TSLR = 6 // turn slightly right //$NON-NLS-1$
        const val TSHR = 7 // turn sharply right //$NON-NLS-1$
        const val KL = 8 // keep left //$NON-NLS-1$
        const val KR = 9 // keep right//$NON-NLS-1$
        const val TU = 10 // U-turn //$NON-NLS-1$
        const val TRU = 11 // Right U-turn //$NON-NLS-1$
        const val OFFR = 12 // Off route //$NON-NLS-1$
        const val RNDB = 13 // Roundabout
        const val RNLB = 14 // Roundabout left
*/
        /* Alpine Cluster Values
        1 - turn right
        2 - turn sharply right
        3 - turn sharply left
        4 - turn  left
        5 - turn slightly left
        6 - continue (go straight)
        7 - Right U-turn
        8 - cross lane to right
        9 - change lane to right
        10 - exit right
        11 - cross road right (tri lanes)
        12 - cross road right (two lanes)
        13 - Roundabout exit 81° right
        14 - Rounabout exit 69° right
        15 - Rounabout exit 57° right
        16 - Rounabout exit 45° right
        17 - Rounabout exit 36° right
        18 - Rounabout exit 24° right
        19 - Roundabout exit 12° right
        20 - Roundabout U - turn right (180°)
        21 - Roundabout exit 168° right to left
        22 - Roundabout exit 156° right to left
        23 - Roundabout exit 144° right to left
        24 - Roundabout exit 135° right to left
        25 - Roundabout exit 123° right to left
        26 - Roundabout exit 111° right to left
        27 - Roundabout exit 99° right to left
        28 - Roundabout exit 90° front
        29 - Turn right then left
        30 - Leave lane to right
        31 - Leave lane to right right
        32 - 48 - Compass Info
        49 - continue (go straight)
        50 - Left U Turn
        51 - cross lane to left
        52 - change lane to left
        53 - exit left
        54 - cross road left (tri lanes)
        55 - cross road left (two lanes)
        56 - Roundabout exit 81° left
        57 - Rounabout exit 69° left
        58 - Rounabout exit 57° left
        59 - Rounabout exit 45° left
        60 - Rounabout exit 36° left
        61 - Rounabout exit 24° left
        62 - Roundabout exit 12° left
        63 - Roundabout U - turn right (180°)
        64 - Roundabout exit 168° right to left
        65 - Roundabout exit 156° right to left
        66 - Roundabout exit 144° right to left
        67 - Roundabout exit 135° right to left
        68 - Roundabout exit 123° right to left
        69 - Roundabout exit 111° right to left
        70 - Roundabout exit 99° right to left
        71 - Roundabout exit 90° front
        72 - Turn left then right
        73 - Leave lane to left
        74 - Leave lane to left left
        75 - cross road straight (tri lanes)
        76 - go straight
        77 - roundabout 1st Exit right
        78 - roundabout 1st Exit left
        79 - go left
        80 - Destination
        81 - Arrived grey
        82 - Start
        83 - Arrived Yellow
        84 - Left lane (two lanes)
        85 - Arrived Yellow
        86 - 255 - same as 8

         */

        mAidlHelper = OsmAndAidlHelper(this.application, this)

        mAidlHelper.setNavigationInfoUpdateListener(object :
            OsmAndAidlHelper.NavigationInfoUpdateListener {
            override fun onNavigationInfoUpdate(directionInfo: ADirectionInfo) {
                when (directionInfo.turnType)
                {
                    1 -> nextTurnTypee=6
                    2 -> nextTurnTypee=4
                    3 -> nextTurnTypee=5
                    4 -> nextTurnTypee=3
                    5 -> nextTurnTypee=1
                    6 -> nextTurnTypee=79
                    7 -> nextTurnTypee=2
                    8 -> nextTurnTypee=53
                    9 -> nextTurnTypee=10
                    10 -> nextTurnTypee=50
                    11 -> nextTurnTypee=7
                    12 -> nextTurnTypee=81
                    13 -> nextTurnTypee=13
                    14 -> nextTurnTypee=56
                }
                distanceToturn = directionInfo.distanceTo
                unitToKilometer = false
                if (distanceToturn>1999) {
                    distanceToturn = (distanceToturn / 1000).toDouble().roundToInt()
                    unitToKilometer = true
                }
            }
        })

        callbackKeys = mAidlHelper.registerForNavigationUpdates(true,0)


        frameFlowTurn+=1

        updateMusic = (prevtrackName != trackName)

        if (!updateMusic && frameFlowTurn>6)
            {
                    prevtrackName=trackName
                    frameFlowTurn=0
                    updateMusic=true
                // trying to update name in case of missing frame
                    albumName= application.alpdroidServices.getalbumName().toString()
                    artistName= application.alpdroidServices.getartistName().toString()
                    trackName= application.alpdroidServices.gettrackName().toString()
            }

        if (updateMusic) {
                for (i in 0..4) {
                    application.alpineCanFrame.addFrame(
                        CanFrame(
                            0,
                            CanMCUAddrs.Audio_Display.idcan + i,
                            getStringLine(artistName, i + 1)
                        )
                    )

                    application.alpineCanFrame.addFrame(
                        CanFrame(
                            0,
                            CanMCUAddrs.Audio_Display.idcan + i + 5,
                            getStringLine(trackName, i + 1)
                        )
                    )
                }
            }


        // Setting audio Info to Internet Source
        application.alpineCanFrame.addFrame(
            CanFrame(
                0,
                CanMCUAddrs.Audio_Info.idcan,
                byteArrayOf(
                    0xFC.toByte(),
                    0x70.toByte(),
                    0x80.toByte(),
                    0x09.toByte(),
                    0x00.toByte(),
                    0x7F.toByte(),
                    0x7F.toByte(),
                    0x7F.toByte()
                )
            )
        )


// Compass
        application.alpdroidData.setFrameParams(CanMCUAddrs.Compass_Info.idcan+0,0,8,application.alpdroidData.get_CompassOrientation())

// Navigation / Direction

        if (iconTest)
        {
           // do nothing
        }
        else {
            application.alpdroidData.setFrameParams(
                CanMCUAddrs.RoadNavigation.idcan + 0,
                0,
                12,
                distanceToturn
            )
            if (unitToKilometer)
                application.alpdroidData.setFrameParams(CanMCUAddrs.RoadNavigation.idcan + 0, 12, 4, 1)
            else
                application.alpdroidData.setFrameParams(CanMCUAddrs.RoadNavigation.idcan + 0, 12, 4, 0)
            
            application.alpdroidData.setFrameParams(
                CanMCUAddrs.RoadNavigation.idcan + 0,
                16,
                8,
                nextTurnTypee
            )
        }

// Heure
        rightNow = Calendar.getInstance()
        application.alpdroidData.set_VehicleClock_Hour(rightNow.get(Calendar.HOUR_OF_DAY))
        application.alpdroidData.set_VehicleClock_Minute(rightNow.get(Calendar.MINUTE))
        application.alpdroidData.set_VehicleClock_Second(rightNow.get(Calendar.SECOND))

    }

    fun getStringLine (line : String, longueur : Int ) : ByteArray
    {
        var tableau:ByteArray=byteArrayOf(0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte())
        var pas = 4*(longueur-1)

        if (line.length>pas) {
            for (i in 0..7 step 2) {
                if (pas<line.length) {
                    line[pas].code.toByte().also { tableau[i + 1] = it }
                    pas += 1
                }
            }
        }

        return tableau

    }

    fun fromOsmData(extras: Bundle)
    {
        if (extras.size() > 0) {
         /*   nextTurnTypee = extras.getBundle("no_speak_next_turn_type").toString().toInt()
            //  alpine2Cluster.nextTurnTypee  = extras.getBundle("turn_type").toString().toInt()
            distanceToturn  = extras.getBundle("next_turn_distance").toString().toInt()
            Log.d("next_turn",nextTurnTypee.toString())
            Log.d("turn_type",extras.getBundle("turn_type").toString())
            Log.d("distance_2_turn",distanceToturn.toString())*/
            for (key in extras.keySet()) {

                Log.d("key to read : ", key)
                Log.d("value read : ", extras[key].toString())
            }

        }
    }


    override fun osmandMissing() {

        Log.d(TAG,"OsmAND not ready")

    }


}