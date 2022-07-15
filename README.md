<H1> Alpine : Remplacer le poste multimédia  par un poste Android </H1>

Le poste multimédia Alpine présente 2 inconvénients : il n'est pas compatible Android Auto/Apple CarPlay. Il a perdu au fur et à mesure les apps intéressantes comme Spotify via MySpin. On pourrait ajouter que l'écran 7 pouces peut sembler un peu petit.

Il présente aussi comme inconvénient qu'Alpine a manifestement décidé qu'il n'y aurait pas d'upgrade de celui-ci. Ceci étant d'autant plus dommage qu'il s'agit du même poste Multimédia **Bosch** que celui qu'on retrouve dans la Suzuki Swift 2014 et que l'écran **Valeo** est le même que celui du Renault Kadjar 2016.
Pour ces deux modèles, des upgrades ont été livrés pour faire bénéficier d'Android & Apple leurs utilisateurs. 
Bon, après, on parle de véhicules très haut de gamme (la swift et le Kaddjar), normal qu'il y ait un peu d'animation en après vente...

Concernant l'Alpine, il est assez complexe de travailler sur une carte qui aurait pour objet d'intercepter le signal vidéo de l'Alpine pour permettre de switcher avec un adaptateur Android Auto ou CarPlay. Après avoir longuement étudié la question, la solution a semblé relativement couteuse et longue à produire. 

L'autre solution pourrait consister à adapter un poste Android avec l'écran de l'Alpine. On verra plus loin qu'il faudrait redévelopper une carte affichage et ajouter un contrôleur CAN pour réussir l'opération. C'est possible. Un peu complexe mais possible.

**La solution proposée ici** est donc d'intégrer un poste multimédia Android dernière génération à la place de l'ancien poste et de remplacer l'écran 7 pouces par l'écran de 8 à 11 pouces livré avec le poste Android.

Pour ma part, j'ai choisi ce poste de chez Joying :
[Joying Auto 10.1' Android 10](https://www.joyingauto.eu/joying-android-10-0-autoradio-10-1-inch-1280-800-screen-single-1din-car-stereo.html)

Tout l'enjeu est maintenant de faire fonctionner ce poste en lieu est place du "formidable" duo Valeo Bosch de l'Alpine A110.

Mais en premier, ce projet a pour objet de partager toute l'expérience et de permettre à ceux qui le souhaitent de faire la même chose et de contribuer à l'amélioration de la solution.

## **Les composants existants**

### Le poste Multimédia
Il est d'origine **BOSCH**. 
On peut retrouver la documentation sur les modules opensource utilisés pour son logiciel [ici](https://oss.bosch-cm.com/alpine.html) 

L'arrière du poste se présente ainsi :
![ ](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/Arrie%CC%80re%20Poste%20Alpine.jpg)

De gauche à droite : 

BLEU : **Connecteur d'Alimentation** intégrant +12 / GND , 4 hauts parleurs et Acc 

BLANC : **Connecteur CAN +Speed/ABS** le vert CAN High, le premier blanc CAN Low, le dernier blanc Speed/Abs

Noir/Embase Marron : **Connecteur USB** renvoi vers les prises USB de la console


Vert : **Connecteur Caméra de recul**

Gris/Embase Blanche : **Connecteur Micro** Rouge +5v, Noir Ground, Jaune Signal micro

Blanc/Embase Crème : **Connecteur lecteur de carte** 

Noir : **Connecteur Antenne DAB+** 

Gris : **Connecteur Antenne Radio**

Bleu : **Connecteur GPS**

![ ](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/Resultat%20poste.jpg)

Enfin Connecteur Violet (face avant) : **Données Vidéo Tablette Multiplexée+TouchScreen**


Si on veut remplacer ce poste, nous devons donc à minima récupérer le signal d'antenne Radio, DAB+ si le nouveau poste l'a, le GPS, l'USB console, le Micro et l'Alimentation.

Selon les postes, on peut envisager de reprendre la caméra de recul (n'a pas été fait dans ce projet), le lecteur de carte (non fait ici, un adaptateur vers l'USB semble possible) et le CAN. Même si je n'ai pas trouvé de poste "Chinois" Android apportant la prise en charge directe du CAN.


### L'écran

L'écran Valéo se démonte relativement facilement. Il y a deux caches derrière l'écran qu'on peut faire sauter simplement, puis il suffit de déviser les 2 vis et sortir l'écran en poussant légèrement vers le haut.

A l'arrière de l'écran, on a deux connecteurs :

Connecteur type USB : **arrivée vidéo du poste**
Connecteur Noir : **alimentation & CAN** (Rouge+12v, Noir GND, Vert CANH, Blanc CANL)

Cela signifie que l'écran récupère l'image et le multiplexage du touchscreen via un lien LVDS (connecteur type USB & Fakra Violet) et se met en marche sur la base d'une trame CANBUS qui lui indique s'il faut s'allumer ou non.
Voici le briochage des connecteurs de ce câble:
![](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/HSD%20et%20USB.png)

La trame est probablement celle-ci :
058F code opération sur 4 octets, 2bits 0 :sleep 11:Réveil 2 bits : type de réveil

Ce n'est pas l'objet de ce projet, mais si quelqu'un voulait adapter un nouveau poste, sans démonter l'écran, il devrait :
1) Adapter la sortie vidéo du poste Android (généralement standard vidéo sur 40 pins) vers une sortie multiplexé LVDS avec un sérialiseur désérialiseur.
2) Emettre, depuis le poste (si compatible CAN) ou via un kit Arduino, une trame 058F pour réveiller l'écran.
3) (Probablement) Adapter la gestion du tactile de l'écran pour être compris par le poste comme un écran normal.

J'ai tenté la création d'une carte SerDes mais faute de temps et de compétences avancées, j'ai échoué. Je pense qu'il s'agirait pourtant d'une solution moins intrusive que de changer l'écran.


## Les BUS CAN de l'Alpine, le connecteur OBD II

L'Alpine exploite deux bus CAN :
* Un bus ECU : paramètres moteur et tout composant non "multimédia"
* Un bus MMU : composants Multimédia : Ecran, Poste, Caméra de récul, Tableau de bord

A noter que des informations du bus ECU sont transmises au bus MMU par l'intermédiaire d'une interface. 

Est-ce le tableau de bord qui remplit ce rôle ou un autre composant ?
En tout cas, la télémétrie ne s'appuie pas directement sur les informations de l'ECU mais bien sur celles "renvoyées" sur le bus MMU.

L'existence de ces 2 bus expliquent aussi pourquoi, via un dongle OBDII (type ELM) vous ne voyez en général que les défauts "moteurs". L'explication se trouve dans le brochage du connecteur OBD qui permet d'accéder aux 2 Bus :
![Schéma Prise OBD](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/Prise%20OBD.png)

Ainsi, lorsque vous voulez utilisez DDT4ALL par exemple et souhaitez accéder aux menus qui concernent la partie multimédia, vous devez changer le brochage de votre ELM en utilisant un inverseur comme sur le schéma ci-dessus.
(L'activation de la télémétrie passe par un menu accédant au CAN multimédia par exemple).

Le bus Multimédia est accessible directement en se raccordant sur la broche blanche du poste d'origine.

Le bus ECU n'est pas accessible depuis les câbles arrivant au poste. Il faut aller se raccorder directement sur la prise OBD. 

A partir d'ici, le projet peut comporter deux variantes :

* Utilisation exclusive du bus multimédia
* Utilisation des deux bus (multimédia & ecu)

Le choix entre les deux variantes est relativement aisé :

* Si on ne discute qu'avec le bus MMU, on évite le risque d'envoyer des trames "erronnées" vers l'ECU. On reste focalisé sur le sujet "poste audio". C'est plus sûr, ça libère la prise OBD. Par contre, on s'interdit de lire des données qui ne sont pas dans la télémétrie d'origine.
* Si on discute avec MMU et ECU, on s'autorise à lire toutes les données moteurs, ce qui peut être très utile sur et hors circuit. Par contre, on prend le risque d'envoyer des trames parasites sur le CAN ECU et d'obtenir des messages d'erreur moteur.


**Pourquoi peut on avoir des messages d'erreurs voir d'arrêt d'urgence de la part de l'ordinateur de bord ?**

Le protocole CAN utilisé par Renault est relativement standard et à surtout comme particularité de ne pas prévoir ni sécurité ni handshaking des informations échangées. Cela veut dire que tout parasite sur le bus CAN (court-circuit, défaut de masse, trame erronnée envoyée par un système défaillant) est automatiquement interprété ou exécuté par l'ECU comme "légitime".

Dans mes multiples tests, et surtout erreurs, j'ai par exemple déclenché une alerte "bris de vitre imminent" :) J'ai aussi réussi à mettre tous les systèmes défaillants et le véhicule à l'arrêt.

Heureusement, lorsqu'on coupe le moteur, débranche le système défaillant ou parasite et qu'on redémarre, tout revient dans l'ordre. **Pourquoi ?** tout simplement parce que le système redémarre et renvoit toutes les trames de contrôles. Par défaut, si elles ne retournent pas à nouveau le "code défaut" alors le système considère qu'il a été "réparé".

**Attention au dongle OBD II Chinois** : On peut parfois être tenté d'acheter un dongle OBD II ultra plat, souvent d'origine chinoise, notamment aussi parce que le prix est très faible (moins de 15 euros). La qualité des composants est aléatoire. Vous pouvez provoquer les parasites dont je parle simplement en utilisant ce type de dongle. Si vous rencontrez des alertes en branchant ou en interrogeant votre dongle OBDII , posez vous la question de sa qualité.
Pour ma part, l'un de mes dongles provoquait une alarme "défaut ABS" systématiquement au démarrage du véhicule. 

## Les commandes au volant

Les commandes au volant (commodo) sont d'origines Renault voir assez courante dans les CLIO, Trafic ou autre véhicule d'entrée de gamme Renault.
Je ne mets pas de photo mais vous avez 6 boutons :

Haut Gauche.(src) -  Haut Droite.(tel+voix)

Volume (+)
Volume (-)

Bas Droite. (Ok)

Au dos, une molette permettant de passer à la musique suivante ou précédente.  


Les commandes au volant sont envoyés vers le poste via le BUS CAN MMU sous la forme d'une trame spécifique :

**Adr. D1.D2**

02D0 XX YY

Adr : Adresse de la trame. Cette adresse, unique, permet de reconnaitre qu'il s'agit des commandes au volant

D1 : Sur un octet, l'information code entre 0 et 255 la position de la molette au dos du commodo. Si vous tournez vers le haut, D1 = D1+1, si vous tournez vers le bas D1 = D1-1

D2 : Sur un octet, l'état des autres boutons avec le codage suivant :

  0: toutes les touches relachées

  1: Bouton du bas        

  2: Bouton Haut Droite       

  4: Bouton Haut Gauche

  8: Volume DOWN                  

 16: Volume UP

 24: MUTE
      
Les postes Android sont majoritairement d'origine Chinoise et utilisent une reconnaissance soit de type "série" soit de type impédance des commandes aux volants. 
Vous allez trouver à l'arrière du poste un faisceau dont deux fils vont attirer votre attention : KEY1 et KEY2. 
KEY1 et KEY2 sont interprétés par les postes de plusieurs manières :
1. KEY1 + KEY2 : En général, correspond à l'utilisation d'un protocole de liaison série ou KEY1=Tx (transmit) et LEY2=Rx (receive). Mais le génie des fabricants est de ne pas fournir de documentation sur le protocole, le débit a utiliser etc. 
2. KEY1 fournit une différence d'impédance (en ce cas KEY2 est "en l'air" ou sur GND) : Le poste détecte sur la ligne KEY1 les variations d'impédances et identifie la correspondance à une touche en fonction des niveaux.


La très grande majorité des postes fonctionnent ainsi même les postes occidentaux présentent rarement une option pour accèder directement au BUS CAN pour pouvoir ensuite rechercher/décoder les trames. Par ailleurs, chaque constructeur (Renault comme les autres) implémente son propre "langage", même si certaines trames sont standards via OBD, en direct, le constructeur a adapté à son besoin les échanges. Le poste devra donc être fourni avec un "programme" pour gérer le bus CAN du constructeur. 
C'est pourquoi vous voyez fleurir, notamment chez Alibaba, des boitiers adaptateurs dont l'objectif est d'interpréter les trames du BUS CAN, de reconnaitre celles représentant les commandes au volant et de fournir ensuite l'information transcrite au poste Android soit via Key1, Key1+Key2 ou directement sur une entrée CAN du poste.

On pourrait donc acheter l'un de ces boitiers pour reprendre les commandes au volant sur l'Alpine.

[Decodeur CANBUS pour Renault Mégane](https://www.alibaba.com/product-detail/TEYES-For-Renault-Megane-3-2008_1600226712858.html?spm=a2700.galleryofferlist.0.0.5ad74bc7SuaPpb)

Toute la difficulté est d'obtenir de la documentation sur les entrèes Key1, Key2 voir CAN des postes et sur le brochage (réel) du décodeur. Manifestement, il existe 3 ou 4 fabricants de poste Android. Aucun ne livre ni ne documente réellement ses postes et l'accès au support est impossible ou nullissime.

Après avoir testé une CANBOX pour CLIO et CAPTUR et un boitier similaire pour CLIO et TRAFIC, je suis arrivée à la conclusion que ne maîtrisant ni le brochage réel de chaque boitier ni la logique de connection sur le poste Android, qu'il faudrait des années de tests avant de trouver la bonne combinaison.
Inversement, un "fabricant" de boitier adaptateur finira par ajouter l'Alpine à son boitier.

J'ai donc décidé de créer ma propre **"CANBOX"** en m'appuyant sur un boitier Arduino et un relais CANBUS ayant pour objectif de "repérer" la fameuse trame 02D0 et d'envoyer le différentiel d'impédance vers la ligne KEY1 du poste.

## Le projet

* **Installer un poste Android à la place du poste Alpine**
* **Fabriquer un faisceau pour récupérer Alimentation et Haut Parleurs** 
* **Prendre en comte le micro intégré de l'Alpine** 
* **Raccordement sur le BUS CAN**
* **Raccordement de l'USB sur la console centrale**
* **Utilisation de l'Antenne Radio Alpine**
* **Utilisation de l'Antenne GPS**
* **Ajout d'une Antenne 4G**


Installer le poste Android : 
Le démontage n'est pas très compliqué bien qu'il faille, comme toujours, être attentif à ne pas forcer ni tirer trop fort sur les différentes parties.

**Accéder au poste:** Il faut retirer le cache, côté passager, qui remplace "la boite à gants". Pour cela, déclipser en tirant légèrement vers vous et en commençant par le bas et les côtés. Faites attention à ne pas tirer complètement vers vous car il y a 4 "pattes" qui "verrouillent" la planche de bord et pourraient être arrachées. Une fois déclipsé, déplacer la planche légèrement vers la droite et retirer vers vous. 
Je n'ai pas fait de photo de cette partie, si quelqu'un en a.

**Démontage du poste** : Il y a deux fois deux vis torx sur le côté du boitier. Il suffit de les dévisser et le boitier vient tout seul. 
Il faut ensuite retirer 1 à 1 les connecteurs. Sachant que chaque connecteur est clipsé. Ils viennent plus ou moins facilement en appuyant sur l'ergo de chaque connecteur. Ne forcez pas, ils sont parfois dure mais ils se retirent tous ainsi c'est l'appui sur l'ergo qui est parfois difficile surtout pour le connecteur Antenne Radio.




La console USB peut se raccorder très facilement en utilisant ce câble adaptateur : [Câble USB pour Suzuki BRZ](https://www.amazon.fr/gp/product/B07HMT5H5D/ref=ppx_yo_dt_b_asin_title_o02_s00?ie=UTF8&psc=1)





