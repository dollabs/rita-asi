{  :R101 "Room 101"
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
   :CSNorth "Corner space north" 
   :CSEast "Corner space east"  
   :Terrace "Terrace"
   :WomRR "Womens rest room"
   :MenRR "Mens rest room"
   :RoomJ "Janitor"
   :SCR1 "Amway Conference Room 1"
   :SCR2 "Mary Conference Room 2"
   :MCR "Herballfe Conference Room"
   :Cfarm "Computer farm"
   :StagingArea "Outside Staging Area"}

;; vector [] = ordered path
;; set #{} = unordered path
;; map {} = multiple plans founded 

;; Member 30
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-55_Team-na_Member-30_Vers-1.png", 
 :trialID 55, 
 :participantID 30, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :R106 :WomRR :MenRR :RoomJ :Cfarm :R111 :R110 :R109 :R108 :R107 :SCR2 :SCR1]
 :unvisitedArea []}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-56_Team-na_Member-30_Vers-1.png", 
 :trialID 56, 
 :participantID 30, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR]
  #{:SCR1 :SCR2}
  [:R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :WomRR :MenRR :RoomJ]], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-57_Team-na_Member-30_Vers-1.png", 
 :trialID 57, 
 :participantID 30, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :Room101 :MCR :SCR2 :SCR1 :MenRR :WomRR :RoomJ :Cfarm :Room111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102], 
 :unvisitedAreas []} 

;; Member 33
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-64_Team-na_Member-33_Vers-1.png", 
 :trialID 64, 
 :participantID 33, 
 :plan 
 [[:Lobby]
  #{:SO :Cfarm :BreakR :ExecS1 :ExecS2 :KO :KT}
  [:R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :WomRR :MenRR :RoomJ]
  #{:MCR :SCR1 :SCR2}], 
 :unvisitedAreas []} 
 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-65_Team-na_Member-33_Vers-1.png", 
  :trialID 65, 
  :participantID 33, 
  :plan 
  [[:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101]
   #{:KO :KT}
   #{:MCR :SRC1 :SRC2}
   #{:ExecS2 :ExecS1 :BreakR :RoomJ}], 
  :unvisitedAreas [:SO :MenRR :WomRR]} 
 
 {:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-66_Team-na_Member-33_Vers-1.png",
 :trialID 66, 
 :participantID 33, 
 :plan 
 [[:Lobby :Cfarm]
  #{:R111 :R110 :R109 :R108 :R107 :R106 :R105}
  #{:R104 :R103 :R102 :R101}
  #{:MCR :SCR1 :SCR2 :RoomJ :MenRR :WomRR}
  #{:KO :KT :ExecS1 :ExecS2 :BreakR :SO}]
 :unvisitedAreas []} 

;; Member 42
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-91_Team-na_Member-42_Vers-1.png", 
 :trialID 91, 
 :participantID 42, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2]
  #{#{:KT :KO}
    #{:R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111}
    [:R101 :R102]
    [:MCR :SCR1 :SCR2 :RoomJ :MenRR :WomRR]
    [:Cfarm]}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-92_Team-na_Member-42_Vers-1.png", 
 :trialID 92, 
 :participantID 42, 
 :plan [:Lobby :Cfarm :BreakR :ExecS1 :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104: :R105 :R106 :R107 :R108 :R109 :R110 :111 :WomRR :MenRR :RoomJ :SCR1 :SCR2]
 :unvisitedArea [:SO]}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-93_Team-na_Member-42_Vers-1.png", 
 :trialID 93, 
 :participantID 42, 
 :plan 
 [[:Lobby :Cfarm :SO :BreakJ :ExecS1 :RoomJ :ExecS2]
  #{:KO :KT}
  [:SCR1 :SCR2 :MCR :R101 :R102 :R103 :R104 :R105 :R106 :R107]
  #{[:WomRR :MenRR]
    [:R108 :R109 :R110 :R111]}], 
 :unvisitedAreas []}

;; Member 43
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-94_Team-na_Member-43_Vers-1.png", 
 :trialID 94, 
 :participantID 43, 
 :plan 
 [[:Lobby]
  #{[:RoomJ :MenRR :WomRR :SCR2 :SCR1]
    [[:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :MCR]
     #{:KO :KT}
     [:ExecS2 :ExecS1 :BreakR]]}], 
 :unvisitedAreas [:SO]} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-95_Team-na_Member-43_Vers-1.png", 
 :trialID 95, 
 :participantID 43, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :RoomJ :MenRR :WomRR :SCR1 :SCR2 :MCR], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-96_Team-na_Member-43_Vers-1.png", 
 :trialID 96, 
 :participantID 43, 
 :plan
 [[:Lobby]
  #{[:SO :BreakR :ExecS1 :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :MCR :SCR1 :SCR2 :R106 :R107 :R108 :R109 :R110 :R111]
    [:Cfarm]
    [:RoomJ :MenRR :WomRR]}]
  :unvisitedArea []}

;; Member 44
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-97_Team-na_Member-44_Vers-1.png", 
 :trialID 97, 
 :participantID 44, 
 :plan 
 [[:Lobby]
  #{:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :ExecS2 :ExecS1 :RoomJ :MenRR :WomRR :SCM2 :SCM1 :SO :BreakR :MCR}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-98_Team-na_Member-44_Vers-1.png", 
 :trialID 98, 
 :participantID 44, 
 :plan [:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :ExecS2 :ExecS1 :RoomJ :MenRR :WomRR :SCM2 :SCM1 :MCR]
 :unvisitedArea [:SO :BreakR]}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-99_Team-na_Member-44_Vers-1.png", 
 :trialID 99, 
 :participantID 44, 
 :plan
 [[:Lobby :Cfarm]
  #{:R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103}
  #{:R102 :R101 :KO :KT}
  #{:MCR :SCR1 :SCR2 :RoomJ :MenRR :WomRR}
  #{:SO :BreakR :ExecS1 :ExecS2}]
 :unvisitedArea []}

 ;; Member 46
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-103_Team-na_Member-46_Vers-1.png", 
 :trialID 103, 
 :participantID 46, 
 :plan 
 [[:Lobby :Cfarm]
  #{:R111 :R110 :R109}
  [:R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR1 :SCR2 :WomRR :MenRR]
  #{:RoomJ :ExecS2 :ExecS1 :BreakR :SO}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-104_Team-na_Member-46_Vers-1.png", 
 :trialID 104, 
 :participantID 46, 
 :plan [:Lobby :SO :BreakR :ExecS1 :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :WomRR :MenRR :R108 :R109 :R110 :R111 :Cfarm], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-105_Team-na_Member-46_Vers-1.png", 
 :trialID 105, 
 :participantID 46, 
 :plan [:Lobby :SO :BreakR :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR2 :SCR1 :ExecS2 :RoomJ :ExecS1 :menRR :WomRR], 
 :unvisitedAreas []} 

;; Member 49
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-112_Team-na_Member-49_Vers-1.png", 
 :trialID 112, 
 :participantID 49, 
 :plan 
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :MCR :SCR2 :SCR1 :KT :KO :ExecS2 :ExecS1 :RoomJ :MenRR :WomRR :BreakR :SO],
   :plan2 [:SO :BreakR :WomRR :MenRR :RoomJ :ExecS1 :ExecS2 :KO :KT :SCR1 :SCR2 :MCR :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}], 
 :unvisitedAreas []}  
  
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-113_Team-na_Member-49_Vers-1.png", 
 :trialID 113, 
 :participantID 49, 
 :plan 
 [[:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :MCR :SCR2 :SCR1]
  #{:KO :KT}
  [:ExecS2 :RoomJ :ExecS1 :MenRR :WomRR :BreakR :SO]], 
 :unvisitedAreas []} 
  
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-114_Team-na_Member-49_Vers-1.png", 
 :trialID 114, 
 :participantID 49, 
 :plan [], 
 :unvisitedAreas []} 

;; Member 51
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-118_Team-na_Member-51_Vers-1.png", 
 :trialID 118, 
 :participantID 51, 
 :plan 
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR2 :SCR1 :ExecS2 :RoomJ :MenRR :WomRR :ExecS1 :BreakR :SO],
   :plan2 [:SO :BreakR :ExecS1 :WomRR :MenRR :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :KT :KO :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}
  ], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-119_Team-na_Member-51_Vers-1.png", 
 :trialID 119, 
 :participantID 51, 
 :plan 
 [[:Lobby]
  #{[:Cfarm]
    [:RoomJ :MenRR :WomRR]}
  [:R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KT :SCR2 :SCR1 :ExecS2]], 
 :unvisitedAreas [:KT :MCR :ExecS1 :BreakR :SO]} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-120_Team-na_Member-51_Vers-1.png", 
 :trialID 120, 
 :participantID 51, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2]
  #{:KO :KT}
  [:R101 :R102 :R103 :R104 :R105]
  #{:SCR1 :SRC2 :MCR}
  [:R106 :R107]
  #{:WomRR :MenRR :RoomJ}
  [:R108 :R109 :R110 :R111 :Cfarm]], 
 :unvisitedAreas []} 

;; Member 52  
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-121_Team-na_Member-52_Vers-1.png", 
 :trialID 121, 
 :participantID 52, 
 :plan 
 [[:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT]
  #{[:ExecS2 :ExecS1 :BreakR]
    [:SCR1 :SCR2 :MCR]
    #{:RoomJ :MenRR :WomRR}}], 
 :unvisitedAreas [:SO]} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-123_Team-na_Member-52_Vers-1.png", 
 :trialID 123, 
 :participantID 52, 
 :plan 
 [[:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :WomRR :MenRR :BreakR :ExecS1 :RoomJ :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105]
  #{[:R106 :R107]
    [:MCR :SCR2 :SCR1]}], 
 :unvisitedAreas []} 

;; Member 54
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-127_Team-na_Member-54_Vers-1.png", 
 :trialID 127, 
 :participantID 54, 
 :plan [:Lobby :Cfarm :RoomJ :MenRR :WomRR :SCR2 :SCR1 :MCR :R102 :R101 :KO :ExecS2 :ExecS1 :BreakR :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104], 
 :unvisitedAreas [:KT :R103 :SO]} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-128_Team-na_Member-54_Vers-1.png", 
 :trialID 128, 
 :participantID 54, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2]
  #{:SCR1 :SCR2 :MCR :KO :KT :R101 :R102}
  [:R103 :R104 :R105 :R106 :R107]
  #{:WomRR :R108 :R109 :R110 :R111 :Cfarm}], 
 :unvisitedAreas [:MenRR :RoomJ]} 

;; Member 55
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-130_Team-na_Member-55_Vers-1.png", 
 :trialID 130, 
 :participantID 55, 
 :plan [:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :MCR :SCR2 :SCR1 :WomRR :MenRR :RoomJ :BreakR :ExecS1 :ExecS2 :KO :KT], 
 :unvisitedAreas []}

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-131_Team-na_Member-55_Vers-1.png", 
 :trialID 131, 
 :participantID 55, 
 :plan 
 [[:Lobby :SO :BreakR :ExecS1 :ExecS2]
  #{:KO :KT}
  [:R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :WomRR :MenRR :RoomJ :SCR1 :SCR2 :MCR]], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-132_Team-na_Member-55_Vers-1.png", 
 :trialID 132, 
 :participantID 55, 
 :plan [:Lobby :SO :BreakR :Cfarm :R111 :R110 :R109 :R108 :WomRR :MenRR :ExecS1 :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT], 
 :unvisitedAreas []} 

;; Member 60 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-147_Team-na_Member-60_Vers-1.png", 
 :trialID 147, 
 :participantID 60, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :R101 :R102 :MCR :SCR1 :SCR2 :RoomJ :MenRR :WomRR :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :Cfarm], 
 :unvisitedAreas []} 

;; Member 72
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-181_Team-na_Member-72_Vers-1.png", 
 :trialID 181, 
 :participantID 72, 
 :plan 
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR2 :SCR1 :ExecS2 :RoomJ :MenRR :WomRR :ExecS1 :BreakR :SO],
   :plan2 [:SO :BreakR :ExecS1 :WomRR :MenRR :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :KT :KO :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-182_Team-na_Member-72_Vers-1.png", 
 :trialID 182, 
 :participantID 72, 
 :plan 
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR2 :SCR1 :ExecS2 :RoomJ :MenRR :WomRR :ExecS1 :BreakR :SO],
   :plan2 [:SO :BreakR :ExecS1 :WomRR :MenRR :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :KT :KO :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}], 
 :unvisitedAreas []} 


{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-183_Team-na_Member-72_Vers-1.png", 
 :trialID 183, 
 :participantID 72, 
 :plan [:Lobby :BreakR :ExecS1 :ExecS2 :KO :KT :MCR :SCR2 :SCR1 :RoomJ :MenRR :WomRR :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101], 
 :unvisitedAreas [:SO]} 

;; Member 74
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-187_Team-na_Member-74_Vers-1.png", 
 :trialID 187, 
 :participantID 74, 
 :plan 
 [[:Lobby]
  {:plan1 [:SO :ExecS1 :ExecS2 :KO :KT :R101 :R102 :MCR :SCR1 :SCR2 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :WomRR :MenRR :RoomJ :BreakR :Cfarm]
   :plan2 [:Cfarm :BreakR :RoomJ :MenRR :WomRR :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :SCR2 :SCR1 :MCR :R102 :R101 :KT :KO :ExecS2 :ExecS1 :SO]}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-188_Team-na_Member-74_Vers-1.png", 
 :trialID 188, 
 :participantID 74, 
 :plan [:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :MCR :SCR2 :SCR1 :RoomJ :WomRR :MenRR :BreakR :ExecS1 :ExecS2 :KO :KT :SO], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-189_Team-na_Member-74_Vers-1.png", 
 :trialID 189, 
 :participantID 74, 
 :plan [:Lobby :Cfarm :R111 :R110 :R109 :R108 :WomRR :MenRR :BreakR :ExecS1 :RoomJ :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :MCR :SCR1 :SCR2 :SO], 
 :unvisitedAreas []} 

;; Member 81
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-208_Team-na_Member-81_Vers-1.png", 
 :trialID 208, 
 :participantID 81, 
 :plan [:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :SCR1 :SCR2 :MCR :ExecS2 :RoomJ :ExecS1 :MenRR :WomRR], 
 :unvisitedAreas [:BreakR]} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-209_Team-na_Member-81_Vers-1.png", 
 :trialID 209, 
 :participantID 81, 
 :plan [:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :ExecS2 :RoomJ :ExecS1 :MenRR :WomRR :MCR :SCR2 :SCR1], 
 :unvisitedAreas [:BreakR]}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-210_Team-na_Member-81_Vers-1.png", 
 :trialID 210, 
 :participantID 81, 
 :plan [:Lobby :BreakR :ExecS1 :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :RoomJ :MenRR :WomRR :MCR :SCR2 :SCR1], 
 :unvisitedAreas [:SO]} 

;; Member 87
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-227_Team-na_Member-87_Vers-1.png", 
 :trialID 227, 
 :participantID 87, 
 :plan 
 [[:Lobby :Cfarm]
  #{:RoomJ :MenRR :WomRR :SCR1 :SCR2}
  [:MCR]
  #{:R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103}
  #{:R102 :R101}
  #{:KT :KO}
  #{:ExecS2 :ExecS1 :BreakR :SO}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-226_Team-na_Member-87_Vers-1.png", 
 :trialID 226, 
 :participantID 87, 
 :plan 
 [[:Lobby]
  {:plan1 [:Cfarm :RoomJ :MenRR :WomRR :R111 :R110 :R109 :R108 :R107 :R106 :R105 :SCR1 :SCR2 :MCR :R104 :R103 :R102 :R101 :KO :KT :ExecS2 :ExecS1 :BreakR :SO]
   :plan2 [:SO :BreakR :ExecS1 :ExecS2 :KT :KO :R101 :R102 :R103 :R104 :MCR :SCR2 :SCR1 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :WomRR :MenRR :RoomJ :Cfarm]}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-228_Team-na_Member-87_Vers-1.png", 
 :trialID 228, 
 :participantID 87, 
 :plan
 [[:Lobby :BreakR :Cfarm]
  #{:R111 :R110 :R109 :R108}
  #{:WomRR :MenRR :RoomJ :SCR1 :SCR2}
  [:MCR]
  #{:R107 :R106 :R105 :R104 :R1023}
  #{:R102 :R101}
  #{:KO :KT}
  #{:ExecS1 :ExecS2}]
 :unvisitedArea [:SO]}

;; Member 89
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-232_Team-na_Member-89_Vers-1.png", 
 :trialID 232, 
 :participantID 89, 
 :plan [:Lobby :SO :BreakR :ExecS1 :RoomJ :ExecS2 :SCR1 :KO :KT :MCR :R101 :R102 :R103 :R104 :SCR2 :R105 :R106 :R107 :WomRR :MenRR :R108 :Cfarm :R109 :R110 :R111], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-233_Team-na_Member-89_Vers-1.png", 
 :trialID 233, 
 :participantID 89, 
 :plan 
 [[:Lobby :SO :BreakR]
  #{:RoomJ :MenRR}
  [:ExecS1 :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :MCR :SCR2 :SCR1 :R106 :R107 :WomRR :R108 :R109 :Cfarm :R110 :R111]], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-234_Team-na_Member-89_Vers-1.png", 
 :trialID 234, 
 :participantID 89, 
 :plan [[:Lobby :ExecS1 :RoomJ :ExecS2 :SCR1 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :MCR :SCR2 :R106 :R107 :R108 :WomRR :MenRR :R109 :Cfarm :R110 :R111]], 
 :unvisitedAreas [:SO :BreakR]} 

;; Member 94
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-247_Team-na_Member-94_Vers-1.png", 
 :trialID 247, 
 :participantID 94, 
 :plan 
 [[:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :MCR :SCR2 :SCR1 :ExecS2] 
  {:RoomJ :MenRR :WomRR :ExecS1 :BreakR :SO}], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-248_Team-na_Member-94_Vers-1.png", 
 :trialID 248, 
 :participantID 94, 
 :plan [:Lobby :Cfarm], 
 :unvisitedAreas [:R111 :R110 :R109 :R108 :SO :BreakR :ExecS1 :RoomJ :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :R106 :R107 :WomRR :MenRR :SCR1 :SCR2]} 

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-249_Team-na_Member-94_Vers-1.png", 
 :trialID 249, 
 :participantID 94, 
 :plan
 [[:Lobby]
  #{[:Cfarm #{:R111 :R110 :R109 :R108}] 
    [:SO :BreakR #{:ExecS1 :RoomJ} :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :R106 :R107]}]
 :unvisitedArea [:WomRR :MenRR :SCR1 :SCR2]}

;; Member 95
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-250_Team-na_Member-95_Vers-1.png", 
 :trialID 250, 
 :participantID 95, 
 :plan 
 [[:Lobby]
  {:plan1 [:SO :BreakR :ExecS1 :RoomJ :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :SCR2 :SCR1 :R105 :R106 :R107 :WomRR :MenRR :R108 :R109 :R110 :R111 :Cfarm]
   :plan2 [:Cfarm :R111 :R110 :R109 :R108 :MenRR :WomRR :R107 :R106 :R105 :SCR1 :SCR2 :R103 :R102 :R101 :MCR :KT :KO :ExecS2 :RoomJ :ExecS1 :BreakR :SO]}], 
 :unvisitedAreas [:R104]} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-251_Team-na_Member-95_Vers-1.png", 
 :trialID 251, 
 :participantID 95, 
 :plan [:Lobby :SO :BreakR :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KT :KO :MCR :SCR2 :SCR1 :ExecS2 :ExecS1 :RoomJ :MenRR :WomRR], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-s252_Team-na_Member-95_Vers-1.png", 
 :trialID 252, 
 :participantID 95, 
 :plan [:Lobby :SO :BreakR :Cfarm :R111 :R110 :R109 :R108 :WomRR :MenRR :R107 :R106 :SCR2 :SCR1 :R105 :R104 :R103 :R102 :R101 :MCR :KO :KT :ExecS2 :ExecS1], 
 :unvisitedAreas [:RoomJ]} 

;; Member 97
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-256_Team-na_Member-97_Vers-1.png", 
 :trialID 256, 
 :participantID 97, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :RoomJ :MenRR :WomRR :SCR2 :SCR1 :MCR], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-257_Team-na_Member-97_Vers-1.png", 
 :trialID 257, 
 :participantID 97, 
 :plan [:Lobby :SO :BreakR :ExecS1 :ExecS2 :KO :KT :MCR :SCR1 :SCR2 :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :RoomJ :MenRR :WomRR], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-258_Team-na_Member-97_Vers-1.png", 
 :trialID 258, 
 :participantID 97, 
 :plan [:Lobby :SO :BreakR :ExecS1 :RoomJ :ExecS2 :SCR1 :SCR2 :MCR :KO :KT :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm :MenRR :WomRR], 
 :unvisitedAreas []} 

;; Member 98
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-259_Team-na_Member-98_Vers-1.png", 
 :trialID 259, 
 :participantID 98, 
 :plan
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :R104 :R103 :R102 :R101 :KO :KT :SCR1 :SCR2 :MCR :WomRR :MenRR :RoomJ :ExecS2 :ExecS1 :BreakR :SO],
   :plan2 [:SO :BreakR :ExecS1 :ExecS1 :RoomJ :MenRR :WomRR :MCR :SCR2 :SCR1 :KT :KO :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}]
 :unvisitedArea []}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-260_Team-na_Member-98_Vers-1.png", 
 :trialID 260, 
 :participantID 98, 
 :plan
 [[:Lobby]
  {:plan1 [:Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :WomRR :MenRR :RoomJ :SCR1 :SCR2 :MCR :R105 :R104 :R103 :R102 :R101 :KO :KT :ExecS2 :ExecS1],
   :plan2 [:ExecS1 :ExecS2 :KO :KT :R101 :R102 :R103 :R104 :R105 :MCR :SCR2 :SCR1 :RoomJ :MenRR :WomRR :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm]}]
 :unvisitedArea [:SO :BreakR]}

{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-261_Team-na_Member-98_Vers-1.png", 
 :trialID 261, 
 :participantID 98, 
 :plan 
 [[:Lobby :Cfarm :R111 :R110 :R109 :R108 :R107 :R106 :R105 :MCR :R101 :R103 :R102 :R104 :KO :KT :SCR1 :SCR2 :ExecS2 :RoomJ :ExecS1]
  #{:MenRR :WomRR :BreakR :SO}], 
 :unvisitedAreas []} 

;; Member 101
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-268_Team-na_Member-101_Vers-1.png", 
 :trialID 268, 
 :participantID 101, 
 :plan [:Lobby :SO :BreakR :ExecS1 :RoomJ :MenRR :WomRR :SCR2 :SCR1 :ExecS2 :KO :KT :MCR :R101 :R102 :R103 :R104 :R105 :R106 :R107 :R108 :R109 :R110 :R111 :Cfarm], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-269_Team-na_Member-101_Vers-1.png", 
 :trialID 269, 
 :participantID 101, 
 :plan [:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :R107 :WomRR :MenRR :BreakR :ExecS1 :RoomJ :ExecS2 :SCR1 :SCR2 :R106 :R105 :MCR :KO :KT :R101 :R102 :R103 :R104], 
 :unvisitedAreas []} 

{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-270_Team-na_Member-101_Vers-1.png", 
 :trialID 270, 
 :participantID 101, 
 :plan [:Lobby :SO :Cfarm :R111 :R110 :R109 :R108 :R107 :WomRR :MenRR :BreakR :ExecS1 :RoomJ :ExecS2 :SCR1 :SCR2 :R106 :R105 :MCR :KO :KT :R101 :R102 :R103 :R104], 
 :unvisitedAreas []} 







{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-145_Team-na_Member-60_Vers-1.png", :trialID 145, :participantID 60, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-146_Team-na_Member-60_Vers-1.png", :trialID 146, :participantID 60, :plan [[:Lobby]], :unvisitedAreas []}
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-90_Team-na_Member-41_Vers-1.png", :trialID 90, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-243_Team-na_Member-92_Vers-1.png", :trialID 243, :participantID 92, :plan [[:Lobby]], :unvisitedAreas []}
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-197_Team-na_Member-77_Vers-1.png", :trialID 197, :participantID 77, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-114_Team-na_Member-49_Vers-1.png", :trialID 114, :participantID 49, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-271_Team-na_Member-102_Vers-1.png", :trialID 271, :participantID 102, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-135_Team-na_Member-56_Vers-1.png", :trialID 135, :participantID 56, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-44_Team-na_Member-26_Vers-1.png", :trialID 44, :participantID 26, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-78_Team-na_Member-37_Vers-1.png", :trialID 78, :participantID 37, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-177_Team-na_Member-70_Vers-1.png", :trialID 177, :participantID 70, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-202_Team-na_Member-79_Vers-1.png", :trialID 202, :participantID 79, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-102_Team-na_Member-45_Vers-1.png", :trialID 102, :participantID 45, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-190_Team-na_Member-75_Vers-1.png", :trialID 190, :participantID 75, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-47_Team-na_Member-27_Vers-1.png", :trialID 47, :participantID 27, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-77_Team-na_Member-37_Vers-1.png", :trialID 77, :participantID 37, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-172_Team-na_Member-69_Vers-1.png", :trialID 172, :participantID 69, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-192_Team-na_Member-75_Vers-1.png", :trialID 192, :participantID 75, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-151_Team-na_Member-62_Vers-1.png", :trialID 151, :participantID 62, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-80_Team-na_Member-38_Vers-1.png", :trialID 80, :participantID 38, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-164_Team-na_Member-66_Vers-1.png", :trialID 164, :participantID 66, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-170_Team-na_Member-68_Vers-1.png", :trialID 170, :participantID 68, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-215_Team-na_Member-83_Vers-1.png", :trialID 215, :participantID 83, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-86_Team-na_Member-40_Vers-1.png", :trialID 86, :participantID 40, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-43_Team-na_Member-26_Vers-1.png", :trialID 43, :participantID 26, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-101_Team-na_Member-45_Vers-1.png", :trialID 101, :participantID 45, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-113_Team-na_Member-49_Vers-1.png", :trialID 113, :participantID 49, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-150_Team-na_Member-61_Vers-1.png", :trialID 150, :participantID 61, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-139_Team-na_Member-58_Vers-1.png", :trialID 139, :participantID 58, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-169_Team-na_Member-68_Vers-1.png", :trialID 169, :participantID 68, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-160_Team-na_Member-65_Vers-1.png", :trialID 160, :participantID 65, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-186_Team-na_Member-73_Vers-1.png", :trialID 186, :participantID 73, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-174_Team-na_Member-69_Vers-1.png", :trialID 174, :participantID 69, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-154_Team-na_Member-63_Vers-1.png", :trialID 154, :participantID 63, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-157_Team-na_Member-64_Vers-1.png", :trialID 157, :participantID 64, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-70_Team-na_Member-35_Vers-1.png", :trialID 70, :participantID 35, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-229_Team-na_Member-88_Vers-1.png", :trialID 229, :participantID 88, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-213_Team-na_Member-82_Vers-1.png", :trialID 213, :participantID 82, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-156_Team-na_Member-63_Vers-1.png", :trialID 156, :participantID 63, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-220_Team-na_Member-85_Vers-1.png", :trialID 220, :participantID 85, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-231_Team-na_Member-88_Vers-1.png", :trialID 231, :participantID 88, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-225_Team-na_Member-86_Vers-1.png", :trialID 225, :participantID 86, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-115_Team-na_Member-50_Vers-1.png", :trialID 115, :participantID 50, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-230_Team-na_Member-88_Vers-1.png", :trialID 230, :participantID 88, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-204_Team-na_Member-79_Vers-1.png", :trialID 204, :participantID 79, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-155_Team-na_Member-63_Vers-1.png", :trialID 155, :participantID 63, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-49_Team-na_Member-28_Vers-1.png", :trialID 49, :participantID 28, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-273_Team-na_Member-102_Vers-1.png", :trialID 273, :participantID 102, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-212_Team-na_Member-82_Vers-1.png", :trialID 212, :participantID 82, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-112_Team-na_Member-49_Vers-1.png", :trialID 112, :participantID 49, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-205_Team-na_Member-80_Vers-1.png", :trialID 205, :participantID 80, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-46_Team-na_Member-27_Vers-1.png", :trialID 46, :participantID 27, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-114_Team-na_Member-49_Vers-1.png", :trialID 114, :participantID 49, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-242_Team-na_Member-92_Vers-1.png", :trialID 242, :participantID 92, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-81_Team-na_Member-38_Vers-1.png", :trialID 81, :participantID 38, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-84_Team-na_Member-39_Vers-1.png", :trialID 84, :participantID 39, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-217_Team-na_Member-84_Vers-1.png", :trialID 217, :participantID 84, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-214_Team-na_Member-83_Vers-1.png", :trialID 214, :participantID 83, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-193_Team-na_Member-76_Vers-1.png", :trialID 193, :participantID 76, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-87_Team-na_Member-40_Vers-1.png", :trialID 87, :participantID 40, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-245_Team-na_Member-93_Vers-1.png", :trialID 245, :participantID 93, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-88_Team-na_Member-41_Vers-1.png", :trialID 88, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-222_Team-na_Member-85_Vers-1.png", :trialID 222, :participantID 85, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-162_Team-na_Member-65_Vers-1.png", :trialID 162, :participantID 65, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-173_Team-na_Member-69_Vers-1.png", :trialID 173, :participantID 69, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-221_Team-na_Member-85_Vers-1.png", :trialID 221, :participantID 85, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-69_Team-na_Member-34_Vers-1.png", :trialID 69, :participantID 34, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-203_Team-na_Member-79_Vers-1.png", :trialID 203, :participantID 79, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-191_Team-na_Member-75_Vers-1.png", :trialID 191, :participantID 75, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-266_Team-na_Member-100_Vers-1.png", :trialID 266, :participantID 100, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-60_Team-na_Member-31_Vers-1.png", :trialID 60, :participantID 31, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-240_Team-na_Member-91_Vers-1.png", :trialID 240, :participantID 91, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-72_Team-na_Member-35_Vers-1.png", :trialID 72, :participantID 35, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-74_Team-na_Member-36_Vers-1.png", :trialID 74, :participantID 36, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-73_Team-na_Member-36_Vers-1.png", :trialID 73, :participantID 36, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-158_Team-na_Member-64_Vers-1.png", :trialID 158, :participantID 64, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-239_Team-na_Member-91_Vers-1.png", :trialID 239, :participantID 91, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-165_Team-na_Member-66_Vers-1.png", :trialID 165, :participantID 66, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-89_Team-na_Member-41_Vers-1.png", :trialID 89, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-175_Team-na_Member-70_Vers-1.png", :trialID 175, :participantID 70, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-45_Team-na_Member-26_Vers-1.png", :trialID 45, :participantID 26, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-198_Team-na_Member-77_Vers-1.png", :trialID 198, :participantID 77, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-62_Team-na_Member-32_Vers-1.png", :trialID 62, :participantID 32, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-111_Team-na_Member-48_Vers-1.png", :trialID 111, :participantID 48, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-100_Team-na_Member-45_Vers-1.png", :trialID 100, :participantID 45, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-126_Team-na_Member-53_Vers-1.png", :trialID 126, :participantID 53, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-153_Team-na_Member-62_Vers-1.png", :trialID 153, :participantID 62, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-179_Team-na_Member-71_Vers-1.png", :trialID 179, :participantID 71, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-180_Team-na_Member-71_Vers-1.png", :trialID 180, :participantID 71, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-207_Team-na_Member-80_Vers-1.png", :trialID 207, :participantID 80, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-161_Team-na_Member-65_Vers-1.png", :trialID 161, :participantID 65, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-262_Team-na_Member-99_Vers-1.png", :trialID 262, :participantID 99, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-267_Team-na_Member-100_Vers-1.png", :trialID 267, :participantID 100, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-171_Team-na_Member-68_Vers-1.png", :trialID 171, :participantID 68, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-90_Team-na_Member-41_Vers-1.png", :trialID 90, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-218_Team-na_Member-84_Vers-1.png", :trialID 218, :participantID 84, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-238_Team-na_Member-91_Vers-1.png", :trialID 238, :participantID 91, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-117_Team-na_Member-50_Vers-1.png", :trialID 117, :participantID 50, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-235_Team-na_Member-90_Vers-1.png", :trialID 235, :participantID 90, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-136_Team-na_Member-57_Vers-1.png", :trialID 136, :participantID 57, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-83_Team-na_Member-39_Vers-1.png", :trialID 83, :participantID 39, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-194_Team-na_Member-76_Vers-1.png", :trialID 194, :participantID 76, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-144_Team-na_Member-59_Vers-1.png", :trialID 144, :participantID 59, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-59_Team-na_Member-31_Vers-1.png", :trialID 59, :participantID 31, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-68_Team-na_Member-34_Vers-1.png", :trialID 68, :participantID 34, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-138_Team-na_Member-57_Vers-1.png", :trialID 138, :participantID 57, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-255_Team-na_Member-96_Vers-1.png", :trialID 255, :participantID 96, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-236_Team-na_Member-90_Vers-1.png", :trialID 236, :participantID 90, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-195_Team-na_Member-76_Vers-1.png", :trialID 195, :participantID 76, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-79_Team-na_Member-38_Vers-1.png", :trialID 79, :participantID 38, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-159_Team-na_Member-64_Vers-1.png", :trialID 159, :participantID 64, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-76_Team-na_Member-37_Vers-1.png", :trialID 76, :participantID 37, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-48_Team-na_Member-27_Vers-1.png", :trialID 48, :participantID 27, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-110_Team-na_Member-48_Vers-1.png", :trialID 110, :participantID 48, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-124_Team-na_Member-53_Vers-1.png", :trialID 124, :participantID 53, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-75_Team-na_Member-36_Vers-1.png", :trialID 75, :participantID 36, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-163_Team-na_Member-66_Vers-1.png", :trialID 163, :participantID 66, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-264_Team-na_Member-99_Vers-1.png", :trialID 264, :participantID 99, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-272_Team-na_Member-102_Vers-1.png", :trialID 272, :participantID 102, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-143_Team-na_Member-59_Vers-1.png", :trialID 143, :participantID 59, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-244_Team-na_Member-93_Vers-1.png", :trialID 244, :participantID 93, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-184_Team-na_Member-73_Vers-1.png", :trialID 184, :participantID 73, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-148_Team-na_Member-61_Vers-1.png", :trialID 148, :participantID 61, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-108_Team-na_Member-47_Vers-1.png", :trialID 108, :participantID 47, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-140_Team-na_Member-58_Vers-1.png", :trialID 140, :participantID 58, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-263_Team-na_Member-99_Vers-1.png", :trialID 263, :participantID 99, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-253_Team-na_Member-96_Vers-1.png", :trialID 253, :participantID 96, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-DynamicMap_Trial-85_Team-na_Member-40_Vers-1.png", :trialID 85, :participantID 40, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-116_Team-na_Member-50_Vers-1.png", :trialID 116, :participantID 50, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-88_Team-na_Member-41_Vers-1.png", :trialID 88, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-216_Team-na_Member-83_Vers-1.png", :trialID 216, :participantID 83, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-142_Team-na_Member-59_Vers-1.png", :trialID 142, :participantID 59, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-63_Team-na_Member-32_Vers-1.png", :trialID 63, :participantID 32, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-265_Team-na_Member-100_Vers-1.png", :trialID 265, :participantID 100, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-133_Team-na_Member-56_Vers-1.png", :trialID 133, :participantID 56, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-50_Team-na_Member-28_Vers-1.png", :trialID 50, :participantID 28, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-185_Team-na_Member-73_Vers-1.png", :trialID 185, :participantID 73, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-107_Team-na_Member-47_Vers-1.png", :trialID 107, :participantID 47, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-67_Team-na_Member-34_Vers-1.png", :trialID 67, :participantID 34, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-149_Team-na_Member-61_Vers-1.png", :trialID 149, :participantID 61, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-141_Team-na_Member-58_Vers-1.png", :trialID 141, :participantID 58, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-219_Team-na_Member-84_Vers-1.png", :trialID 219, :participantID 84, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-51_Team-na_Member-28_Vers-1.png", :trialID 51, :participantID 28, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-58_Team-na_Member-31_Vers-1.png", :trialID 58, :participantID 31, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-196_Team-na_Member-77_Vers-1.png", :trialID 196, :participantID 77, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-241_Team-na_Member-92_Vers-1.png", :trialID 241, :participantID 92, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconMed-StaticMap_Trial-211_Team-na_Member-82_Vers-1.png", :trialID 211, :participantID 82, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-199_Team-na_Member-78_Vers-1.png.PNG", :trialID 199, :participantID 78, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-109_Team-na_Member-48_Vers-1.png", :trialID 109, :participantID 48, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-137_Team-na_Member-57_Vers-1.png", :trialID 137, :participantID 57, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-237_Team-na_Member-90_Vers-1.png", :trialID 237, :participantID 90, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-199_Team-na_Member-78_Vers-1.png", :trialID 199, :participantID 78, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-176_Team-na_Member-70_Vers-1.png", :trialID 176, :participantID 70, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-206_Team-na_Member-80_Vers-1.png", :trialID 206, :participantID 80, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-224_Team-na_Member-86_Vers-1.png", :trialID 224, :participantID 86, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconHard-StaticMap_Trial-134_Team-na_Member-56_Vers-1.png", :trialID 134, :participantID 56, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-DynamicMap_Trial-82_Team-na_Member-39_Vers-1.png", :trialID 82, :participantID 39, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-StaticMap_Trial-61_Team-na_Member-32_Vers-1.png", :trialID 61, :participantID 32, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-223_Team-na_Member-86_Vers-1.png", :trialID 223, :participantID 86, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageNoSignal_CondWin-FalconEasy-StaticMap_Trial-71_Team-na_Member-35_Vers-1.png", :trialID 71, :participantID 35, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconMed-StaticMap_Trial-106_Team-na_Member-47_Vers-1.png", :trialID 106, :participantID 47, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-152_Team-na_Member-62_Vers-1.png", :trialID 152, :participantID 62, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "PretrialMap_CondBtwn-TriageSignal_CondWin-FalconHard-StaticMap_Trial-89_Team-na_Member-41_Vers-1.png", :trialID 89, :participantID 41, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-254_Team-na_Member-96_Vers-1.png", :trialID 254, :participantID 96, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconMed-StaticMap_Trial-178_Team-na_Member-71_Vers-1.png", :trialID 178, :participantID 71, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-TriageSignal_CondWin-FalconEasy-DynamicMap_Trial-125_Team-na_Member-53_Vers-1.png", :trialID 125, :participantID 53, :plan [[:Lobby]], :unvisitedAreas []} 
{:fileName "HSRData_PretrialMap_CondBtwn-NoTriageNoSignal_CondWin-FalconHard-StaticMap_Trial-246_Team-na_Member-93_Vers-1.png", :trialID 246, :participantID 93, :plan [[:Lobby]], :unvisitedAreas []} 