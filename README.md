# Train-Signal-App
Indian Railways has a huge system of trains running throughout the country on a daily basis. For any large system, the safety of their workers and passengers is of utmost importance and unusual weather conditions like fog or heavy rainfall restrict the view of the train driver and hamper their decision making, in turn, putting their lives and the passengers live in danger.

Many people might not consider the mere fog as a danger to trains, but throughout the years it has been one of the deadliest natural phenomena which were a cause of a lot of accidents. 
“From 2003 to 2015, 239 accidents have happened on the network of the Indian Railways” (“239 Train Accidents”, 2016). The major cause of these accidents is the damage to the railway lines and lack of visibility due to foggy winter conditions. Because of these accidents, railway authorities are taking this problem of visibility during foggy winter conditions seriously. Last year during the winter, the Indian Railways canceled 24 trains and rescheduled 23 trains all due to fog (“Indian Railways”, 2018). 

As this problem of poor visibility was growing, the Indian Railways themselves proposed to fight this problem by introducing a new global positioning system (GPS) based device called FOG PASS which will display the name and distance of the approaching landmark in advance. This device is still on trial and is being tested continuously (“Safety Measures”, 2016). Apart from this, there has been work put into this problem and gradually people are coming up with different solutions. The Indian Institute of Technology (IIT) came up with an anti-fog device which when installed on the train cameras will remove any fog from the footage received by the driver and give them a clear picture of the surroundings in front of them (Dikshit, 2016). Additionally, Indian Railways also proposed a Train Protection and Warning System (TPWS) which will be an automatic train controlling system. It will reduce the speed of the train automatically during poor visibility conditions even if the driver fails to do so (“Advance Safety System”, 2017).

All these new ideas to tackle the growing problem of visibility due to fog is definitely a step in the right direction but we are still in the stage of execution and testing because none of these solutions provide a long-term, feasible and a reliable solution. The FOG PASS device created by the Indian Railways has limitations of its own as it is unable to inform the driver about the aspect of the next signal the train is approaching and the anti-fog device developed by IIT has a cost of Rs 56 lakh per device so that idea is definitely not economic right now. The TPWS has the same problem as it is proposed to support only around 3330 km of railway track for a price of Rs 2000 crore. As of 2011, the Indian Railway has a total track length of 87,040 km (“Current Route Length”). So, to cover the whole track with TPWS will take ages and fortunes worth of money to complete.  

Two of the main shortcomings of the ideas already proposed are price and the lack of information being provided to the drivers. This idea called the Fog Signal is an app which proposes to address these two main shortcomings of its predecessors. An app was the best fit to use for this problem as everyone carries a smartphone these days making the cost of implementation virtually nothing. Also, in the past android apps have helped deal with various difficult situations such as safety of women (“7 Best Women Safety”). Nowadays mobile apps are used to solve all kinds of problems like for transportation apps like OLA, Uber are household names. Ordering food online can be done efficiently too using apps like Food Panda and Swiggy. Mobile applications are a global phenomenon and people are developing new mobile apps constantly to solve every small or big problem around them efficiently (Adeleye, 2016). Every train driver will have the app on their own mobile devices. The Fog Signal app also addresses the problem which other solutions can not- Providing more information to the train driver. Not only it shows the driver the aspect color of the next three signals but it also shows a map view which provides drivers with the information regarding their current location and location of the next three signals in front of them. Smartphones are the future as all the problem’s solution could be found right at our fingertips making life even simpler.
# The Idea
The app which is primarily designed for train drivers uses an existing service called the TMS service which provides data about the trains and the next three signals in front of the trains. This app will help the drivers while they are running the train by notifying them about the aspect of the next three signals ahead of them. This app also features a map view where the drivers can see their current location and the location and aspects of the next three signals in front of them on the map.
# Objectives
1.	To Inform the train drivers about the next signal in front of them using voice and audio notification.
2.	To Show the train drivers their current location and the location of upcoming signals.
3.	No implementation cost as everyone owns a smartphone these days.
# Basic Architecture 
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
                                      Figure 2: Sample TMS Server JSON data 
```
The text shown in Figure 2 is a sample JSON data sent by the TMS server. Direction, Track Name, Train ID, Train Name, Train Number are the information extracted from the JSON string provided by the TMS Server. Train objects are created using this information. zSignals is used to get information about the signals in front of the train. Index, track name, and relays inside the zToAspectSignal object is used to create signal objects for the respective trains.<br>
The color of the signals is decoded using the following chart:<br>
| First Header  | Second Header |
| ------------- | ------------- |
| Content Cell  | Content Cell  |
| Content Cell  | Content Cell  |

<b>Open Street Map API</b>: The open source API which provides the applications with the map interface.<br>
<b>Mobile Application</b>: Sends and receives the HTTP Requests and performs all the processes necessary for the working of the application.<br>
<b>Web GIS Application</b>: A central application which keeps a track of all the active mobile applications by plotting their locations on the map while getting the data from the database server.<br>
<b>Database Server</b>: A SQL server which provides additional data for both the applications.<br>
