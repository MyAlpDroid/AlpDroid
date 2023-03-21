package com.alpdroid.huGen10

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.alpdroid.huGen10.OsmAndHelper.OnOsmandMissingListener
import com.alpdroid.huGen10.ui.WidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.osmand.aidlapi.info.AppInfoParams
import java.util.*
import kotlin.math.roundToInt


class ClusterInfo (var application: AlpdroidApplication):OnOsmandMissingListener
{



    private val TAG = ClusterInfo::class.java.name

    var frameFlowTurn: Int = 0

    var albumName: String = "Phil"
    var trackName: String = "Alpdroid"
    var artistName: String = "2022(c)"
    var albumArtist: String = "MyAlpDroid"
    var trackId: Int = 0
    var trackLengthInSec: Int = 0
    var audioSource:Int =0

    var startIndexAlbum: Int = 0
    var startIndexTrack: Int = 0
    var startIndexArtist: Int = 0

    var nextTurnTypee: Int = 0
    var secondnextTurnTypee: Int = 0
    var distanceToturn: Int = 0
    var unitToKilometer:Boolean=false
    var isNavigated:Boolean=false
    var isRoundAbout: Boolean = false
    var isRoundAboutsecondary: Boolean = false
    var turnAngle:Float= 0.0F
    var turnAnglescondary:Float= 0.0F
    var isleftSide: Boolean=false

    var noNav_app: Boolean = false


    var prevtrackName: String = "prev"

    var updateMusic: Boolean = true


    var index: Int = 0

    var rightNow = Calendar.getInstance()

    var clusterStarted: Boolean


    private val mutex_push = Mutex()

    private lateinit var mAidlHelper:OsmAndAidlHelper

    private var app:AlpdroidApplication=this.application


    init {


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

        // Creating first Compass Frame
        application.alpineCanFrame.addFrame(
            CanFrame(
                1,
                CanECUAddrs.CANECUSEND.idcan,
                byteArrayOf(
                    0x03.toByte(),
                    0x22.toByte(),
                    0x11.toByte(),
                    0x03.toByte(),
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



        CoroutineScope(Dispatchers.IO).launch {

            mAidlHelper = OsmAndAidlHelper(app, this@ClusterInfo)

            while (true) {

                if (application.alpdroidServices.isServiceStarted) {
                    clusterStarted = false
                    if (!mAidlHelper.isBind) {
                        mAidlHelper = OsmAndAidlHelper(app, this@ClusterInfo)
                    }
                    mutex_push.withLock {

                        try {
                            // Cluster's Frame
                            clusterInfoUpdate()
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Audio_Info.idcan + 0)
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.CustomerClockSync.idcan + 0)
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.RoadNavigation.idcan + 0)
                            application.alpineCanFrame.pushFifoFrame(CanMCUAddrs.Compass_Info.idcan + 0)

                            if (updateMusic) {
                                // settings audio info
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
                                application.alpdroidData.ask_OBDTyreTemperature()
                                application.alpdroidData.ask_OBDBattV2()
                                application.alpdroidData.ask_OBDStandardCode()
                            }

                            clusterStarted = true

                            application.alpineCanFrame.setSending()

                        } catch (e: Exception) {
                            Log.d(TAG,"exception cluster")
                            clusterStarted = true
                            updateMusic = false
                            prevtrackName = "-- something wrong --"
                            trackName = "-- oups --"
                        } finally {

                            val intent = Intent(app, WidgetProvider::class.java)
                            intent.action = WidgetProvider.ACTION_WIDGET_UPDATE

                            val ids: IntArray =
                                AppWidgetManager.getInstance(AlpdroidApplication.app)
                                    .getAppWidgetIds(ComponentName(AlpdroidApplication.app, WidgetProvider::class.java))
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

                            app.sendBroadcast(intent)

                            delay(1250)
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
        var endIndex:Int=20+index
        val padding:Int
        var finalResult:String


        if (this.length<endIndex) {
            endIndex = this.length

            if (index>0)
                finalResult=this.substring(index, endIndex)+this.substring(0,index)
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
        var infoParams:AppInfoParams



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
            RNDB / RNLB + exitout
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

       if (mAidlHelper.isBind)
       {
         try {

            infoParams=mAidlHelper.appInfo


             // lanes 7 , 2 ==> Use Left , not straight
             // lanes 10,5 ==> Use right turn, not turn left

            isNavigated=(mAidlHelper.appInfo.arrivalTime>0)

            distanceToturn=infoParams.turnInfo.getInt("next_turn_distance",0)

            isleftSide=infoParams.turnInfo.getBoolean("next_turn_possibly_left", false)

            unitToKilometer = false

            if (distanceToturn>2999) {
                distanceToturn = (distanceToturn / 1000).toDouble().roundToInt()
                unitToKilometer = true
            }

            var checkstring=infoParams.turnInfo.getString("next_turn_type", "no type")

            when (checkstring)
            {
                "no type" -> nextTurnTypee=0
                "C" -> nextTurnTypee=6
                "TL" -> nextTurnTypee=4
                "TSLL" -> nextTurnTypee=5
                "TSHL" -> nextTurnTypee=3
                "TR" -> nextTurnTypee=1
                "TSLR" -> nextTurnTypee=79
                "TSHR" -> nextTurnTypee=2
                "KL" -> nextTurnTypee=53
                "KR" -> nextTurnTypee=10
                "TU" -> nextTurnTypee=50
                "TRU" -> nextTurnTypee=7
                "OFFR" -> nextTurnTypee=81
                else -> {
                    if (checkstring.matches(Regex("RN.B.")))
                        isRoundAbout=true
                }

            }


            if (isRoundAbout)
            {
                turnAngle=infoParams.turnInfo.getFloat("next_turn_angle", 0.0F)

                isRoundAbout=false
                when
                {
                    turnAngle< -158 -> nextTurnTypee=21
                    turnAngle< -135 -> nextTurnTypee=22
                    turnAngle< -112 -> nextTurnTypee=23
                    turnAngle< -90 -> nextTurnTypee=24
                    turnAngle< -67 -> nextTurnTypee=25
                    turnAngle< -45 -> nextTurnTypee=26
                    turnAngle< -22 -> nextTurnTypee=27
                    turnAngle< 0 -> nextTurnTypee=28
                    turnAngle>158-> nextTurnTypee=20
                    turnAngle>135 -> nextTurnTypee=19
                    turnAngle>112 -> nextTurnTypee=18
                    turnAngle>90 -> nextTurnTypee=17
                    turnAngle>67 -> nextTurnTypee=16
                    turnAngle>45 -> nextTurnTypee=15
                    turnAngle>22 -> nextTurnTypee=14
                    turnAngle>=0f -> nextTurnTypee=13

                }

            }


            if (isleftSide)
                nextTurnTypee+=43

            var checkstring2=infoParams.turnInfo.getString("no_speak_next_turn_type", "no type")

            when (checkstring2)
            {
                "no type" -> secondnextTurnTypee=0
                "C" -> secondnextTurnTypee=6
                "TL" -> secondnextTurnTypee=4
                "TSLL" -> secondnextTurnTypee=5
                "TSHL" -> secondnextTurnTypee=3
                "TR" -> secondnextTurnTypee=1
                "TSLR" -> secondnextTurnTypee=79
                "TSHR" -> secondnextTurnTypee=2
                "KL" -> secondnextTurnTypee=53
                "KR" -> secondnextTurnTypee=10
                "TU" -> secondnextTurnTypee=50
                "TRU" -> secondnextTurnTypee=7
                "OFFR" -> secondnextTurnTypee=81
                else -> {
                    if (checkstring2.matches(Regex("RN.B.")))
                        isRoundAboutsecondary = true
                }
            }

                if (isRoundAboutsecondary)
                 {
                    turnAnglescondary=infoParams.turnInfo.getFloat("no_speak_next_turn_angle", 0.0F)
                    isRoundAboutsecondary=false
                    when
                    {
                        turnAnglescondary< -158 -> secondnextTurnTypee=21
                        turnAnglescondary< -135 -> secondnextTurnTypee=22
                        turnAnglescondary< -112 -> secondnextTurnTypee=23
                        turnAnglescondary< -90 -> secondnextTurnTypee=24
                        turnAnglescondary< -67 -> secondnextTurnTypee=25
                        turnAnglescondary< -45 -> secondnextTurnTypee=26
                        turnAnglescondary< -22 -> secondnextTurnTypee=27
                        turnAnglescondary< 0 -> secondnextTurnTypee=28
                        turnAnglescondary>158-> secondnextTurnTypee=20
                        turnAnglescondary>135 -> secondnextTurnTypee=19
                        turnAnglescondary>112 -> secondnextTurnTypee=18
                        turnAnglescondary>90 -> secondnextTurnTypee=17
                        turnAnglescondary>67 -> secondnextTurnTypee=16
                        turnAnglescondary>45 -> secondnextTurnTypee=15
                        turnAnglescondary>22 -> secondnextTurnTypee=14
                        turnAnglescondary>=0f -> secondnextTurnTypee=13


                    }

                }

                    if (isleftSide)
                        secondnextTurnTypee+=43

        }
        catch (e:Exception)
        {
            nextTurnTypee=0
        }
       }

        frameFlowTurn+=1

        updateMusic = (prevtrackName != trackName)

        if (!updateMusic && frameFlowTurn>3)
            {
                    frameFlowTurn=0
                    updateMusic=true

            }

        var radioartistname:String = artistName

        if (trackName==artistName)
            radioartistname=albumArtist

        if (updateMusic) {
                for (i in 0..4) {
                    application.alpineCanFrame.addFrame(
                        CanFrame(
                            0,
                            CanMCUAddrs.Audio_Display.idcan + i,
                            getStringLine(radioartistname, i + 1)
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

        application.alpdroidData.setFrameParams(CanMCUAddrs.Audio_Info.idcan+0,0,3,audioSource)

        if (audioSource==1) {
            when {
                albumName.contains("FM") -> application.alpdroidData.setFrameParams(
                    CanMCUAddrs.Audio_Info.idcan + 0,
                    3,
                    2,
                    0
                )
                albumName.contains("AM") -> application.alpdroidData.setFrameParams(
                    CanMCUAddrs.Audio_Info.idcan + 0,
                    3,
                    2,
                    1
                )
            }
            application.alpdroidData.setFrameParams(
                CanMCUAddrs.Audio_Info.idcan + 0,
                5,
                2,
                1
            )
            application.alpdroidData.setFrameParams(
                CanMCUAddrs.Audio_Info.idcan + 0,
                7,
                4,
                0
            )

        }


// Compass
        application.alpdroidData.setFrameParams(CanMCUAddrs.Compass_Info.idcan+0,0,8,application.alpdroidData.get_CompassOrientation())


// Navigation / Direction
// TODO : mise à jour plus rapide des directions

        if (!isNavigated)
        {
            // Reset Navigation Frame

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

            application.alpdroidData.setFrameParams(
                CanMCUAddrs.RoadNavigation.idcan + 0,
                24,
                8,
                0
            )

            application.alpdroidData.setFrameParams(
                CanMCUAddrs.RoadNavigation.idcan + 0,
                40,
                8,
                0
            )

            application.alpdroidData.setFrameParams(
                CanMCUAddrs.RoadNavigation.idcan + 0,
                32,
                8,
                secondnextTurnTypee
            )
        }

// Heure
        rightNow = Calendar.getInstance()
        application.alpdroidData.set_VehicleClock_Hour(rightNow.get(Calendar.HOUR_OF_DAY))
        application.alpdroidData.set_VehicleClock_Minute(rightNow.get(Calendar.MINUTE))
        application.alpdroidData.set_VehicleClock_Second(rightNow.get(Calendar.SECOND))
  //      Log.d(TAG, "Info ClusterInfo -- Exit cluster")

    }

    fun getStringLine (line : String, longueur : Int ) : ByteArray
    {
        val tableau:ByteArray=byteArrayOf(0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte(),0x00.toByte(),0x20.toByte())
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


    override fun osmandMissing() {

        Log.d(TAG,"OsmAND not ready")
        noNav_app=true

    }


}


