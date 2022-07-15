# Programme pour Ardunio UNO rev3

**Change these 2 defines if your CS pins are differents**
**They CANNOT be the same**
#define CANMMU_CS 9
#define CANECU_CS 10

**Interrupts (not use in this version)**
#define CANMMU_INT 3
#define CANECU_INT 2

**Other Arduino PINS**
// SCK = 13
// SI = 11
// SO = 12
// Pin to drive CD4051
// 4 5 6 = S0 to S2 coding
#define Enable_CD 7

Les trames MMU sont peu nombreuses, les trames ECU sont très nombreuses et peuvent surcharger la connexion USB.

Il faut donc prévoir une latence sur les trames ECU pour laisser "le temps" à l'interface USB de dialoguer
  if (delayTime+50<millis())
              {
                delayTime=millis();                                  

                 // CAN ECU(1) to Android 
                 // wait a little between to ECU frame - due to heavy flow form ECU
                 writeFrame(1, &io_can_frame_read);
              }
Par défaut l'écriture sur le bus CAN ECU est interdit. A changer si on veut envoyer des trames vers ECU.
      case 1: // do not write to CAN ECU, if you are not sure of what you do
             // canECU->sendMessage(&io_can_frame);
           break;
