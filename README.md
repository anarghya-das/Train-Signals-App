# Train-Signals-App
Indian Railways has a huge system of trains running throughout the country on a daily basis. For any large system, the safety of their workers and passengers is of utmost importance and unusual weather conditions like fog or heavy rainfall restrict the view of the train driver and hamper their decision making, in turn, putting their lives and the passengers live in danger.

Many people might not consider the mere fog as a danger to trains, but throughout the years it has been one of the deadliest natural phenomena which were a cause of a lot of accidents. 
From 2003 to 2015, 239 accidents have happened on the network of the Indian Railways. The major cause of these accidents is the damage to the railway lines and lack of visibility due to foggy winter conditions. Because of these accidents, railway authorities are taking this problem of visibility during foggy winter conditions seriously. Last year during the winter, the Indian Railways canceled 24 trains and rescheduled 23 trains all due to fog. 

As this problem of poor visibility was growing, the Indian Railways themselves proposed to fight this problem by introducing a new global positioning system (GPS) based device called FOG PASS which will display the name and distance of the approaching landmark in advance. This device is still on trial and is being tested continuously. Apart from this, there has been work put into this problem and gradually people are coming up with different solutions. The Indian Institute of Technology (IIT) came up with an anti-fog device which when installed on the train cameras will remove any fog from the footage received by the driver and give them a clear picture of the surroundings in front of them. Additionally, Indian Railways also proposed a Train Protection and Warning System (TPWS) which will be an automatic train controlling system. It will reduce the speed of the train automatically during poor visibility conditions even if the driver fails to do so.

All these new ideas to tackle the growing problem of visibility due to fog is definitely a step in the right direction but we are still in the stage of execution and testing because none of these solutions provide a long-term, feasible and a reliable solution. The FOG PASS device created by the Indian Railways has limitations of its own as it is unable to inform the driver about the aspect of the next signal the train is approaching and the anti-fog device developed by IIT has a cost of Rs 56 lakh per device so that idea is definitely not economic right now. The TPWS has the same problem as it is proposed to support only around 3330 km of railway track for a price of Rs 2000 crore. As of 2011, the Indian Railway has a total track length of 87,040 km. So, to cover the whole track with TPWS will take ages and fortunes worth of money to complete.  

Two of the main shortcomings of the ideas already proposed are price and the lack of information being provided to the drivers. This idea called the Fog Signal is an app which proposes to address these two main shortcomings of its predecessors. An app was the best fit to use for this problem as everyone carries a smartphone these days making the cost of implementation virtually nothing. Also, in the past android apps have helped deal with various difficult situations such as safety of women. Nowadays mobile apps are used to solve all kinds of problems like for transportation apps like OLA, Uber are household names. Ordering food online can be done efficiently too using apps like Food Panda and Swiggy. Mobile applications are a global phenomenon and people are developing new mobile apps constantly to solve every small or big problem around them efficiently. Every train driver will have the app on their own mobile devices. The Fog Signal app also addresses the problem which other solutions can not- Providing more information to the train driver. Not only it shows the driver the aspect color of the next three signals but it also shows a map view which provides drivers with the information regarding their current location and location of the next three signals in front of them. Smartphones are the future as all the problem’s solution could be found right at our fingertips making life even simpler.
# The Idea
The app which is primarily designed for train drivers uses an existing service called the TMS service which provides data about the trains and the next three signals in front of the trains. This app will help the drivers while they are running the train by notifying them about the aspect of the next three signals ahead of them. This app also features a map view where the drivers can see their current location and the location and aspects of the next three signals in front of them on the map.
# Objectives
1.	To Inform the train drivers about the next signal in front of them using voice and audio notification.
2.	To Show the train drivers their current location and the location of upcoming signals.
3.	No implementation cost as everyone owns a smartphone these days.
# Basic Architecture 
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure1.png)<br>
*Figure 1: Basic Architecture Diagram*<br><br>
<b>TMS Server</b>: The main server on which the mobile application works. It is responsible to provide the train information in the form of JSON. The server updates the JSON data about every 5-10 millisecond. 
```JSON
[{
	"direction":"String content",
	"location":"String content",
	"locoNo":"String content",
	"trackId":2147483647,
	"trackName":"String content",
	"trainId":9223372036854775807,
	"trainName":"String content",
	"trainNo":2147483647,
	"updatedAt":"String content",
	"zSignals":[{
		"distance":1.26743233E+15,
		"index":2147483647,
		"station":"String content",
		"trackName":"String content",
		"ztoAspectSignal":{
			"objectName":"String content",
			"objectType":"String content",
			"relays":[{
				"channelDescription":"String content",
				"currentStatus":"String content"
			}]
		},
		"ztoCallingOnSignal":{
			"objectName":"String content",
			"objectType":"String content",
			"relays":[{
				"channelDescription":"String content",
				"currentStatus":"String content"
			}]
		},
		"ztoShuntSignal":{
			"objectName":"String content",
			"objectType":"String content",
			"relays":[{
				"channelDescription":"String content",
				"currentStatus":"String content"
			}]
		}
	}]
}]                                     
```
*Figure 2: Sample TMS Server JSON data* <br>
<br>
The text shown in Figure 2 is a sample JSON data sent by the TMS server. Direction, Track Name, Train ID, Train Name, Train Number are the information extracted from the JSON string provided by the TMS Server. Train objects are created using this information. zSignals is used to get information about the signals in front of the train. Index, track name, and relays inside the zToAspectSignal object is used to create signal objects for the respective trains.<br>
The color of the signals is decoded using the following chart:<br>

| Signal Key  | Signal Color Value |
| ------------- | ------------- |
| "RGKE" | Red  |
| "HGKE"  | Yellow  |
| "HGKE" and "HGKE" | YellowYellow  |
| "DGKE"  | Green  |

<b>Open Street Map API</b>: The open source API which provides the applications with the map interface.<br>
<b>Mobile Application</b>: Sends and receives the HTTP Requests and performs all the processes necessary for the working of the application.<br>
<b>Web GIS Application</b>: A central application which keeps a track of all the active mobile applications by plotting their locations on the map while getting the data from the database server.<br>
<b>Database Server</b>: A SQL server which provides additional data for both the applications.<br>
# Use Case Diagaram 
<b>Train Driver</b>: The primary actor of the Use Case diagram who interacts with the Main Screen, Signal View and Map View of the app. The app was primarily designed for the primary actor, Train Driver.

<b>TMS</b>: The secondary actor of the Use Case diagram which provides the app with the train data from which the required information is fetched from the app.

**Open Street Map**: Another secondary actor of the Use Case diagram which provides the app with the map data from which the map is created in the Map View of the app.

**Main Screen**: The first module with which the primary actor (train driver) interacts with as soon as the app is opened. It has an "include" relationship with **Connectivity Check** and **Train Data** which means that every time it starts the connectivity check for the internet connection is bound to happen and it will also fetch the train data from the TMS server automatically. It also has an "extend" relationship with **Display Error** which means that it will not be shown every time the Main Screen module starts but only under certain conditions (when connectivity check fails or Train Driver enters incorrect details).

**Signal View**: The second module with which the primary actor (train driver) can interact after the Main Screen module. It has an "include" relationship with **Train Data** which means that every time this module starts the app will fetch the train data from the TMS server automatically. It also has an "extend" relationship with **Connection Lost Error** which means that it will not be shown every time this module starts but only under certain conditions (when
internet connection is lost).

**Map View**: The third module with which the primary actor (train driver) can interact after the **Signal View**. It has an "include" relationship with **Train Data** and **Load Map Data** which means that every time this module starts the app will fetch the train data from the TMS server and map data from the Open Street Map API automatically. It also has an "extend" relationship with **Connection Lost Error** which means that it will not be shown every time this module starts but only under certain conditions (when internet connection is lost).

![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure3.png)<br>
*Figure 3: Use Case Diagram for Fog Signal App*<br><br>
# Mobile Application Design
The app design was divided into three modules: MainScreenActivity which displays the basic information of the train to be entered by the driver, SignalActivity which displays the next three signals with their aspect color and the MapActivity which displays a map view where the next three signals are plotted along with the current user location.
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure4.png)<br>
*Figure 4: Mobile Application Diagram*<br><br>
1.	**Background Process**: This is a process which takes place in the background while the app is running. There are three main jobs that this process handles:<br>
a.	 To create HTTP connections to the server and receive the response from the server. <br>
b.	To update the UI accordingly after it has received a response from the server. <br>
c.	To check the network connectivity of the application.<br>
This mobile application uses the HTTP POST method to connect to the TMS Server. In this type of connection, data is usually required to be sent to the server and then a response is received accordingly. It also uses HTTP GET method to connect to the database server. In this type of connection, data is not required to be sent to the server and response if received accordingly.

2.	**MainScreenActivity Module**
As soon as this activity starts, an HTTP request is sent to the TMS server in the **background process**. The response which is a JSON string containing all the train information with their signal is received and parsed into train objects and signal objects. The parsed train objects are then sent to the MainScreenActivity module which populates the user input dropdowns using the **createTrainNameInput, createTrainNumberInput, createTrackNameInput** functions. When all the inputs are populated the **startApplication** function is created to validate all the inputs by the user and to proceed the application to the next screen. This module also checks whether the gangman is connected to the internet via mobile data or WiFi using the connectivityCheck function.

3.	**SignalActivity Module** 
The user input data is transferred to this module. The volume of the phone is set to maximum using the **increaseVolumeToMax** function, the display timeout of the phone is turned off while on this module and HTTP requests are made every 5ms to the TMS server using the **background process** (as new data from the server is updated at every 5-10 millisecond) and the JSON data received is parsed into train objects. Out of all the train objects, the train object corresponding to the train name entered by the user is selected and the Signal view is updated using the signals of that train object using the **createSignal** method.

4.	**MapActivity Module**
The user input data is transferred from the SignalActivity module to this module. The map is initialized by the current user location by **createMap** method which uses the Open Street Map API to generate the map. The volume of the phone is set to maximum using the **increaseVolumeToMax** function, the display timeout of the phone is turned off while on this module and HTTP requests are made every 5ms to the TMS server using the **background process** (as new data from the server is updated at every 5-10 millisecond) and the JSON data received is parsed into train objects. Another HTTP GET request is made to the database server which sends a JSON data consisting of Signal IDs and their geo coordinates. The JSON data received by the SQL data server is parsed and signal objects are created and mapped with their coordinates. Those signal objects are converted into map markers by the **populateMarkers** function. The initial signals are plotted to the map using the **addSignalsToMap** function and the corres
ponding signal markers are updated using the **updateSignalMap** function.
# Database Server Desgin
The database server consists of two SQL tables: Signals Table and Train Information Table. The signals table stores the signal id and geo-coordinates of each signal while the Train Information Table stores the user entered information along with the current user location and active/inactive status.<br>
**Signals Table Structure**:<br>

|Signal ID|	Latitude| 	Longitude|
| ------------- | ------------- |------------- |
|Signal1 	|Signal1_Latitude|	Signal1_Longitude|

**Train Information Table Structure**:<br>

|Device ID|	Train Name|	Train Number|	Track Name|	Phone Number|	User Latitude|	User Longitude|	Status|
| ------------- | ------------- |------------- | ------------- | ------------- |------------- |------------- |------------- |
|Id1		|Train1		|trainNo1	|Track1		|Number1	|Id1_latitude|	Id1_longitude|	active/inactive|

The communication with the app and the database server is also divided into two modules: **MainScreenActivity module and Signal & Map View module.**
1.	**MainScreenActivity Module**

As soon as the start button is pressed in the MainScreenActivity, HTTP POST request with the user entered information as a JSON string is sent to the database server using the **background process**. The JSON string is then processed to check if there is a duplicate entry in the Train Information Table for that particular Train ID. If no duplicate entry is found the entry is added to the table and a positive response is sent to the app. On receiving the positive response, the app moves forward to the next screen. If there is a duplicate entry for the same train ID, then a negative response is sent to the app and an error message is displayed accordingly.

2.	**Signal & Map View module**

During the time the app is running, these modules constantly send the user entered information to the database server with the status as "active" using **background process**. This is done to ensure that the device which is using the particular train ID is flagged as active and no other device can use the app with the train ID currently in use. In the MapActivity, additional to the user entered data, the current user coordinates are also sent to the server. When the app is closed, one last piece of data is sent to the server using **background process** but this time with status as "inactive" to tell the database that the particular train ID is no longer in use.
# Development
The app was developed on android because android devices are most popular in India and because of it is open source there was the low cost of development. The IDE used in the development of this app was Android Studio.

**Async Task** was used as a **background process** because it fulfills all the three objectives required from the **background process**. Async Task has a function called **doInBackground** where the HTTP requests are made and the adequate response is then transferred to another method inside the **Async Task** called the **onPostExecute** which updates the UI based on the response.

**Handler** and **Timer** were used in combination with the **Async Task** to make the HTTP requests to the server in every 5 milliseconds. The 5 millisecond time was chosen because of the TMS server updates with new data every 5-10 milliseconds, therefore to avoid losing any data the minimum interval of 5 milliseconds was chosen. The mobile application uses voice notifications to inform the user about the color of the next signal which also works when the phone's screen is off or the app is running in the background. This was also done with the help of **Async Task**.

This project also has a few dependencies namely a [JSON library](https://github.com/ralfstx/minimal-json) which is used to parse the JSON responses received by the app, Open Street Map library used to generate the map view of the app and Android support design libraries to bring the material look and feel to the app.
# Usage
As soon as the app starts, the data is fetched from the server and the train driver is greeted with a welcome screen. There the user is required to enter the information of the train or select it from the drop-down list along with their phone number. After all the fields are filled the train driver can click on the enter button and proceed to the next screen of the app. Figure 5 shows the main screen state after all the inputs are properly filled by the train driver.<br> 
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure5.png)<br>
*Figure 5: Screenshot of Main Screen*<br><br>
The next screen of the app shows the signals view (Figure 6). It has the information about the train which the train driver just entered and the aspect colors of the next three signals. These aspect colors of the signals update in real time as the signals get updated in the railway track using the data fetched from the TMS server. This screen also has an audio notification for the drivers that inform them about the next signal in front of the train. The repeat frequency of the audio notification can be set by the train driver also the language of the audio notification can be cycled between English, Hindi, and Bengali. The audio notification can be muted by the train driver.<br>
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure6.png)<br>
*Figure 6: Screenshot of Signal View*<br><br>
The third screen of the app displays the map view (Figure 7). In this screen, the user location is displayed (train icon) along with the location of the next three signal posts with their aspect colors on the app. The location of the train driver updates in real time as the train moves and the signal posts also get updates as soon as their color changes. This screen also has an audio notification for the drivers that inform them about the next signal in front of the train. The repeat frequency of the audio notification can be set by the train driver also the language of the audio notification can be cycled between English, Hindi, and Bengali. The audio notification can be muted by the train driver.<br>
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure7.png)<br>
*Figure 7: Screenshot of Map View*<br><br>
If the app is started without turning on mobile data or WiFi, there will be an error dialog box displayed as shown in Figure 8.<br>
![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure8.png)<br>
*Figure 8: Connectivity Error (Main Screen)*<br><br>
If the internet connection is lost or there is a server error while the app is running an error dialog (as shown in Figure 9) will be displayed on the screen. The app will try to reconnect automatically for a period of 1 minute but if it fails to reconnect another error dialog will be displayed (as shown in Figure 10).<br>

![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure9.png)  | ![alt text](https://github.com/anarghya-das/Train-Signals-App/blob/master/Images/figure10.png)
:-------------------------:|:-------------------------:
*Figure 9: Connection Error Dialog*|*Figure 10: Failed to Reconnect Dialog*
# Testing
The app has been tested for incorrect inputs and during slow network conditions. All the exceptions are handled and proper user dialog is provided based on each dialog. As of now, the app has been tested in the following conditions:<br> 

|INPUT|	OUTPUT|
|-----|-------|
|The app started with no Wifi or network connection |	Dialog box displayed which tells the user to turn on the Wifi or network connection in order to use the app.|
|Pressing the start button with invalid inputs in the Main Screen|	Displays an appropriate message for the wrong inputs|
|Pressing the start button with valid inputs in the Main Screen|	App switches to the Signal Screen and starts all the described processes |
|Network connection turned off while the app is running| Displays a network connectivity lost dialog and then tries to reconnect for one minute and if it still fails it gives the user an option to exit or restart the app| 
|Bottom navigation bar|	Selected icons switch the activity to the desired sections of the app|
|Mute, Change audio language, frequency bar|	On click, these buttons do the desired job|

The app is compatible in devices with Android SDK versions 16 and above (Android 4.1 aka Jelly Bean). The app has been tested both in the lowest SDK- Jelly Bean and the latest SDK- Android P and no problems were encountered. This shows that the app should be compatible with virtually any Android device in the market.
The app has also been tested under various network speeds, details of which are mentioned below:<br>

|Network|	Initial Loading Time| 	Map Loading Time|
|-------|---------------------------|-------------------|
|3G	|5.204 milliseconds|	11.123 milliseconds|
|4G| 	1.079 milliseconds|	3.453 milliseconds|
# Conclusion
Low visibility due to foggy weather is still a major problem in India. We know the seriousness of this problem is increasing day by as it was also discussed in the Lok Sabha as an important issue. But till now no one has come up with an economic solution that could be used in our day to day life. As efforts are being made to solve this problem which proves that we are going in the right direction in dealing with this problem and this app is another small step to contribute to the effort of providing drivers with clear information during the times of fog. This app has the potential to overcome the barrier that the drivers face currently with all the features that the train drivers might need to avoid a devastating accident. Like every solution, this app has some limitations as it won’t work if the TMS server or the database server encounters a problem and it won’t work when there is a network problem at the user end. Apart from the fact that this app has not yet been tested in a real-world environment, it shows promising results in the initial tests and hopefully one day it’ll become the solution to the ever haunting problem of visibility during fog. 
