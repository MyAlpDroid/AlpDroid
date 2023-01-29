# Program for Ardunio UNO rev3

The program for Arduino will take care of receiving the CAN frames from the CANSHIELD(s) and sending the frame in GSON format back to the Android station.

The frames are always of the type:
CAN BUS NUMBER: 0 or 1 depending on the number of CANSHIELDs
CAN Frame ID: Frame sender address between 0 and 0x7FF. These are the addresses of components on the ECU/MMU etc network that can send or receive a frame.
DATA: the data of the frame on 8 Bytes.

Example: {bus:0,id:0x07E8,data:{0x0,0x1,0x2,0x3,0x4,0x5,0x6,0x7}}

-------------------------------------------------- ---------------------
*** CAUTION ***: CANBUS shield boards use either 8Mhz quartz or 16Mhz quartz. For unknown reasons, the MCP2515 library seems to be able to manage the type of quartz and it would suffice to indicate the selected speed for everything to work ... but it does not. As a result, the speed is forced to 500K and 16Mhz for the use of the dual shield peak and the values must be replaced with that of 8Mhz for the basic can shields.
-------------------------------------------------- ---------------------

If necessary, we will modify the following lines of the mcp2515.h file:
If we have a quartz at 8Mhz, we take the values 00,90,82 and if not for 16Mhz we keep 00,F0,86
*
  * force 8Mhz for 500K speed: change here by original value if using 16Mhz canbus
  */
//#define MCP_16MHz_500kBPS_CFG1 (0x00)
//#define MCP_16MHz_500kBPS_CFG2 (0x90)
//#define MCP_16MHz_500kBPS_CFG3 (0x82)

#define MCP_16MHz_500kBPS_CFG1 (0x00)
#define MCP_16MHz_500kBPS_CFG2 (0xF0)
#define MCP_16MHz_500kBPS_CFG3 (0x86)

About the program:

We made the choice to create a permanent reading loop and not to use Interruptions.

CANSHIELDs identify themselves by transmitting a signal on PIN 9 or 10.
Attention, one PIN per CANBUS so either 9 or 10 but not the same for both:

Change these 2 defines if your CS pins are different
They CANNOT be the same

#define CANMMU_CS 9

#define CANECU_CS 10

Interrupts can be useful when you receive few messages. Here, in particular because of the CAN ECU, too many frames are received for use by interrupt. The system saturates too quickly.

#define CANMMU_INT 3
#define CANECU_INT 2

Finally the program exploits other PINS to process the needs around the communication with the CANSHIELD as to emit the signal towards KEY1 for the steering wheel controls:
// Other Arduino PINS
// SCK = 13
// IF = 11
// SO = 12
// Pin to drive CD4051
// 4 5 6 = S0 to S2 coding


#define Enable_CD 7

MMU frames are few, ECU frames are very many and may overload the USB connection.
As the workstations are mostly USB 2.0, communication is not bidirectional. USB can therefore quickly saturate and it becomes impossible to receive or transmit on one of the channels.

It is therefore necessary to provide for a latency on the ECU frames to allow "time" for the USB interface to dialogue:

   if (delayTime+random(10)<millis())
               {
                 delayTime=millis();

                  // CAN ECU(1) to Android
                  // wait a little between to ECU frame - due to heavy flow form ECU
                  writeFrame(1, &io_can_frame_read);
               }
   For the same reasons, we did the same on writing to the MMU.
              
   Moreover, if the "delayTime" time is too high, or if the Arduino loses the 'thread' of the frames, too many frames are lost or even all the frames are lost for lack of synchronization. And if the time is too low, too much frame is lost from Android to Alpine. It has therefore been added to the frames from Android a control on 2 characters '@@' allowing to reduce the latency time in transmission (between 0 and 10 milliseconds instead of 50) while keeping a control of the quality of the frames in reception.

If the station has USB 3.0, it is possible to test two-way communication by removing the 10 millisecond delay above.

By default, writing to the ECU CAN bus is prohibited. To be changed if you want to send frames to ECU.

       case 1: // do not write to CAN ECU, if you are not sure of what you do
              // canECU->sendMessage(&io_can_frame);
            break;
            
            
## How to install the program on the Arduino:
Install the Arduino IDE environment on a PC or Mac: https://www.arduino.cc/en/Guide
Load the Arduino UNO project
* Upload via GitHub the files from the UNO CODE folder to a directory.
* Open Arduino IDE, File , Open, Choose directory and open
Compile and Upload the program:
You need a type B USB cable (printer type on the Arduino side) and type C or another PC side.
Once the Arduino program has been loaded into the IDE, and the cable connected to the Arduino, all you have to do is upload the program to the Arduino.
https://docs.arduino.cc/software/ide-v2/tutorials/getting-started/ide-v2-uploading-a-sketch
* In the IDE, choose the Arduino UNO card connected to the PC / Mac
* then Tools / Port / "name of your USB/Card port"
* then Sketch / Upload

If you have selected the wrong USB port, you will see a message appear repeatedly (10 times) indicating that it cannot find the card.

Once the program is loaded, it launches automatically as soon as the card is powered and loops indefinitely.

You can test that the program works by plugging the board with its CANSHIELD into one of the Alpine's CAN buses via the OBD socket and plugging the Arduino board, via USB, into an Android tablet or phone and using the excellent "USB SERIAL TERMINAL" application:
https://play.google.com/store/apps/details?id=de.kai_morich.serial_usb_terminal&hl=fr&gl=US

Set the application to a rate of 230,400 Bauds, connect to the Arduino once detected and you will see the GSON messages representing the CAN frames appear.
