# Homeglue version 0 #

Discover and connect to Belkin WeMo Insight plug power meters and retrieve data from them. 
Also identifies other UPnP devices on the LAN, in order to monitor whether they are on or off.
Integrates with IFTTT, and possibly other free cloud services in the future.

The software is currently capable of detecting when my coffee maker has been left on and turning
it off automatically, while notifying me via IFTTT.

See future development plans below. Issues and pull requests are welcome via GitHub. 

Homeglue home page - https://github.com/4levity/homeglue

Homeglue builds on Travis CI - https://travis-ci.com/4levity/homeglue

### Compatibility ###

Known working:

* Communicates with Belkin WeMo Insight wifi plug meter
  * **WeMo_WW_2.00.11057.PVT-OWRT-Insight** (hardware version 1)
  * **WeMo_WW_2.00.11057.PVT-OWRT-InsightV2** (hardware version 2)

* Runs on Raspberry Pi (any model) or any other Linux/OS X/Windows PC with Java 8 and 256 MB RAM

* Works with IFTTT Maker Webhooks to report device status (more to come)

### Build + Use ###

Java 8 currently required. The command `java -version` should say something something version 1.8.x. 

To build and test locally, run this command from project folder (skip the `./` on MS Windows):

    ./gradlew build shadowJar

Then to run the application:

    java -jar build/libs/homeglue-0.1-all.jar

To enable sending to [IFTTT Maker Webhooks](https://ifttt.com/maker_webhooks), create or edit a file called 
`homeglue.properties` in the project root which contains a line similar to 
this (use your own Maker Webhooks key):

    ifttt.webhooks.key=c1eN9MOy2kPSgJ98iIzcie

### Disclaimers ###

This product should not be assumed to be fit for any purpose. Even though it 
has pretty decent automated test coverage, you can't hold me responsible if, 
for example, it fails to turn off your coffeemaker and subsequently your house
burns down. You should have bought a better coffee maker.

The terms "Belkin", "WeMo" and "WeMo Insight" are presumably trademarks of and otherwise the
property of Belkin. These terms are used only to refer to those neat-o
products. 4Levity and Homeglue is not in any way affiliated with Belkin.

### Future plans ###

* support more devices - ESP8266 generic, X10, LIFX ?
* better detectionId for generic UPnP + expose service info
* detect non-UPnP devices (detect phone on wifi etc)
* more tests 
  * commands, appliance detector, friendly name, offline marker ..
  * IOExceptions at various points
  * malformed XML at various points
* show recent telemetry data
* IFTTT incoming webhooks for device controls
* store raw telemetry data (local SQL db, CSV file, Google Sheets, AWS DynamoDB or other free cloud db)
* split up logic in DeviceStateProcessorService
* auto versioning (major/branch/build)
* user interface, configuration in database
* publish packaged binary from Travis CI
* support other home metering and automation devices
* style and static analysis checks
* consider updating to Java 9 or 10
