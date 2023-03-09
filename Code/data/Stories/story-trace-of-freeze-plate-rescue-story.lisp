

(Story:  RESCUE-FROZEN-USER
         (Actors: Person2 Person1)
         (Tools: medkit)
         (obstacles: freeze_tile)
         (vacinities: building)
         (Actions: MOVE-AROUND STEP-ON CALL-FOR-HELP STATES-OWN-CONDITION REQUEST-SOMEBODY-TO-COME 
                   REQUEST-SOMEBODY-TO-UNFREEZE ARRIVE UNFREEZE WORKS)

         (Initial-state: 
          (IS-IN-VACINITY Person1 building)
          (IS-IN-CONDITION Person1 MOBILE)
          )

         (Action MOVE-AROUND: 
                 Next-State AFTER-MOVE-AROUND 
                 Assertions:
                 (IS-MOVING-AROUND Person1 building)
                 (NOT (IS-STANDING-ON Person1 freeze_tile)))

         (Action STEP-ON: 
                 Next-State AFTER-STEP-ON 
                 Assertions:
                 (IS-STANDING-ON Person1 freeze_tile)
                 (IS-IN-VACINITY Person1 freeze_tile)
                 (IS-IN-CONDITION Person1 NEEDING-HELP)
                 (NOT (IS-IN-CONDITION Person1 MOBILE))
                 (NOT (IS-WORKING Person1)))

         (Action CALL-FOR-HELP: 
                 Next-State AFTER-CALL-FOR-HELP 
                 Assertions:
                 (IS-CALLING-FOR Person1 HELP))

         (Action STATES-OWN-CONDITION: 
                 Next-State AFTER-STATEMENT 
                 Assertions:
                 (STATES-CONDITION Person1 NEEDING-HELP))

         (Action REQUEST-SOMEBODY-TO-COME: 
                 Next-State AFTER-REQUEST-COME 
                 Assertions:
                 (REQUESTS-SOMEONE-TO-COME Person1 ANYBODY))

         (Action REQUEST-SOMEBODY-TO-UNFREEZE: 
                 Next-State AFTER-REQUEST-UNFREEZE 
                 Assertions:
                 (IS-IN-CONDITION Person2 MOBILE)
                 (IS-IN-POSSESSION-OF Person2 medkit)
                 (REQUESTS-SOMEONE-TO-UNFREEZE Person1 ANYBODY))

         (Action ARRIVE: 
                 Next-State AFTER-ARRIVE 
                 Assertions:
                 (IS-IN-VACINITY Person2 freeze_tile))

         (Action UNFREEZE: 
                 Next-State AFTER-UNFREEZE 
                 Assertions:
                 (IS-IN-CONDITION Person1 MOBILE))

         (Action WORKS: 
                 Next-State AFTER-RESUMES-WORKING 
                 Assertions:
                 (IS-IN-CONDITION Person1 WORKING))
         (Depedencies
          ((IN-STATE (IS-IN-VACINITY Person1 building) INITIAL)
           PREMISE)
          ((IN-STATE (IS-IN-CONDITION Person1 MOBILE) INITIAL)
           PREMISE)
          ((NOT (IN-STATE (IS-STANDING-ON Person1 freeze_tile) AFTER-MOVE-AROUND))
           (OBJECT-TYPE-OF freeze_tile FREEZE-TILE)
           (OBJECT-TYPE-OF Person1 PERSON)
           (IN-STATE (IS-MOVING-AROUND Person1 building) AFTER-MOVE-AROUND))
          ((IN-STATE (IS-MOVING-AROUND Person1 building) AFTER-MOVE-AROUND)
           (ACTION-TAKEN MOVE-AROUND BEFORE-MOVE-AROUND AFTER-MOVE-AROUND))
          ((NOT (IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-STEP-ON))
           (OBJECT-TYPE-OF freeze_tile FREEZE-TILE)
           (OBJECT-TYPE-OF Person1 PERSON)
           (IN-STATE (IS-STANDING-ON Person1 freeze_tile) AFTER-STEP-ON))
          ((NOT (IN-STATE (IS-WORKING Person1) AFTER-STEP-ON))
           (OBJECT-TYPE-OF Person1 PERSON)
           (NOT (IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-STEP-ON)))
          ((IN-STATE (IS-STANDING-ON Person1 freeze_tile) AFTER-STEP-ON)
           (ACTION-TAKEN STEP-ON BEFORE-STEP-ON AFTER-STEP-ON))
          ((IN-STATE (IS-IN-VACINITY Person1 freeze_tile) AFTER-STEP-ON)
           (OBJECT-TYPE-OF freeze_tile FREEZE-TILE)
           (OBJECT-TYPE-OF Person1 PERSON)
           (IN-STATE (IS-STANDING-ON Person1 freeze_tile) AFTER-STEP-ON))
          ((IN-STATE (IS-IN-CONDITION Person1 NEEDING-HELP) AFTER-STEP-ON)
           (OBJECT-TYPE-OF Person1 PERSON)
           (NOT (IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-STEP-ON)))
          ((IN-STATE (IS-CALLING-FOR Person1 HELP) AFTER-CALL-FOR-HELP)
           (ACTION-TAKEN CALL-FOR-HELP BEFORE-CALL-FOR-HELP AFTER-CALL-FOR-HELP))
          ((IN-STATE (STATES-CONDITION Person1 NEEDING-HELP) AFTER-STATEMENT)
           (ACTION-TAKEN STATES-OWN-CONDITION BEFORE-STATEMENT AFTER-STATEMENT))
          ((IN-STATE (REQUESTS-SOMEONE-TO-COME Person1 ANYBODY) AFTER-REQUEST-COME)
           (ACTION-TAKEN REQUEST-SOMEBODY-TO-COME BEFORE-REQUEST-COME AFTER-REQUEST-COME))
          ((IN-STATE (IS-IN-CONDITION Person2 MOBILE) AFTER-REQUEST-UNFREEZE)
           PREMISE)
          ((IN-STATE (IS-IN-POSSESSION-OF Person2 medkit) AFTER-REQUEST-UNFREEZE)
           PREMISE)
          ((IN-STATE (REQUESTS-SOMEONE-TO-UNFREEZE Person1 ANYBODY) AFTER-REQUEST-UNFREEZE)
           (ACTION-TAKEN REQUEST-SOMEBODY-TO-UNFREEZE BEFORE-REQUEST-UNFREEZE AFTER-REQUEST-UNFREEZE))
          ((IN-STATE (IS-IN-VACINITY Person2 freeze_tile) AFTER-ARRIVE)
           (ACTION-TAKEN ARRIVE BEFORE-ARRIVE AFTER-ARRIVE))
          ((IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-UNFREEZE)
           (ACTION-TAKEN UNFREEZE BEFORE-UNFREEZE AFTER-UNFREEZE))
          ((IN-STATE (IS-IN-CONDITION Person1 WORKING) AFTER-RESUMES-WORKING)
           (ACTION-TAKEN WORKS BEFORE-RESUMES-WORKING AFTER-RESUMES-WORKING))
          ((ACTION-TAKEN MOVE-AROUND BEFORE-MOVE-AROUND AFTER-MOVE-AROUND)
           (IN-STATE (IS-IN-VACINITY Person1 building) INITIAL))
          ((ACTION-TAKEN STEP-ON BEFORE-STEP-ON AFTER-STEP-ON)
           (NOT (IN-STATE (IS-STANDING-ON Person1 freeze_tile) AFTER-MOVE-AROUND)))
          ((ACTION-TAKEN CALL-FOR-HELP BEFORE-CALL-FOR-HELP AFTER-CALL-FOR-HELP)
           (IN-STATE (IS-IN-CONDITION Person1 NEEDING-HELP) AFTER-STEP-ON))
          ((ACTION-TAKEN STATES-OWN-CONDITION BEFORE-STATEMENT AFTER-STATEMENT)
           (IN-STATE (IS-IN-CONDITION Person1 NEEDING-HELP) AFTER-STEP-ON))
          ((ACTION-TAKEN REQUEST-SOMEBODY-TO-COME BEFORE-REQUEST-COME AFTER-REQUEST-COME)
           (IN-STATE (IS-CALLING-FOR Person1 HELP) AFTER-CALL-FOR-HELP))
          ((ACTION-TAKEN REQUEST-SOMEBODY-TO-UNFREEZE BEFORE-REQUEST-UNFREEZE AFTER-REQUEST-UNFREEZE)
           (IN-STATE (IS-CALLING-FOR Person1 HELP) AFTER-CALL-FOR-HELP))
          ((ACTION-TAKEN ARRIVE BEFORE-ARRIVE AFTER-ARRIVE)
           (IN-STATE (IS-IN-CONDITION Person2 MOBILE) AFTER-REQUEST-UNFREEZE))
          ((ACTION-TAKEN UNFREEZE BEFORE-UNFREEZE AFTER-UNFREEZE)
           (NOT (IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-STEP-ON)))
          ((ACTION-TAKEN WORKS BEFORE-RESUMES-WORKING AFTER-RESUMES-WORKING)
           (IN-STATE (IS-IN-CONDITION Person1 MOBILE) AFTER-UNFREEZE))
          )
         )