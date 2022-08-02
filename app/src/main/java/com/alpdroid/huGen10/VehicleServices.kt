package com.alpdroid.huGen10

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.alpdroid.huGen10.CanFrame
import com.google.gson.GsonBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


// Main CLass, as a service, listening to Arduino, sending to arduino and giving Frame value

class VehicleServices : Service(), ArduinoListener

     {
         companion object {
             fun isNotificationAccessEnabled(context: Context): Boolean {

                 return NotificationManagerCompat.getEnabledListenerPackages(context)
                     .contains(context.packageName)
             }
         }

    private val TAG = VehicleServices::class.java.name

    var mapFrame : ConcurrentHashMap<Int, CanFrame> = ConcurrentHashMap<Int, CanFrame>(100)

    var queueoutFrame : LinkedHashMap<Int, CanFrame> = LinkedHashMap(50)

    private lateinit var arduino : Arduino

    private val iBinder = VehicleServicesBinder()

    var alpine2Cluster: ClusterInfo? = null

    var isConnected : Boolean = false
    var isBad : Boolean = false

    private var executor = Executors.newScheduledThreadPool(1)


    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */
    override fun onBind(intent: Intent): IBinder {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "Vehicle Services Binded")
        return iBinder
       // throw UnsupportedOperationException("Not yet implemented")
    }
         inner class VehicleServicesBinder : Binder() {
             fun getService() : VehicleServices {
                 Log.d(TAG, "Vehicle Services get bind intent")
                 return this@VehicleServices
             }
         }
      override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // onTaskRemoved(intent)
          Log.d(TAG, "Vehicle Services started")

          isConnected=true
          arduino=Arduino(this, 115200)
          arduino.setArduinoListener(this)
          if (alpine2Cluster==null)
              alpine2Cluster = ClusterInfo(this)

         return START_STICKY
        }

         override fun onCreate() {
             super.onCreate()
             isConnected=true
             arduino=Arduino(this, 115200)
             arduino.setArduinoListener(this)
             if (alpine2Cluster==null)
                 alpine2Cluster = ClusterInfo(this)
             Log.d(TAG, "Arduino Listener started")


             // sending frame from FiFo queue every 125 ms due to unidirectionnal USB 2.0
             // with USB 3.0 port this could be change

             executor.scheduleAtFixedRate(
                 {
                     try {

                         this.sendFifoFrame()

                     }
                     catch (e: Exception) {
                         Log.d("VehicleServices sending frame : ","Exception")

                     }

                 }, 0, 125, TimeUnit.MILLISECONDS
             )
         }

         override fun onTaskRemoved(rootIntent: Intent) {
             super.onTaskRemoved(rootIntent)
             Log.d(TAG, "Arduino Listener stopped")
             arduino.unsetArduinoListener()
             arduino.close()
             isConnected=false
         }

    override fun onDestroy() {
        super.onDestroy()
        onTaskRemoved(Intent(applicationContext, this.javaClass))
        Log.d(TAG, "Vehicle Services Destroy")
    }



    fun isVehicleEnabled(): Boolean {
        return arduino.isOpened
    }


    fun onPause() {

        isConnected=true
        arduino=Arduino(this, 115200)
        arduino.setArduinoListener(this)
        Log.d(TAG, "Arduino Listener started")
        if (alpine2Cluster==null)
            alpine2Cluster = ClusterInfo(this)

    }

         fun setalbumName(albumname:String)
         {
             alpine2Cluster?.albumName=albumname
             alpine2Cluster?.prevalbumName=albumname.substring(0,minOf(albumname.length, 16))
             alpine2Cluster?.startIndexAlbum=0

         }

         fun getalbumName(): String? {
             return alpine2Cluster?.albumName
         }
         fun settrackName(trackname:String)
         {
             alpine2Cluster?.trackName=trackname
             alpine2Cluster?.prevtrackName=trackname.substring(0,minOf(trackname.length, 16))
             alpine2Cluster?.startIndexTrack=0
         }

         fun setartistName(artistname:String)
         {
             alpine2Cluster?.artistName=artistname
             alpine2Cluster?.prevartistName=artistname.substring(0,minOf(artistname.length, 16))
             alpine2Cluster?.startIndexArtist=0
         }

         fun settrackId(trackid:Int)
         {
             alpine2Cluster?.trackId=trackid
         }
         fun settrackLengthInSec(tracklengthinsec:Int)
         {
             alpine2Cluster?.trackLengthInSec=tracklengthinsec
         }

    override fun onArduinoAttached(device: UsbDevice?) {
        arduino.open(device)
    }

    override fun onArduinoDetached() {
        isConnected=false
        arduino.close()
    }


    @Synchronized
    override fun onArduinoMessage(bytes: ByteArray?) {

        //receive frame as Gson message
        val frame: CanFrame
        val buff = String(bytes!!)
        val gson = GsonBuilder()
            .registerTypeAdapter(CanFrame::class.java, CanframeGsonDeserializer())
            .create()
        try {
            frame = gson.fromJson(buff, CanFrame::class.java)
            if (frame != null) {
                this.addFrame(frame)
                isBad=false
            }
        } catch (e:Exception) {
            //checking bad message
         Log.d("Vehicle Services", "VehicleServices Bad Frame")
            isBad=true
        }


    }

    override fun onArduinoOpened() {}

    override fun onUsbPermissionDenied() {
        Looper.myLooper()?.let { Handler(it).postDelayed({ arduino.reopen() }, 3000) }
    }

    @Synchronized
    fun addFrame(frame: CanFrame) {

        if (mapFrame.replace(frame.id,frame)==null)
            mapFrame[frame.id] = frame

    }


   fun getFrame(candID:Int): CanFrame? {
       try {
           return mapFrame[candID]
       }
       catch (e:Exception) {
           return null
       }
   }


    fun getFrameParams(canID:Int, bytesNum:Int, len:Int): Int {
        val frame:CanFrame

        try {
            frame= this.getFrame(canID)!!
        }
        catch (e: Exception)
        {
            return 0
        }

        return frame.getValue(bytesNum,len)

    }

    @Synchronized
    fun setFrameParams(candID:Int, bytesNum:Int, len:Int, param:Int) {

        var frame: CanFrame

        getFrame(candID).also {
            if (it != null) {

            frame=it

           // Set in given range

            frame.setBitRange(bytesNum,len,param)

            this.addFrame(frame)
            }
        }

 }

    fun getFrameBool(candID:Int, bytesNum:Int): Boolean {

        getFrame(candID).also {
            if (it != null) {
                return it.getBit(bytesNum)
            }
        }
        return false
    }

    fun pushFifoFrame(candID: Int)
    {
        // Push frame to send into FiFO queue
        getFrame(candID).also { if (it!=null)
            queueoutFrame.put(candID,it)
        }

    }

    fun sendFifoFrame()
    {
        val keys: Set<Int> = queueoutFrame.keys
        val iterator = keys.iterator()
        val key2fifo:CanFrame

        //Unqueue frame : first in first out
        if (queueoutFrame.isNotEmpty()) {
            key2fifo = queueoutFrame.get(iterator.next())!!
            sendFrame(key2fifo.id)
            queueoutFrame.remove(key2fifo.id)

        }

    }

    @Synchronized
    fun sendFrame(candID: Int) {
        getFrame(candID).also {
         //send frame as byte to serial port
            if (it != null) {
                arduino.send(it.toByteArray())

            }
        }
    }

    fun checkFrame(ecuAddrs:Int):Boolean {

        // check if the frame is valuable
        if (this.getFrame(ecuAddrs)!=null) {
            return true
        }
        return false
    }

    // Update Regular Services

    // ECU Params Functions
    /**
     *  Return the state of the frame parameters
     *  Some params are note available on all Renault Cars
     *  GearBox & Torque
     **/

    /** Get code GearboxOilTemperature **/
    fun get_GearboxOilTemperature() : Int = this.getFrameParams(CanECUAddrs.AT_CANHS_R_01.idcan, 0, 8)


    /** Get Code DifferentialTorqueCalculated **/
    fun get_DifferentialTorqueCalculated() : Int = this.getFrameParams(CanECUAddrs.AT_CANHS_R_01.idcan, 8, 16)

    /**
     *  Lot of params : Acc, Clim, Batt, etc
     **/

    /** Get code ACCompPowerUsed_V2 **/
    fun get_ACCompPowerUsed_V2() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 0, 8)

    /** Get Code ACCompClutchStatus **/
    fun get_ACCompClutchStatus() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 8, 1)!=0

    /** Get Code BlowerState **/
    fun get_BlowerState() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 9, 1)!=0

    /** Get Code ACCoolingFanSpeedRequest **/
    fun get_ACCoolingFanSpeedRequest() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 10, 2)

    /** Get Code PumpActivationRequest **/
    fun get_PumpActivationRequest() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 12, 2)

    /** Get Code AC_StopAutoForbidden **/
    fun get_AC_StopAutoForbidden() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 14, 2)

    /** Get Code PTCNumberThermalRequest **/
    fun get_PTCNumberThermalRequest() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 16, 4)

    /** Get Code ACMinEngineIdleSpeedRequest **/
    fun get_ACMinEngineIdleSpeedRequest() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 20, 4)

    /** Get Code MinimumVoltagebyAC **/
    fun get_MinimumVoltagebyAC() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 24, 4)

    /** Get Code ACCompRequest **/
    fun get_ACCompRequest() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 28, 1)!=0

    /** Get Code ACCompClutchRequest **/
    fun get_ACCompClutchRequest() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 29, 1)!=0

    /** Get Code RearDefrostRequest **/
    fun get_RearDefrostRequest() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 30, 1)!=0

    /** Get Code ChillerActivationStatus **/
    fun get_ChillerActivationStatus() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 32, 2)

    /** Get Code BatTempoDelayRequest_CLIM **/
    fun get_BatTempoDelayRequest_CLIM() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 34, 2)

    /** Get Code FrontDefrostRequest **/
    fun get_FrontDefrostRequest() : Boolean = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 36, 1)!=0

    /** Get Code AC_SailingIdleForbidden **/
    fun get_AC_SailingIdleForbidden() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 37, 2)

    /** Get Code InternalTemp **/
    fun get_InternalTemp() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 40, 16)

    /**
     *  Oil, Batt, Washer Level, Fuel
     **/

    /** Get code MILDisplayState **/
    fun get_MILDisplayState() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 0, 4)

    /** Get Code DisplayedOilLevel **/
    fun get_DisplayedOilLevel() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 4, 4)

    /** Get code GlobalVehicleWarningState **/
    fun get_GlobalVehicleWarningState() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 8, 2)


    /** Get Code WasherLevelWarningState **/
    fun get_WasherLevelWarningState() : Boolean = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 10, 1) !=0

    /** Get Code BatteryWarningState **/
    fun get_BatteryWarningState() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 11, 2)
    /** Get Code ParticulateFilterWarningState **/
    fun get_ParticulateFilterWarningState() : Boolean = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 13, 1) !=0

    /** Get Code FuelAutonomyInKm **/
    fun get_FuelAutonomyInKm() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 14, 10)

    /** Get Code FuelLevelDisplayed **/
    fun get_FuelLevelDisplayed() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 24, 7)

    /** Get Code WaterInDieselWarningState **/
    fun get_WaterInDieselWarningState() : Boolean = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 31, 1) !=0

    /** Get Code FixedMaintenanceRange **/
    fun get_FixedMaintenanceRange() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 32, 8)

    /** Get Code AverageFuelConsumptionAfterSale **/
    fun get_AverageFuelConsumptionAfterSale() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 40, 8)

    /** Get Code MalfunctionLampWarningState **/
    fun get_MalfunctionLampWarningState() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 48, 2)


    /** Get Code BadgeWarningState **/
    fun get_BadgeWarningState() : Boolean = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 50, 1) !=0

    /** Get Code PersoOilDrainingRange **/
    fun get_PersoOilDrainingRange() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_01.idcan, 56, 8)

    /**
     *  Engine params
     **/

    /** Get code EngineFanSpeedRequest **/
    fun get_EngineFanSpeedRequest() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 0, 2)


    /** Get Code EmissionN_IdleRequest **/
    fun get_EmissionN_IdleRequest() : Boolean = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 2, 1)!=0

    /** Get code MaxElectricalPowerAllowed **/
    fun get_MaxElectricalPowerAllowed() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 3, 5)

    /** Get Code ElectricalPowerCutFreeze **/
    fun get_ElectricalPowerCutFreeze() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 8, 2)

    /** Get Code EngineStatus_R **/
    fun get_EngineStatus_R() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 10, 2)

    /** Get Code EngineStopRequestOrigine **/
    fun get_EngineStopRequestOrigine() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 12, 4)

    /** Get Code CrankingAuthorization_ECM **/
    fun get_CrankingAuthorization_ECM() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 16, 2)

    /** Get Code ACCompClutchActivation **/
    fun get_ACCompClutchActivation() : Boolean = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 18, 1) !=0

    /** Get Code OpenActiveBrakeSwitch_ECM **/
    fun get_OpenActiveBrakeSwitch_ECM() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 19, 2)


    /** Get Code N_IdleInstructionSignal **/
    fun get_N_IdleInstructionSignal() : Boolean = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 21, 1)!=0

    /** Get Code ACCompAuthorized **/
    fun get_ACCompAuthorized() : Boolean = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 22, 1)!=0

    /** Get Code ACHighPressureSensor **/
    fun get_ACHighPressureSensor() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 23, 9)

    /** Get Code FloatingInhibition **/
    fun get_FloatingInhibition() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 32, 2)

    /** Get Code DMSOrder **/
    fun get_DMSOrder() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 34, 2)

    /** Get Code RST_VehicleMode **/
    fun get_RST_VehicleMode() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 38, 2)

    fun set_RST_VehicleMode(params:Int) = this.setFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 38, 2, params)

    /** Get Code AllowedPowerGradient **/
    fun get_AllowedPowerGradient() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 40, 8)

    /** Get Code AlternatorMaximumPower **/
    fun get_AlternatorMaximumPower() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 48, 8)

    /** Get Code ACCompMaximumPower **/
    fun get_ACCompMaximumPower() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_RNr_01.idcan, 56, 8)

    /**
     *  Lights, Ambient, Batt
     **/

    /** Get code EMM_Refuse_to_Sleep **/
    fun get_EMM_Refuse_to_Sleep() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 0, 2)


    /** Get Code PositionLightsOmissionWarningEMM **/
    fun get_PositionLightsOmissionWarningEMM() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 2, 2)

    /** Get code IgnitionControlState **/
    fun get_IgnitionControlState() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 4, 1) != 0

    /** Get IgnitionSupplyConfirmation  **/
    fun get_IgnitionSupplyConfirmation() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 5, 1) !=0

    /** Get IdleSpeedInhibitionRequest **/
    fun get_IdleSpeedInhibitionRequest() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 6, 2)

    /** Get SharpInstantMecanicalPowerByAlt **/
    fun get_SharpInstantMecanicalPowerByAlt() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 8, 8)

    /** Get ElecPowerConsumedByAux **/
    fun get_ElecPowerConsumedByAux() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 16, 8)

    /** Get AlternatorLoad **/
    fun get_AlternatorLoad() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 24, 8)

    /** Get SOCBattery14V **/
    fun get_SOCBattery14V() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 32, 6)

    /** Get RearFogLightStateDisplayEMM **/
    fun get_RearFogLightStateDisplayEMM() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 38, 1) !=0

    /** Get FrontFogLightStateDisplayEMM **/
    fun get_FrontFogLightStateDisplayEMM() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 39, 1) !=0

    /** Get PositionLightsDisplayEMM **/
    fun get_PositionLightsDisplayEMM() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 40, 1) !=0

    /** Get HighBeamDisplayEMM **/
    fun get_HighBeamDisplayEMM() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 41, 1) !=0

    /** Get LowBeamDisplayEMM **/
    fun get_LowBeamDisplayEMM() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 42, 1) !=0

    /** Get RemoteLightingRequest **/
    fun get_RemoteLightingRequest() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 43, 2)

    /** Get EngineStopAutoWarnStatus **/
    fun get_EngineStopAutoWarnStatus() : Boolean = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 45, 1) !=0

    /** Get MiniConsumptionRequest**/
    fun get_MiniConsumptionRequest() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 48, 2)

    /** Get BatteryVoltage **/
    fun get_BatteryVoltage() : Int = this.getFrameParams(CanECUAddrs.EMM_CANHS_R_01.idcan, 56, 8)

    /**
     *  Brakes Temp
     **/

    /** Get code FrontLeftBrakeTemperature **/
    fun get_FrontLeftBrakeTemperature() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 0, 8)

    /** Get Code FrontRightBrakeTemperature **/
    fun get_FrontRightBrakeTemperature() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 8, 8)

    /** Get code RearLeftBrakeTemperature **/
    fun get_RearLeftBrakeTemperature() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 16, 8)

    /** Get Code RearRightBrakeTemperature **/
    fun get_RearRightBrakeTemperature() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 24, 8)

    /** Get Code LeftDrivenWheelSlip **/
    fun get_LeftDrivenWheelSlip() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 32, 6)
    /** Get Code RightDrivenWheelSlip **/
    fun get_RightDrivenWheelSlip() : Int = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_Rst_01.idcan, 38, 2)

    /**
     *  Tires Pressure
     **/

    /** Get code SpeedPressureMisadaptation **/
    fun get_SpeedPressureMisadaptation() : Boolean = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 0, 1) !=0


    /** Get Code TPMSFailureWarningRequest **/
    fun get_TPMSFailureWarningRequest() : Boolean = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 1, 1) !=0

    /** Get code RearRightWheelState **/
    fun get_RearRightWheelState() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 2, 3)

    /** Get code RearLeftWheelState **/
    fun get_RearLeftWheelState() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 5, 3)

    /** Get code FrontRightWheelState **/
    fun get_FrontRightWheelState() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 8, 3)

    /** Get code FrontLeftWheelState **/
    fun get_FrontLeftWheelState() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 11, 3)

    /** Get Right_LeftImbalance **/
    fun get_Right_LeftImbalance() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 14, 2)

    /** Get RearRightWheelPressure_V2 **/
    fun get_RearRightWheelPressure_V2() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 16, 8)

    /** Get RearLeftWheelPressure_V2 **/
    fun get_RearLeftWheelPressure_V2() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 24, 8)

    /** Get FrontRightWheelPressure_V2 **/
    fun get_FrontRightWheelPressure_V2() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 32, 8)

    /** Get FrontLeftWheelPressure_V2 **/
    fun get_FrontLeftWheelPressure_V2() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 40, 8)

    /** Get TPMS_AckResetDriver **/
    fun get_TPMS_AckResetDriver() : Int = this.getFrameParams(CanECUAddrs.MMI_TPMS_CANHS_R_01.idcan, 48, 2)

    /** Get Cluster_ResetRequest **/

    fun get_Cluster_ResetRequest() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 0, 1)

    /** Get TripUnitConsumption **/

    fun get_TripUnitConsumption() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 1, 3)

    /** Get TripUnitDistance **/
    fun get_TripUnitDistance() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 4, 2)

    /** Get TripAverageConsumption **/
    fun get_TripAverageConsumption() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 6, 10)

    /** Get TripDistance **/
    fun get_TripDistance() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 16, 17)
    /** Get TripConsumption **/

    fun get_TripConsumption() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 33, 15)
     /** Get TripAverageSpeed **/

     fun get_TripAverageSpeed() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 48, 12)
     /** Get AdvisorEcoLightingActivationStat **/

     fun get_AdvisorEcoLightingActivationStat() : Int = this.getFrameParams(CanECUAddrs.CLUSTER_CANHS_R_05.idcan, 60, 2)

         /** Get MilageMinBeforeOverhaul **/
     fun get_MilageMinBeforeOverhaul() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANHS_R_07.idcan, 0, 8)

        /** Get  TimeBeforeOverhaul**/
     fun get_TimeBeforeOverhaul() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANHS_R_07.idcan, 8, 9)

       /** Get  AlertMinBeforeOverhaul**/
     fun get_AlertMinBeforeOverhaul() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANHS_R_07.idcan, 17, 2)

/** Get    FlashingIndicatorStatusDisplay **/
fun get_FlashingIndicatorStatusDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 0, 3)
/** Get    RearFogLightStateDisplay **/
fun get_RearFogLightStateDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 3, 1)!=0
/** Get         FrontFogLightsDisplay **/
fun get_FrontFogLightsDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 4, 1)!=0
/** Get         PositionLightsDisplay **/
fun get_PositionLightsDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 5, 1)!=0
/** Get         LowBeamDisplay **/
fun get_LowBeamDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 6, 1)!=0
/** Get         HighBeamDisplay **/
fun get_HighBeamDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 7, 1)!=0
/** Get         PositionLightsOmissionWarning **/
fun get_PositionLightsOmissionWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 8, 2)
/** Get         ALSMalfunction **/
fun get_ALSMalfunctionl() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 10, 1)!=0
/** Get        FrontLeftDoorOpenWarning **/
fun get_FrontLeftDoorOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 11, 2)
/** Get        FrontRightDoorOpenWarning **/
fun get_FrontRightDoorOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 13, 2)
/** Get         KeyInfoReemissionRequest **/
fun get_KeyInfoReemissionRequestl() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 15, 1)!=0
/** Get        RearLeftDoorOpenWarning **/
fun get_RearLeftDoorOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 16, 2)
/** Get         RearRightDoorOpenWarning **/
fun get_RearRightDoorOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 18, 2)
/** Get         BDUSafetyLineFailure **/
fun get_BDUSafetyLineFailure() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 20, 1)!=0
/** Get        SteeringLock_Failure **/
fun get_SteeringLock_Failure() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 21, 2)
/** Get         UnlockingSteeringColumnWarning **/
fun get_UnlockingSteeringColumnWarning() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 23, 1)!=0
/** Get         AutomaticLockUpActivationState **/
fun get_AutomaticLockUpActivationState() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 24, 1)!=0
/** Get         BadgeBatteryLow **/
fun get_BadgeBatteryLow() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 25, 1)!=0
/** Get        ChildproofLockMalfunctionDisplay **/
fun get_ChildproofLockMalfunctionDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 26, 2)
/** Get         TripDisplayScrollingRequest **/
fun get_TripDisplayScrollingRequest() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 28, 2)
/** Get         StartAutoInformationDisplay **/
fun get_StartAutoInformationDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 30, 2)
/** Get         SmartKeylessInformationDisplay **/
fun get_SmartKeylessInformationDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 32, 4)
/** Get         KeylessInfoReemissionRequest **/
fun get_KeylessInfoReemissionRequest() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 34, 1)!=0
/** Get         KeylessCardReaderFailureDisplay **/
fun get_KeylessCardReaderFailureDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 35, 1)!=0
/** Get         DRLstate **/
fun get_DRLstate() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 36, 2)
/** Get         FollowMeHomeDurationDisplay **/
fun get_FollowMeHomeDurationDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 40, 3)
/** Get         SingleDoorOpeningState **/
fun get_SingleDoorOpeningState() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 43, 2)
/** Get        ManualLockDisplayRequest **/
fun get_ManualLockDisplayRequest() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 45, 1)!=0
/** Get         ClutchSwitchFaultDisplay **/
fun get_ClutchSwitchFaultDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 46, 1)!=0
/** Get         BrakeSwitchFaultDisplay **/
fun get_BrakeSwitchFaultDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 47, 1)!=0
/** Get         StopLampFailureDisplay_BCM **/
fun get_StopLampFailureDisplay_BCM() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 49, 1)!=0
/** Get         FlashingIndicatorFailureDisplay **/
fun get_FlashingIndicatorFailureDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 50, 1)!=0
/** Get         VarPowSteeringFailureDisplay **/
fun get_VarPowSteeringFailureDisplay() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 51, 1)!=0
/** Get        AutomaticLockUpInhibitionWarning **/
fun get_AutomaticLockUpInhibitionWarning() : Boolean = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 52, 1)!=0
/** Get        KeyInformationDisplay **/
fun get_KeyInformationDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 52, 3)
/** Get         RearWiperStatus **/
fun get_RearWiperStatus() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 56, 2)
/** Get         BootOpenWarning **/
fun get_BootOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 58, 2)
/** Get         GlasshatchOpenWarning **/
fun get_GlasshatchOpenWarning() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 60, 2)
/** Get         EcoModeStatusDisplay **/
fun get_EcoModeStatusDisplay() : Int = this.getFrameParams(CanMCUAddrs.MMI_BCM_CANHS_R_01.idcan, 62, 2)

    // MCU Params Functions
    /**
     *  MCU Params, Multimedia Data
     *  Radio State
     **/

    /** Get CurrentAudioSourceV2 **/
    fun get_CurrentAudioSourceV2() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 0, 4)

    /** Get CurrentRadioBand **/
    fun get_CurrentRadioBand() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 4, 4)

    /** Get CurrentRadioMode **/
    fun get_CurrentRadioMode() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 8, 2)

    /** Get CurrentRadioPreset **/
    fun get_CurrentRadioPreset() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 10, 4)

    /** Get CurrentAudioStatus **/
    fun get_CurrentAudioStatus() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 16, 2)

    /** Get CurrentPhoneStatus **/
    fun get_CurrentPhoneStatus() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 18, 3)

    /** Get CurrentAudioTrackNumber **/
    fun get_CurrentAudioTrackNumber() : Int = this.getFrameParams(CanMCUAddrs.Audio_Info.idcan, 24, 8)

    /**
     *  Temp / Light
     **/


    /** Get MM_ExternalTemp **/
    fun get_MM_ExternalTemp() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANM_01.idcan, 0, 8)

    /** Get MMNightRheostatedLightMaxPercent **/
    fun get_MMNightRheostatedLightMaxPercent() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANM_01.idcan, 8, 8)

    /** Get MM_DayNightStatusForBacklights **/
    fun get_MM_DayNightStatusForBacklights() : Boolean = this.getFrameParams(CanMCUAddrs.CLUSTER_CANM_01.idcan, 16, 1) !=0

    /** Get MM_CustomerDeparture **/
    fun get_MM_CustomerDeparture() : Int = this.getFrameParams(CanMCUAddrs.CLUSTER_CANM_01.idcan, 17, 7)

    /**
     *  Clock
     **/


    /** Get VehicleClock_Hour **/
    fun get_VehicleClock_Hour() : Int = this.getFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 3, 5)

    /** Sets VehcileClok_hour - Not Use **/
    fun set_VehicleClock_Hour(hour: Int) {
        this.setFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 3, 5, hour)
    }

    /** Get VehicleClock_Minute **/
    fun get_VehicleClock_Minute() : Int = this.getFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 10, 6)

    /** Sets scroll value - Not Use **/
    fun set_VehicleClock_Minute(minute: Int) {
       this.setFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 10, 6, minute)
    }

    /** Get VehicleClock_Second **/
    fun get_VehicleClock_Second() : Int = this.getFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 18, 6)

    /** Sets VehicleClock_Second **/

    fun set_VehicleClock_Second(seconde: Int) {
       this.setFrameParams(CanMCUAddrs.CustomerClockSync.idcan, 18, 6, seconde)
    }
   
      /**
       *  Speed, Oddometer
       **/
  
      /** Get AccurateOdometer from Cluster **/
      fun get_Ac_Oddo_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 0, 16)


      /** Get DisplayedSpeed from Cluster **/
      fun get_Disp_Speed_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 16, 16)
   

      /** Get EspAsrSport Mode **/
      fun get_EspAsrSportMode_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 32, 3)



      /** Get ClutchSwitchMaximumTravel **/
      fun get_ClutchSwitchMaximumTravel_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 35, 2) != 0
   

      /** Get ClutchSwitchMinimumTravel **/
      fun get_ClutchSwitchMinimumTravel_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 37, 2) != 0
   

      /** Get FuelLow_MM **/
      fun get_FuelLow_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 39, 1) != 0

  
      /** Get DisplayedSpeedUnit_MM **/
      fun get_DisplayedSpeedUnit_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 40, 1) != 0
   

      /** Get BrakeInfoStatus_MM **/
      fun get_BrakeInfoStatus_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 41, 3) != 0
   

      /** Get VehicleState_MM **/
      fun get_VehicleState_MM() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 44, 3) != 0
   

      /** Get TCU_MuteRadioOrder **/
      fun get_TCU_MuteRadioOrder() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 47, 1) != 0
   

      /** Get VehicleSpeed **/
      fun get_VehicleSpeed() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data1.idcan, 48, 16)

   /**
    * Acceleration, YawRate
    **/
  
   /** Get LongitudinalAcceleration_MM **/
   fun get_LongitudinalAcceleration_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data2.idcan, 0, 8)
   

   /** Get TransversalAcceleration **/
   fun get_TransversalAcceleration() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data2.idcan, 8, 16)
   

   /** Get SteeringWheelRotationSpeed **/
   fun get_SteeringWheelRotationSpeed() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data2.idcan, 24, 16)
   
   /** Get YawRate **/
   fun get_YawRate() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data2.idcan, 40, 12)
   

   /** Get LongitudinalAccelerationExtented **/
   fun get_LongitudinalAccelerationExtented() : Int = this.getFrameParams(CanMCUAddrs.GW_Chassis_Data2.idcan, 41, 7)

   /**
    *  Engine RPM, Launch Control, Sport Mode, Gear
    **/

    /** Get Engine RPM**/
    fun get_EngineRPM_MMI() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 0, 16)

    /** Get LaunchControlReady **/
    fun get_RST_LaunchControlReady() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 16, 2)

    /** Get RST_LaunchControlReady_MMI **/
    fun get_RST_LaunchControlReady_MMI() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 18, 2)

    /** Get RST_ATMode **/
    fun get_RST_ATMode() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 20, 1)


    /** Get RST_ATPreSelectedRange **/
    fun get_RST_ATPreSelectedRange() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 21, 3)


    /** Get ATCVT_RangeIndication **/
    fun get_ATCVT_RangeIndication() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 24, 5)


    /** Get RST_CentralShifterPosition **/
    fun get_RST_CentralShifterPosition() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 29, 3)


    /** Get RST_ATOilTemperature **/
    fun get_RST_ATOilTemperature() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 32, 8)


    /** Get RST_ATClutchTemperature **/
    fun get_RST_ATClutchTemperature() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 40, 8)

    /** Get AT_MMIParkActivation **/
    fun get_AT_MMIParkActivation() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 48, 2)

    /** Get AT_Parkfailure **/
    fun get_AT_Parkfailure() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data.idcan, 50, 1)

    /**
     *  Cool temp, Oil Temp, etc
     **/
    
    /** Get EngineCoolantTemp **/
   fun get_EngineCoolantTemp() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 0, 8)
   

   /** Get FuelConsumption **/
   fun get_FuelConsumption() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 8, 8)



   /** Get OilTemperature **/
   fun get_OilTemperature() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 16, 8)

 


   /** Get IntakeAirTemperature **/
   fun get_IntakeAirTemperature() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 24, 8)


   /** Get BoostPressure **/
   fun get_BoostPressure() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 32, 6)
   

   /** Get EngineStatus **/
   fun get_EngineStatus() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 38, 2)



   /** Get PowerTrainSetPoint **/
   fun get_PowerTrainSetPoint() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 40, 10)
   

   /** Get CurrentGear **/
   fun get_CurrentGear() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 50, 3)


   /** Get OvertorqueState **/
   fun get_OvertorqueState() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 53, 2)



   /** Get KickDownActivated **/
   fun get_KickDownActivated() : Boolean = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 55, 1) != 0

 
   /** Get EngineOilPressure **/
   fun get_EngineOilPressure() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data1.idcan, 57, 7)

   /**
    *  Throttle, Brake Pressure, Gear
    **/
  
   /** Get EngineRPM **/
   fun get_EngineRPM() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 0, 16)
   

   /** Get MeanEffTorque **/
   fun get_MeanEffTorque() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 16, 12)
   
   /** Get RearGearEngaged **/
   fun get_RearGearEngaged() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 28, 2)
   

   /** Get NeutralContact **/
   fun get_NeutralContact() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 30, 2)
   
   /** Get RawSensor **/
   fun get_RawSensor() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 32, 10)
 
   /** Get GearShiff **/
   fun get_GearShift() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 47, 1)
   
   /** Get BrakingPressure **/
   fun get_BrakingPressure() : Int = this.getFrameParams(CanMCUAddrs.GW_Engine_Data2.idcan, 48, 8)

   /**
    * Steering Wheel Angle
    **/

   /** Get LightSensorStatus_MM**/
   fun get_LightSensorStatus_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_Steering.idcan, 4, 2)

   /** Get SteeringWheelAngle_Offset **/
   fun get_SteeringWheelAngle_Offset() : Int = this.getFrameParams(CanMCUAddrs.GW_Steering.idcan, 8, 16)

   /** Get SteeringWheelAngle **/
   fun get_SteeringWheelAngle() : Int = this.getFrameParams(CanMCUAddrs.GW_Steering.idcan, 24, 16)

   /** Get SwaSensorInternalStatus **/
   fun get_SwaSensorInternalStatus() : Int = this.getFrameParams(CanMCUAddrs.GW_Steering.idcan, 44, 3)

    /**
     *  Navigation to Cluster
     **/

    /** Get code NextAction_Distance **/
    fun get_NextAction_Distance() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 0, 12)

    fun get_NextAction_Unit() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 12, 2)

    fun get_Action_A_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 16, 2)

    fun get_Action_B_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 24, 2)

    fun get_Action_C_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 32, 2)

    fun get_Action_D_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 40, 2)

    fun get_NextActionsIconOrder() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 48, 2)

    /**
     *  Light and DisplayPanel
     **/


    fun get_DayNightStatus_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 4, 2)

    fun get_DisplayActivation() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 6, 2)

    fun get_DimmingValue() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 8, 16)

    fun get_InternalLightingActivationState() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 32, 1)

    fun get_SwitchOffSESDisturbers_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 33, 1)

    fun get_MMDisplayMode_Status_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_MMI_Info1.idcan, 34, 2)

         /**
          *  Returns the most recent Can Frame representing the state
          *  Distance Totalizer
          **/

         fun get_DistanceTotalizer_MM() : Int = this.getFrameParams(CanMCUAddrs.GW_DiagInfo.idcan, 0, 28)

         /**
          *  TODO : test luminosity value
          **/

     }