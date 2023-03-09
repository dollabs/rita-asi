# Encoded Pre-trial Maps File

### About
The file includes basic information (fileName, trialID, participantID) for each pre-trial map. The user needs to interpret and manually add in the player's plan. 


Each entry in the output file looks somewhat like this:
<pre>
`{ :fileName "
," 
  :trialID 77,
  :participantID 37,
  :plan [[:Lobby]],
  :unvisitedAreas [] }`
</pre>
> File Encoded-maps-62.clj has 62 encoded maps, contact DOLL to get the file.

### Problems with manually encoding pre-trial maps
• Time consuming </br>
• Typing Errors </br>
• Over-interpretation </br>
• Many maps are really hard to understand </br>


# Understanding the Encoding (:plan)
###Abbreviation Name for Falcon's Rooms
<pre>
:R101 "Room 101"
:R102 "Room 102"
:R103 "Room 103"
:R104 "Room 104"
:R105 "Room 105"
:R106 "Room 107"
:R108 "Room 108"
:R109 "Room 109"
:R110 "Room 110"
:R111 "Room 111"
:Lobby "Lobby"
:SO "Security office"
:CloakR "Cloak room"
:BreakR "Break room"
:ExecS1 "Executive Suite 1"
:ExecS2 "Executive Suite 2"
:KO "King Chris's Office"
:KT "The King's Terrace"
:WomRR "Womens rest room"
:MenRR "Mens rest room"
:RoomJ "Janitor"
:SCR1 "Amway Conference Room 1"
:SCR2 "Mary Conference Room 2"
:MCR "Herballfe Conference Room"
:Cfarm "Computer farm"
</pre>

### Vector vs Set
<pre>
Vector [ ]
• represents an ordered list of rooms.
• uses vector when you know the direction and the order of rooms the player wants to visit.
• example: lines with clear directions.


Set #{ } 
• represents an unordered list of rooms.
• uses set to show a collection without a specified order.
• example: circle around 4 rooms, unclear drawings.

Note: We can have multiple layers of sets and/or vectors within the same plan.
</pre>


#### Example:
![](./resources/pretrial_maps/HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-56_Team-na_Member-30_Vers-1.png)

<pre>
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-56_Team-na_Member-30_Vers-1.png", 
 :trialID 56, 
 :participantID 30, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR]
  #{:SCR1 :SCR2}
  [:R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :WomRR :MenRR :RoomJ]], 
 :unvisitedAreas []} 
</pre> 

Number of layer: 2 `[ [] #{} [] ]` </br>
• Layer 1: Outter Vector </br>
• Layer 2: Vector, Set, Vector </br>

Intepretaion: The player visits the 1st vector, then the set, then the last vector. 

1. `[[:Lobby]]` The player always starts at the Lobby
2. `[[:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR]` The player visits each room in the exact order listed in this vector. 
3. `[[:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR] #{:SCR1 :SCR2}]` The player then visits :SCR1 :SCR2, but because the drawing is unclear, we don't know which room the player wants to visit first. 
</br>
4. The player visits the following rooms in the order from left to right: `[:R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :WomRR :MenRR :RoomJ]`.

Most of them time, `:unvisitedAreas` is empty. However, if the player forgot to visit a room, that room will be listed in the `:unvisitedAreas` section




