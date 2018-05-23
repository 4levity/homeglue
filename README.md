# Homeglue version 0 #

Discover and connect to Belkin WeMo Insight plug power meters and retrieve data from them. 
Also identifies other UPnP devices on the LAN, in order to monitor whether they are on or off.

#### Compatibility ####

Known working with:

* Belkin WeMo Insight wifi plug meter
  * **WeMo_WW_2.00.11057.PVT-OWRT-Insight** (hardware version 1)
  * **WeMo_WW_2.00.11057.PVT-OWRT-InsightV2** (hardware version 2)

#### Use ####

Java 8 currently required. The command `java -version` should say something something version 1.8.x. 

To build and test locally, run this command from project folder (skip the `./` on MS Windows):

    ./gradlew build

Then to run the application:

    java -jar build/libs/homeglue-0.1.jar

#### Future plans ####

* more tests 
  * Integration test with device managers on full simulated network
  * application Main DI and startup
  * WeMo UPnP port number changes after detection
  * IOExceptions at various points
  * malformed XML at various points
* store data (local H2, csv, AWS DynamoDB or other free services)
* handle read errors/offline devices
* support turning switch on/off
* automatically turn off my coffee maker if left on
* auto versioning and publishing from Travis CI
* update to Java 10 (after next Lombok release)
* support other home metering and automation devices
* user interface
* style and static analysis checks
