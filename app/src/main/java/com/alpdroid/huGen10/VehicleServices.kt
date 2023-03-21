package com.alpdroid.huGen10

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Main CLass, writing and reading canFrame value
// giving other value to Cluster like Location, Compass, Directions

@SuppressLint("MissingPermission")
class VehicleServices : LocationListener {

    private val TAG = VehicleServices::class.java.name

    private val application:AlpdroidApplication= AlpdroidApplication.app


    var compassOrientation:Int = 0

    lateinit var lm: LocationManager

    private val destinationLatitude = 90.0 // True North coordinate
    private val destinationLongitude = 0.0 // True North coordinate

    init {
             try {
                 lm = application.getSystemService(LOCATION_SERVICE) as LocationManager

                 lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this)
                 lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)

             } catch (ex: java.lang.Exception) {
                 //usually permissions or
                 //java.lang.IllegalArgumentException: provider doesn't exist: network
                // ex.printStackTrace()
                 Log.d("init gps", "pass here")
             }


         }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 123
        private const val MIN_TIME_BW_UPDATES: Long = 500
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1.0f
        private const val TO_RADIANS = Math.PI / 180
        private const val TO_DEGREES = 180 / Math.PI

        private var currentBearing = 0f
    }

    fun onClose()
    {
        lm.removeUpdates(this)
      //  application.alpdroidServices.onDestroy()
    }

    private fun getBearing(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
        ): Float {
        Log.d("getbering  gps", "pass here")
            val lat1 = startLatitude * TO_RADIANS
            val lat2 = endLatitude * TO_RADIANS
            val deltaLong = (endLongitude - startLongitude) * TO_RADIANS
            val y = sin(deltaLong) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLong)
            var bearing: Float = 0f

            bearing= (atan2(y, x) * 180 / Math.PI).toFloat()


            Log.d("en getbearing gps", "pass here")
            if (bearing < 0) bearing += 360
            return bearing.toFloat()
    }

    override fun onLocationChanged(location: Location) {

        val bearing = getBearing(location.latitude, location.longitude, destinationLatitude, destinationLongitude)

        compassOrientation = bearing.toInt()
        Log.d("on location change", "pass here")
        currentBearing = bearing

    }


    // Update Regular Services

    fun get_CompassOrientation() : Int {
        return compassOrientation
    }


    //TODO : ajouter la gestion du bus
    fun getFrameParams(canID:Int, bytesNum:Int, len:Int): Int {
        val frame:CanFrame

        try {
            frame= application.alpineCanFrame.getFrame(canID)!!
        }
        catch (e: Exception)
        {
            return 0
        }

        return frame.getValue(bytesNum,len)

    }

    suspend fun pushOBDParams(candIDSend:Int, servicePID:Int, serviceDir: Int, bytesData:ByteArray)
    {
        val frame2OBD:CanFrame
        var obdframe: OBDframe

        val serviceData:ByteArray = ByteArray(8)

        val mutex_push:Mutex=Mutex()

        var dlc_offset=0

        try {
            if ((serviceDir and 0xBF)<0x20) {
                (2 + bytesData.size).toByte().also { serviceData[0] = it }
                serviceData[1] = serviceDir.toByte()
                serviceData[2] = (servicePID).toByte()
            }
            else
            {
                (3 + bytesData.size).toByte().also { serviceData[0] = it }
                serviceData[1] = serviceDir.toByte()
                serviceData[2] = (servicePID/256).toByte()
                serviceData[3] = (servicePID).toByte()
                dlc_offset=1
            }

            for (i in 3+dlc_offset..7) {
                if (i<bytesData.size)
                   serviceData[i] = bytesData[i - (3+dlc_offset)]
                else
                    serviceData[i] = 0x55.toByte()
            }

            obdframe = OBDframe(candIDSend,0, serviceData)

            frame2OBD = CanFrame(1, candIDSend, serviceData)
            CoroutineScope(Dispatchers.IO).launch {
                mutex_push.withLock {
                    application.alpdroidServices.sendFrame(frame2OBD)
                    // We need to wait P2Can min wait between 2 OBD Messages
                    delay(50)
                }


            }
        }
        catch (e:java.lang.Exception)
        {
            // do nothing
        }
    }


    fun getOBDParams(servicePID:Int, serviceDir:Int, bytesNum:Int, len:Int):Int
    {
        val frameOBD:OBDframe


        try {

            frameOBD = application.alpineOBDFrame.getFrame(servicePID,serviceDir)!!

        }
        catch (e: Exception)
        {
            return 0
        }

        return frameOBD.getValue(bytesNum,len)

    }

    @Synchronized
    fun setFrameParams(candID:Int, bytesNum:Int, len:Int, param:Int) {

        var frame: CanFrame

        application.alpineCanFrame.getFrame(candID).also {
            if (it != null) {

                frame=it

                // Set in given range

                frame.setBitRange(bytesNum,len,param)

                application.alpineCanFrame.addFrame(frame)
            }
        }

    }

    fun getFrameBool(candID:Int, bytesNum:Int): Boolean {

        application.alpineCanFrame.getFrame(candID).also {
            if (it != null) {
                return it.getBit(bytesNum)
            }
        }
        return false
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}


    // ECU Params Functions
    /**
     *  Return the state of the frame parameters
     *  Some params are note available on all Renault Cars
     *  GearBox & Torque
     **/

    /** Get code GearboxOilTemperature **/

    fun get_BattV2() : Float = (this.getOBDParams(0x1103, 0x62,0, 8)*8/100).toFloat()

    suspend fun ask_OBDBattV2()
    {
        pushOBDParams(CanECUAddrs.CANECUSEND.idcan,0x1103, 0x22, ByteArray(0))
       }
    fun get_TyreTemperature1() : Int = this.getOBDParams(0x8011, 0x62,0, 8)-30
    fun get_TyreTemperature2() : Int = this.getOBDParams(0x8018, 0x62,0, 8)-30

    fun get_TyreTemperature3() : Int = this.getOBDParams(0x8025, 0x62,0, 8)-30

    fun get_TyreTemperature4() : Int = this.getOBDParams(0x8032, 0x62,0, 8)-30

    suspend fun ask_OBDTyreTemperature()
    {
        pushOBDParams(0x745,0x8011, 0x22, ByteArray(0))
        pushOBDParams(0x745,0x8018, 0x22, ByteArray(0))
        pushOBDParams(0x745,0x8025, 0x22, ByteArray(0))
        pushOBDParams(0x745,0x8032, 0x22, ByteArray(0))

    }

    suspend fun ask_OBDStandardCode()
    {
        pushOBDParams(0x7DF,0, 0x01, ByteArray(0))

    }


    suspend fun get_PTDCCode()
    {
        pushOBDParams(0x7DF,0,0x03,ByteArray(0))



    }
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
    // TORQUE_ECM_CANHS_RNr_01
    fun get_InternalTemp() : Int = this.getFrameParams(CanECUAddrs.CLIM_CANHS_R_03.idcan, 40, 16)

    /**
     *  Oil, Batt, Washer Level, FuelCooling
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

    /** CoolingFanSpeedStatus **/

    fun get_CoolingFanSpeedStatus() : Int = this.getFrameParams(CanECUAddrs.ECM_CANHS_R_03.idcan, 48, 2)
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


    /** Get code FrontLeftWheelTemperature **/
    fun get_FrontLeftWheelTemperature() : Int = this.getFrameParams(CanECUAddrs.TPMS_Rst1.idcan, 0, 8)

    /** Get Code FrontRightWheelTemperature **/
    fun get_FrontRightWheelTemperature() : Int = this.getFrameParams(CanECUAddrs.TPMS_Rst1.idcan, 8, 8)

    /** Get code RearLeftWheelTemperature **/
    fun get_RearLeftWheelTemperature() : Int = this.getFrameParams(CanECUAddrs.TPMS_Rst1.idcan, 16, 8)

    /** Get Code RearRightWheelTemperature **/
    fun get_RearRightWheelTemperature() : Int = this.getFrameParams(CanECUAddrs.TPMS_Rst1.idcan, 24, 8)

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
     * Start & Stop request
     */

    fun get_StartAutoAuthorization() : Int = this.getFrameParams(CanECUAddrs.SSCU_CANHS_R_02.idcan,2,2)

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

    /** Get and Set code Navigation -  NextAction_Distance, Unit, Icon A B C D, Order **/
    fun get_NextAction_Distance() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 0, 12)

    fun set_NextAction_Distance(distance: Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 0, 12, distance)

    fun get_NextAction_Unit() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 12, 2)

    fun set_NextAction_Unit(unit: Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 12, 2, unit)

    fun get_Action_A_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 16, 2)

    fun set_Action_A_Icon(icon : Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 16, 2, icon)

    fun get_Action_B_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 24, 2)

    fun set_Action_B_Icon(icon : Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 24, 2, icon)

    fun get_Action_C_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 32, 2)

    fun set_Action_C_Icon(icon : Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 32, 2, icon)

    fun get_Action_D_Icon() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 40, 2)

    fun set_Action_D_Icon(icon : Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 40, 2, icon)

    fun get_NextActionsIconOrder() : Int = this.getFrameParams(CanMCUAddrs.RoadNavigation.idcan, 48, 2)

    fun set_NextActionsIconOrder(next : Int)  = this.setFrameParams(CanMCUAddrs.RoadNavigation.idcan, 48, 2, next)

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
     * BCM and StopStart
     */

    fun get_StopStartSwitch() : Int = this.getFrameParams(CanECUAddrs.BCM_CANHS_R_08.idcan,26, 2)

    fun get_BatteryVoltage_V2() : Int = this.getFrameParams(CanECUAddrs.BCM_CANHS_R_08.idcan,8, 8)

    fun get_BCM_StopAutoForbidden() : Int = this.getFrameParams(CanECUAddrs.BCM_CANHS_R_08.idcan, 16,2)

    /** MMI Brake
     * brake ESP ABS State
     */

    fun get_ESPDeactivatedByDriverForDisplay() : Boolean = this.getFrameParams(CanECUAddrs.MMI_BRAKE_CANHS_RNr_01.idcan,5, 1)!=0

}