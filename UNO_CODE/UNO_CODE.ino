 
#include <Arduino.h>
#include "mcp2515.h"
#include <avr/wdt.h>


// Change these 2 defines if your CS pins are different
// They CANNOT be the same
#define CANMMU_CS 9
#define CANECU_CS 10

// Interrupts (not use in this version)
#define CANMMU_INT 3
#define CANECU_INT 2

// Other PINS
// SCK = 13
// SI = 11
// SO = 12
// Pin to drive CD4051
// 4 5 6 = S0 to S2 coding
#define Enable_CD 7


MCP2515* canECU;
MCP2515* canMMU;


// IO struct with the tablet
// It is a compressed CAN Frame
struct tablet_frame {
    uint8_t can_bus_id;
    uint16_t id; 
    uint8_t dlc;
    uint8_t data[8];
};

const uint8_t FRAME_SIZE = sizeof(tablet_frame);

tablet_frame io_frame = {0x00}; // Reserve in memory
can_frame io_can_frame = {0x00}; // Reserve this in memory as well
can_frame io_can_frame_read = {0x00};

// Using Arduino pins 4-7 for CD4051 ctrl
const int table_output_pins[] = {4, 5, 6};


char writeBuf[53]={0x00};


int button_scroll=0;
int button_prev_scroll=0;
int button_press=0;

int m_prevSignal=99;

bool swc_prev=true;

unsigned long delayTime=0;



// Init all pins for 4051

void init_output_pins(const int* m_outPins)
{

// Starting with CD4051 not enable
pinMode (Enable_CD, OUTPUT);
digitalWrite(Enable_CD, HIGH);

for (int pinIdx = 0; pinIdx < 4; pinIdx++)
  {
    pinMode(m_outPins[pinIdx], OUTPUT);
    digitalWrite(m_outPins[pinIdx], LOW);
  } 
}

// Enabling CD4051 - set Key1

void active_CD4051()
{
  digitalWrite(Enable_CD, LOW); 
}

// Stop CD4051 - reset Key1

void inactive_CD4051()
{
  digitalWrite(Enable_CD, HIGH);
}

// Reset the pins before setting

void put_output_pins(const int* m_outPins, char sens)
{ 
 
  for (int pinIdx = 0; pinIdx < 4; pinIdx++)
    {
      digitalWrite(m_outPins[pinIdx], sens);
    }
}

// Set the pins binary code from 000 to 111

void set_output_pins(const int* m_outPins, int m_outSignal)
{ 
  // Settings pins
  if (m_outSignal!=m_prevSignal)
  {
    put_output_pins(m_outPins, LOW);

    for (int pinIdx = 0; pinIdx < 4; pinIdx++)
    {
      if (((unsigned int)1 << pinIdx) & m_outSignal)
        {
        digitalWrite(m_outPins[pinIdx], HIGH);
        }

    }
   m_prevSignal=m_outSignal;
  }
  
}

// Write frame as GSON message
// bus , id, data

void writeFrame(uint8_t bus_id, can_frame* f) {
      

    uint8_t pos = sprintf(writeBuf, "{\"bus\":%1d,\"id\":%04X,\"data\":[", bus_id, f->can_id);
    for (int i = 0; i < f->can_dlc; i++) {
        pos+=sprintf(writeBuf+pos, "%02X,", f->data[i]);
    }
    if (f->can_dlc>0) pos-=1;
    pos+=sprintf(writeBuf+pos,"]");
    pos+=sprintf(writeBuf+pos,"}");
    writeBuf[pos] = '\n'; 
    Serial.write(writeBuf, pos+1);
    

}


void setup() {

    
    // If not up and running within 8s then there is probably some
    // CAN init problem, a restart is our last hope.
    
     wdt_enable(WDTO_8S);
    
    // Wait a little while before starting to configure e.g. CAN controller
    // so that all hw will be ready

    delay(500);
  

    // Init serial to 115200 Bauds (max support by Arduino with Android)
    Serial.begin(115200);

    // Init the CAN modules
    canMMU = new MCP2515(CANMMU_CS);
    canECU = new MCP2515(CANECU_CS);
    canMMU->reset();
    canECU->reset();
    canMMU->setBitrate(CAN_500KBPS);
    canECU->setBitrate(CAN_500KBPS);

    // Can MMU & ECU = Set it as Read + Write 
    // But write to ECU at your own risk
    canMMU->setNormalMode();
    canECU->setNormalMode();
  
    
    // Init Output PINS
    init_output_pins(table_output_pins);

    // A watchdog might be a good idea. Imagine the program hanging while 
    // "volume up" output is active...
    wdt_enable(WDTO_250MS);    

}



void loop() {
  
// If blocked
wdt_reset();

 while (Serial.available()< FRAME_SIZE)   
 { 
   // nothing to do wait for end of incoming frame from USB but no blocking
   wdt_reset();
   // Poll for any new CAN frames on Bus MMU(0)
   if (canMMU->readMessage(&io_can_frame_read) == MCP2515::ERROR_OK) 
    {   
        
        if (io_can_frame_read.can_id==0x02D0)
          {
            button_scroll=io_can_frame_read.data[0];
            // intercept frame from steering wheel control - address pins button_press
            // Value 0 = Scroll Up or Down Button = value between 0 to FF
            // Value 1 = Other buttons - We have 8 values available from 0 to 7
             // If Scroll up or down, depend on previous value
              if (button_scroll>button_prev_scroll)
                {
                  // scroll up
                   button_prev_scroll=button_scroll;
                   set_output_pins(table_output_pins,6);
                   active_CD4051();
                   delay (50);
                   //let a chance to be seen by android system

                }
              else    
                  if (button_scroll<button_prev_scroll)
                 {
                   // scroll down
                   button_prev_scroll=button_scroll;
                   set_output_pins(table_output_pins,7);
                     active_CD4051();
              //     t1.enable();
                    delay (50);
                   //let a chance to be seen by android system
                 }
                

              switch (io_can_frame_read.data[1])
                {
                 case 0:
                 // Key release , inactive all Key1 Value  
                    inactive_CD4051();
                    break;
                  case 1:
                  // 1 : Bouton du bas        
                    set_output_pins(table_output_pins,0);
                    active_CD4051();
                    break;
                  case 2:   
                  // 2 : Bouton Haut Droite       
                    set_output_pins(table_output_pins,1);
                    active_CD4051();
                    break;
                  case 4:
                  // 4 : Bouton Haut Gauche
                    set_output_pins(table_output_pins,2);
                    active_CD4051();  
                    break;
                  case 8:
                  // 8 : Volume DOWN                  
                     set_output_pins(table_output_pins,3);
                     active_CD4051();      
                    break;
                  case 16:
                  // 16 (10 en hexa) : Volume UP
                     set_output_pins(table_output_pins,4);
                     active_CD4051();
                    break;
                  case 24:
                  // 24 (18 Hex) : MUTE
                     set_output_pins(table_output_pins,5);
                     active_CD4051();   
                    break;
                  default:
                // No key or wrong value 
                   break;
              }
            
          }
        else 
          {        
                  // CAN MMU to Android
                  writeFrame(0, &io_can_frame_read);

           }
    }

    // Poll for any new CAN frames on Bus ECU
   if (canECU->readMessage(&io_can_frame_read) == MCP2515::ERROR_OK) {
           if (delayTime+50<millis())
              {
                delayTime=millis();                                  

                 // CAN ECU(1) to Android 
                 // wait a little between to ECU frame - due to heavy flow form ECU
                 writeFrame(1, &io_can_frame_read);
              }
    
    }     

 }  

  
  Serial.readBytes((char *)&io_frame, FRAME_SIZE); 

  io_can_frame.can_id = io_frame.id;
  io_can_frame.can_dlc = io_frame.dlc;

  if (io_frame.dlc>0 && io_frame.dlc<=8)
   {
        memcpy(io_can_frame.data, io_frame.data, io_frame.dlc); 
  
        // Android to CAN MMU or ECU

        switch (io_frame.can_bus_id)
          {
           case 0:
              canMMU->sendMessage(&io_can_frame);
           break;
       
           case 1: // do not write to CAN ECU, if you are not sure of what you do
             // canECU->sendMessage(&io_can_frame);
           break;

           default:
            // bad frame
           break;
         }
    }

 
// If blocked...
wdt_reset();
  

}