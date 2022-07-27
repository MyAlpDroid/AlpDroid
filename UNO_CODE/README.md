# Programme pour Ardunio UNO rev3

Le programme pour Arduino va se charger de recevoir les trames CAN depuis le ou les CANSHIELD et de renvoyer la trame au format GSON vers le poste Android.

Les trames sont toujours du type :
NUMERO DU BUS CAN : 0 ou 1 selon le nombre de CANSHIELD  
ID de la Trame CAN : Adresse de l'expéditeur de la trame comprise entre 0 et 0x7FF. Il s'agit des adresses des composants sur le réseau ECU/MMU etc qui peuvent envoyer ou recevoir une trame.  
DATA : les données de la trame sur 8 Octects.  

Exemple : {bus:0,id:0x07E8,data:{0x0,0x1,0x2,0x3,0x4,0x5,0x6,0x7}}  


Concernant le programme : 

On a fait le choix de réaliser une boucle de lecture permanente et de ne pas utiliser les Interruptions. 

Les CANSHIELD s'identifient en émettant un signal sur la PIN 9 ou 10. 
Attention, un PIN par CANBUS donc soit 9 soit 10 mais pas le même pour les 2 : 

Change these 2 defines if your CS pins are differents 
They CANNOT be the same 

#define CANMMU_CS 9 

#define CANECU_CS 10 

Les interruptions peuvent être utile quand on reçoit peut de message. Ici, notamment en raison du CAN ECU, on reçoit trop de trame pour un usage par interruption. Le système sature trop vite.  

#define CANMMU_INT 3  
#define CANECU_INT 2  

Enfin le programme exploite d'autres PINS pour traiter les besoins autour de la communication avec les CANSHIELD comme pour émettre le signal vers KEY1 pour les commandes au volant :  
// Other Arduino PINS  
// SCK = 13  
// SI = 11  
// SO = 12  
// Pin to drive CD4051  
// 4 5 6 = S0 to S2 coding  
#define Enable_CD 7  

Les trames MMU sont peu nombreuses, les trames ECU sont très nombreuses et peuvent surcharger la connexion USB.
Comme les postes sont majoritairement en USB 2.0, la communication n'est pas bidirectionnel. L'USB peut donc vite saturer et il devient impossible de recevoir ou d'émettre sur l'un des canaux. 

Il faut donc prévoir une latence sur les trames ECU pour laisser "le temps" à l'interface USB de dialoguer :  

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

Si le poste dispose d'USB 3.0, il est possible de tester la communication bidirectionnelle en supprimant le délai de 50 millisecondes ci-dessus.


## Comment installer le programme sur l'Arduino :  
Installer sur un PC ou un Mac l'environnement IDE Arduino : https://www.arduino.cc/en/Guide
Charger le projet Arduino UNO
* Charger via GitHub les fichiers du dossier UNO CODE dans un répertoire.  
* Ouvrir l'IDE Arduino, File , Open, Choisir le répertoire et ouvrir
Compiler et Uploader le programme :  
Il vous faut un câble USB du type B (type imprimante du côté Arduino) et type C ou autre côté PC.
Une fois le programme Arduino chargé dans l'IDE, et le câble branché sur l'Arduino, il suffit d'uploader le programme sur l'Arduino.
https://docs.arduino.cc/software/ide-v2/tutorials/getting-started/ide-v2-uploading-a-sketch  
* Dans l'IDE , choisir la carte Arduino UNO connecté sur le PC / Mac
* puis Tools / Port / "nom de votre port USB/Carte"
* puis Sketch / Upload

Si vous avez sélectionné le mauvais port USB, vous verrez apparaitre en boucle (10 fois) un message indiquant qu'il ne trouve pas la carte.

Une fois le programme chargé, il se lance automatiquement dès qu'on alimente la carte et boucle indéfiniment.

Vous pouvez tester le bon fonctionnement du programme en branchant la carte avec son CANSHIELD sur l'un des bus CAN de l'Alpine via la prise OBD et en branchant la carte Arduino, via USB, sur une tablette ou un téléphone Android et en utilisant l'excellente application "USB SERIAL TERMINAL" : 
https://play.google.com/store/apps/details?id=de.kai_morich.serial_usb_terminal&hl=fr&gl=US

Régler l'application sur un débit de 115 200 Bauds, connecter sur l'Arduino une fois détecter et vous verrez les messages GSON représentant les trames CAN apparaitre.




