# Prediction Analysis

Overall, I will use past player actions to try to predict what players will do next; essentially I am looking for patterns in their actions.

When they change their behavior, I will consider that a change in their action-making. For example, player 6 initially digs ender chests, so I will assume they will not stop digging it. However, after test 2 they stop digging it, and my justification is because they believe it takes too long. Therefore, I will now predict that they will not dig ender chests.

Below I state my overall prediction strategies for each test.

# Player 6

## Test 2

- When did not know player well, guess 
- As player did actions, learned what they would do so predicted they would repeat past actions 
- Keep in mind they are searching for chests (the reward), so they will prioritize finding chests; this helps justify actions done 
- They usually tended to go to the left. This was first a guess then it was made into theory as time went on based on past actions 
- They dug ender chests always when they saw it 
- They dug regular chests always when they saw it 
- They almost always dug dirt, but if the dirt was far away around the corner and there was a closer door, they went through the door 

## Test 3

- Predict based on the patterns learned from the past experiment, keeping in mind they may change their patterns in a different test 
- It seems that they are now skipping ender chests because it takes too long 
- They still tend to go to left 
- They usually dig dirt when they see it closeby, unless they don't have much time 

## Test 5

- Predict based on the patterns learned from the past experiment, keeping in mind they may change their patterns in a different test 
- They were typically consistent with before actions 
- Sometimes they did things differently for unknown reasons 
- They seem to have a love-hate relationship with dirt 

# Player 10

## Test 2

- They dig chests when they see it (regardless of type) 
- They seem to like digging dirt and prioritize it  
- They do not seem spatially aware and can get lost 
- They prefer left actions when faced with directions 

## Test 3

- Predict based on the patterns learned from the past experiment, keeping in mind they may change their patterns in a different test 
- They have made a lot of unpredictable actions this round that differed than before. Regardless, we learned: 
    - They usually prefer doors to dirt when they are very close together 
    - They still dig ender chests 

## Test 5

- Predict based on the patterns learned from the past experiment, keeping in mind they may change their patterns in a different test 
- Both player 6 and player 10 don't like that door that has dirt in the back and tend to skip it. Maybe a sign of the map warning?

# Overall prediction considerations

Essentially, there seems to be a few aspects that predictors will keep in mind when trying to decide how a player will act.

- Past player data. If they have done something in the past, there is a good chance they will repeat it in a similar circumstance 
- Reward function consideration. They will keep in mind that the goal is to dig chests for reward, so actions done will reflect this goal. 
- Commonsense data: 
    - If they open a door, there is a good chance they will go through it 
    - If they walk towards a chest, there is a good chance they will dig it 
    - If they walk towards door/dirt, there is a good chance they will go through it or dig it 
    - If they seem not spatially aware, they may repeat doors. Otherwise, they won't 

# Raw player prediction data

Below is my raw prediction data that shows my predictions for each video in every test, as well as a justification for why I believe they will do this particular action.

# Player 6 prediction data

## Test 2

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Go left | The player didn't see what in the hallway, so they will look around. Also, don't know this player yet so can't say for sure | YES
2 | Go through the door on the left | Closest door, and there are no chests in sight, but other doors are in sight. | YES
3 | Go left | The player went left last time. Not sure otherwise | YES
4 | Dig the dirt | The player has never seen dirt before, and I think they will dig it to see what is behind it | YES
5 | Go left | We don't know what is in the hole on the left, so the player may look at it if it’s a door | YES
6 | Dig the ender chest | See chest, dig it | YES
7 | Dig dirt | They did it last time and since there was a chest behind the first one they might check again | YES
8 | Go through the door in front | It's either the door or the hallway that seems to go down farther away. Door is closer. | YES
9 | Dig chest | See chest, dig it | YES
10 | Go to left | They usually go left when faced with multiple directions to go. Not too certain here. | YES*** (they looked at the left but the video ended before they made a decision)
11 | Go to dirt on right | It seems that they were moving to the right, so it looks like they will return to the chest they saw | NO, they moved back to the chest
12 | Go through door | It's closer, and last time there was nothing behind the dirt so it might have discouraged them from going to the dirt | YES
13 | Look at the doors at the end of the hallway since that's where they're facing | They're facing that way so they might as well look | NO they turned around the moved away
14 | Go through the door that was closed on the right (closest to them) | It's unopened and they usually go through doors unopened | YES
15 | Go into door | They just opened it | YES

## Test 3

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Dig dirt | The door leads to more places, and the dirt is close by. | YES
2 | Dig chest | The chest is super close and they always had dug chests when they saw them | YES
3 | Dig dirt | They dug the dirt when there were doors nearby before | NO, they went through the door in front (maybe discouraged last time when there wasn't a chest behind the dirt)
4 | Go through the door | Always had opened door then gone through door | NO, they went back to the dirt (maybe they changed their mind about skipping the dirt and dug it anyways)
5 | Walk behind dirt to check for chests | They always check for chests after digging dirt | YES
6 | Go through the door that was opened | They opened it, and all other doors are closed | YES
7 | Uncertain. Dig dirt? | Last time when confronted with doors and dirt they opened door then dug dirt, but this time they might just dig dirt? | YES
8 | Door on left | They usually aim to left | YES
9 | Keep digging | Uncertain. They never saw this situation before, so possibly they are curious to see what is behind the dirt. | YES
10 | Stop digging | There's too much dirt and they saw an opening with nothing there. No reward, leave | YES
11 | Open door in front | It's unopened and they haven't gone in that direction yet | YES
12 | Dig ender chest | No evidence thus far says they will skip ender chests | NO. It seems they have changed their strategy to skip ender chests, since they take too long
13 | Dig chest | See regular chest, dig it | YES
14 | Dig dirt | It's close and they usually dig it | NO they skipped it. Perhaps they are time constrained and realize dirt takes too long
15 | Go through door in front | It's the only unopened one in view | YES

## Test 5

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Go to door that they saw | Nowhere else looked promising | YES
2 | Go in door | They opened it so they always went through it | NO. Behavior change: dirt deterred them more than before, so now they will skip it cause they saw dirt
3 | Open door | They are looking at it | YES
4 | Go through the door they skipped | It's closeby and they haven't yet explored it | YES
5 | Open door | They are looking at it and it's close to them | YES
6 | Go to left where they haven't explored yet | It's an open hallway that is not explored yet | NO they return backwards, seem uncertain, then go back again
7 | Go left | They usually go left when not knowing where to go | NO, they went forward. (this might've been cause they knew the map and I am not referencing it)
8 | Open door | They are looking at it | YES
9 | Open door | They are looking at it | YES
10 | Skip the ender chest and go backwards | They didn't dig the ender chest last time | YES
11 | Open door | They are looking at it | NO, they went around and opened a different door
12 | Dig dirt | They usually dig dirt when they see it | YES
13 | Continue digging dirt | They haven't really dug 2 layers yet | YES
14 | Go to right | They haven't looked in that direction yet in the room | NO, they left the room. Possibly time constrained
15 | Go to right | They saw a chest there before | YES

# Player 10 prediction data

## Test 2

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Open door in front | Just a guess. They saw door, went in front. | YES
2 | Look left for chests | They see dirt on right, but nothing seen on left yet | YES
3 | Dig dirt | They are walking towards it and they haven't dug it before | YES
4 | Go left | They saw more stuff on the left (saw the door) | YES
5 | Go left door | They tend to go left | YES
6 | Dig ender chest | They haven't seen ender chests before and will probably dig it | YES
7 | Dig dirt | They are looking right at it and walking towards it | YES
8 | Go through door in front | They see a door and nothing else around them yet | YES
9 | Dig chest in front | See chest, dig it | YES
10 | Look left at hallway | they haven't seen what is on their left yet | NO, they walked to the dirt
11 | Dig the dirt | They walked to the dirt instead of looking left so it seems they are aiming for it | YES
12 | Go left | They haven't looked left yet | YES
13 | Dig ender chest | They have not shown signs of not digging ender chests yet | YES
14 | Exit the door in front | They are walking towards it | YES
15 | Look right | They already looked to the left. IF THEY ARE NOT SPATIALLY AWARE THEY MAY RETURN LEFT | NO they looked left instead
16 | Dig the ender chest they saw | It's the closest thing to them that they can dig | YES
17 | Walk down the hallway and turn right | They haven't gone there yet, all other doors are opened | YES
18 | It seems they are not spatially aware, so they may go through that closest door even though it is a visited room (also they didn't fully explore the visited room) | See above | YES

## Test 3

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Dig the dirt in front | They seem to enjoy digging dirt from last time | YES
2 | Dig the dirt behind them | Same reason as test 1 | NO, they went through the door; it was closer I guess?
3 | Dig the dirt in front of them | Same reason as test 1 | YES
4 | Dig the dirt | Same reason as test 1 | NO, they don't like that dirt (maybe because it shows that there is a door behind it)
5 | Go through door | They opened it | YES
6 | Dig the dirt | Test 1 reason | NO, they went through the door instead. It seems that if there is a door and dirt very close by, they choose door.
7 | Dig ender chest | No signs otherwise saying they won't | YES
8 | Dig dirt on left | Dig the dirt before leaving through the last door | NO, I should've known they would skip the dirt like last time rip
9 | Go through door | They opened it | NO, they changed their mind and dug the dirt finally
10 | Go through the door they opened | they opened it | YES
11 | Dig ender chest | They saw it and always have dug it before | YES

## Test 5

Video Number | Prediction | Justification | Correct?
--- | --- | --- | ---
1 | Go left | They tend to go left when given multiple decisions | YES
2 | Open the door | They were walking towards it | YES
3 | Go left, door closest to them | They tend to go left | YES
4 | Open the door | They were walking towards it | YES
5 | Dig ender chest | No reason to think they will stop, they haven't before | YES
6 | Dig the dirt on their left | There is no door and they seem to enjoy digging dirt | YES
7 | Go left | They usually go left | NO, they went through the door that was closer that they saw
8 | Go through door | They opened it | NO they turned around and went to the door behind instead
9 | Go through door they opened | It is open and easier to go through | NO they looked right
10 | Go through door they opened | See reason 9 | NO they skipped it then returned to where they were before, possibly another sign that they are not spatially aware
11 | Return to the door unopened | See reason 9 | NO they hate that door
12 | Dig the dirt in front of them | They went through to the room and saw dirt | YES