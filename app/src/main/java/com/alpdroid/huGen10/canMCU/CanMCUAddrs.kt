
package com.alpdroid.huGen10
/**
 *   MCU Address data / CAN MCU
 **/
// Dans certains cas rares, les 2 BUS utilisent la même adresse, il faut distringuer les BUS
// A priori concerne uniquement :
/* <SentBytes>03B7</SentBytes>
   <SentBytes>0405</SentBytes>
   <SentBytes>0589</SentBytes>
   <SentBytes>0597</SentBytes> // cas particulier = la même trame des 2 côtés
*/

enum class CanMCUAddrs(val idcan: Int) {
	CSV_CANM_01(0x02D0),
	CLUSTER_CANM_01(0x03B7), //doublon
	GW_AT_Data(0x0589), //doublon
	GW_Chassis_Data1(0x058B),
	GW_Chassis_Data2(0x058C),
	GW_Engine_Data1(0x0592),
	GW_Engine_Data2(0x058E),
	Audio_Info(0x05EB),
	Audio_Display(0x0611),
	CustomerClockSync(0x0396),
	GW_Steering(0x05A8),
	GW_MMI_Info1(0x0558),
	RoadNavigation(0x0399),
	Compass_Info(0x0405), // doublon
	GW_DiagInfo(0x0578),
	UserSetPrefs2_MM(0x0402),
	CLUSTER_CANHS_R_07(0x05CE),
	MMI_BCM_CANHS_R_01(0x05DE),
	CANMCUREC_MMU(0x767),
	CANMCUSEND_MMU(0x747), // Navigation / Multimedia / Télémétrie
	CANMCUSEND(0x712), // Audio - Audio Unit
	CANMCUREC(0x732),

}
