[![paypal](https://www.paypalobjects.com/fr_FR/i/btn/btn_donateCC_LG.gif)](https://paypal.me/myalpdroid?country.x=FR&locale.x=fr_FR)

<H1> Alpine : Remplacer le poste multimédia  par un poste Android </H1>

Le poste multimédia Alpine présente 2 inconvénients : il n'est pas compatible Android Auto/Apple CarPlay. Il a perdu au fur et à mesure les apps intéressantes comme Spotify via MySpin. On pourrait ajouter que l'écran 7 pouces peut sembler un peu petit.

Il présente aussi comme inconvénient qu'Alpine a manifestement décidé qu'il n'y aurait pas d'upgrade de celui-ci. Ceci étant d'autant plus dommage qu'il s'agit du même poste Multimédia **Bosch** que celui qu'on retrouve dans la Suzuki Swift 2014 et que l'écran **Valeo** est le même que celui du Renault Kadjar 2016.
Pour ces deux modèles, des upgrades ont été livrés pour faire bénéficier d'Android & Apple leurs utilisateurs. 
Bon, après, on parle de véhicules très haut de gamme (la swift et le Kadjar 2016), normal qu'il y ait un peu d'animation en après vente...

Concernant l'Alpine, il est assez complexe de travailler sur une carte qui aurait pour objet d'intercepter le signal vidéo de l'Alpine pour permettre de switcher avec un adaptateur Android Auto ou CarPlay. Après avoir longuement étudié la question, la solution a semblé relativement couteuse et longue à produire. 

L'autre solution pourrait consister à adapter un poste Android avec l'écran de l'Alpine. On verra plus loin qu'il faudrait redévelopper une carte affichage et ajouter un contrôleur CAN pour réussir l'opération. C'est possible. Un peu complexe mais possible.

**La solution proposée ici** est donc d'intégrer un poste multimédia Android dernière génération à la place de l'ancien poste et de remplacer l'écran 7 pouces par l'écran de 8 à 11 pouces livré avec le poste Android.

Pour ma part, j'ai choisi ce poste de chez Joying :
[Joying Auto 10.1' Android 10](https://www.joyingauto.eu/joying-android-10-0-autoradio-10-1-inch-1280-800-screen-single-1din-car-stereo.html)

Tout l'enjeu est maintenant de faire fonctionner ce poste en lieu est place du "formidable" duo Valeo Bosch de l'Alpine A110.

Mais en premier, ce projet a pour objet de partager toute l'expérience et de permettre à ceux qui le souhaitent de faire la même chose et de contribuer à l'amélioration de la solution.

**Veuillez noter qu'il s'agit d'un projet expérimental, qu'il peut y avoir des dysfonctionnements dans les composants comme dans les programmes pouvant entrainer alarmes ou arrêt du véhicule pour mise en sécurité. Vous utilisez les résultats de ce projet à vos propres risques et périls, l'auteur n'assurera aucun support ou garantie suite à l'utilisation des programmes et informations issues de ce projet.**

Pour plus d'information, Consulter le Wiki :[ici](https://github.com/MyAlpDroid/AlpDroid/wiki)

Si ce projet vous a fait gagner du temps : [![paypal](https://www.paypalobjects.com/fr_FR/i/btn/btn_donateCC_LG.gif)](https://paypal.me/myalpdroid?country.x=FR&locale.x=fr_FR)

Le projet s'inspire et utilise les résultats des projets suivants :

* Library Arduino et Serial USB : [ici](https://github.com/OmarAflak/Arduino-Library) qui s'appuie aussi sur Libray SerialUSB :[ici](https://github.com/felHR85/UsbSerial)
* Article et exemple de développement autour du CD4066 (multiplexeur à 2 voies) : J'ai perdu la référence, j'en suis désolé, je la remettrai si l'auteur se reconnait ou si je la retrouve entretemps.
* L'excellente Library CANBUS pour Arduino autowp : [ici](https://github.com/autowp/arduino-mcp2515)
* Exemple très bien documenté de pilotage d'une Mercedes et réingéniering des trames CAN : [ici](https://github.com/rnd-ash/W203-canbus)
* Library SpeedView pour les compteurs : [ici](https://github.com/anastr/SpeedView)
* Les services de reconnaissance de musique et service media de Scroball : [ici](https://github.com/peterjosling/scroball)
