# Homeglue version 0 #

Discover and connect to Belkin WeMo Insight plug power meters and retrieve data from them. 
Also identifies other UPnP devices on the LAN, in order to monitor whether they are on or off.

See future development plans below. Issues and pull requests are welcome via GitHub. 

Homeglue home page - https://github.com/4levity/homeglue

Homeglue builds on Travis CI - https://travis-ci.com/4levity/homeglue

### Compatibility ###

Known working:

* Communicates with Belkin WeMo Insight wifi plug meter
  * **WeMo_WW_2.00.11057.PVT-OWRT-Insight** (hardware version 1)
  * **WeMo_WW_2.00.11057.PVT-OWRT-InsightV2** (hardware version 2)

* Runs on Raspberry Pi (any model) or any other Linux/OS X/Windows PC with Java 8 and 256 MB RAM

* Works with IFTTT Maker Webhooks

### Build + Use ###

Java 8 currently required. The command `java -version` should say something something version 1.8.x. 

To build and test locally, run this command from project folder (skip the `./` on MS Windows):

    ./gradlew build

Then to run the application:

    java -jar build/libs/homeglue-0.1.jar

To enable sending to [IFTTT Maker Webhooks](https://ifttt.com/maker_webhooks), create or edit a file called 
`homeglue.properties` in the project root which contains a line similar to 
this (use your own Maker Webhooks key):

    ifttt.webhooks.key=c1eN9MOy2kPSgJ98iIzcie

### Disclaimers ###

This product should not be assumed to be fit for any purpose. Even though it 
has pretty decent automated test coverage, you can't hold me responsible if, 
for example, it fails to turn off your coffeemaker and subsequently your house
burns down. You should have bought a better coffee maker.

Belkin, WeMo and WeMo Insight are presumably trademarks of and otherwise the
property of Belkin. These terms are used only to refer to those neat-o
products. 4Levity and Homeglue is not in any way affiliated with Belkin.

### Future plans ###

* more tests 
  * IOExceptions at various points
  * malformed XML at various points
* store data (local SQL db, CSV file, AWS DynamoDB or other free cloud db)
* handle read errors/offline devices
* support turning switch on/off
* automatically turn off my coffee maker\* if left on\*\*
* auto versioning (major/branch/build)
* user interface
* publish packaged app from Travis CI
* support other home metering and automation devices
* style and static analysis checks
* consider updating to Java 9 or 10

\* - Pretty much why I wrote all this in the first place

\*\* - The Insight switch isn't smart enough to tell on its own, since the
heating element cycles off for a few minutes at a time