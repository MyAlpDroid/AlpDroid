[![](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/MyAlpDroid)
[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://paypal.me/myalpdroid?country.x=FR&locale.x=fr_FR)

[Français](https://github.com/MyAlpDroid/AlpDroid/README.md)    [English](https://github.com/MyAlpDroid/AlpDroid/README.en.md)

<H1> Alpine: Replace the multimedia head unit with an Android station</H1>

The Alpine multimedia head unit has 2 drawbacks: it is not Android Auto/Apple CarPlay compatible. It gradually lost interesting apps like Spotify via MySpin. One might add that the 7-inch screen might seem a bit small.

It also has the disadvantage that Alpine has obviously decided that there will be no upgrade for it. This being all the more unfortunate since it is the same **Bosch** Multimedia station as the one found in the Suzuki Swift 2014 and the **Valeo** screen is the same as that of the Renault Kadjar 2016.
For these two models, upgrades have been delivered to allow their users to benefit from Android & Apple.
Well, after that, we are talking about very high-end vehicles (the swift and the Kadjar 2016), it is normal that there are a lot of after-sales activities...

In short, what we simply want is to have this:

![Avoir des vrais apps](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/Spotify-Coyote.jpg)

![Garder la télémtrie](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/Te%CC%81le%CC%81me%CC%81trie.jpg)

![Avoir Apple Carplay ou Android Auto](https://github.com/MyAlpDroid/AlpDroid/blob/main/Pictures/AppleCarPlay.jpg)

Concerning the Alpine's Head Unit, it is quite complex to work on a card which would have the purpose of intercepting the video signal of the Head Unit to allow switching with an Android Auto or CarPlay adapter. After having studied the question for a long time, the solution seemed relatively expensive and time-consuming to produce.

The other solution could be to adapt an Android station with the Alpine screen. We will see later that it would be necessary to redevelop a display card and add a CAN controller to make the operation a success. It's possible. A bit complex but possible.

**The solution proposed here** is therefore to integrate a latest generation Android multimedia Head Unit in place of the old OEM Head Unit and to replace the 7-inch screen with the 8 to 11-inch screen delivered with the Android Head Unit.

For my part, I chose this position at Joying:
[Joying Auto 10.1' Android 10](https://www.joyingauto.eu/joying-android-10-0-autoradio-10-1-inch-1280-800-screen-single-1din-car-stereo.html)

The challenge now is to make this station work instead of the "amazing" Valeo-Bosch duo of the Alpine A110.

But first of all, this project aims to share all the experience and allow those who wish to do the same and contribute to improving the solution.

**Please note that this is an experimental project, that there may be malfunctions in the components as well as in the programs which can lead to alarms or stopping the vehicle for safety. You use the results of this project at your own risk and peril, the author will not provide any support or guarantee following the use of the programs and information resulting from this project.**

For more information, see the Wiki:[here](https://github.com/MyAlpDroid/AlpDroid/wiki)

Don't forget to sponsor me:

[![](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/MyAlpDroid)

The project uses the results of the following projects:

* Library Arduino and Serial USB : [here](https://github.com/OmarAflak/Arduino-Library) based on felHR85 SerialUSB Library :[here](https://github.com/felHR85/UsbSerial)
* Article and example of development around the CD4066 (2-way multiplexer): I lost the reference, I'm sorry, I'll put it back if the author recognizes himself or if I find it in the meantime.
* L'excellente Library CANBUS pour Arduino autowp : [here](https://github.com/autowp/arduino-mcp2515)
* Very well documented example of driving a Mercedes and re-engineering CAN frames : [here](https://github.com/rnd-ash/W203-canbus)
* Counters Library SpeedView : [here](https://github.com/anastr/SpeedView)
* Scroball's music recognition and media services: [here](https://github.com/peterjosling/scroball)
* The APIs of the OsmAND application for navigation directives to the dashboard: [here](https://github.com/osmandapp/osmand-api-demo)
