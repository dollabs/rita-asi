# My predictions

### My strategies

Left or Right:
  - When player is at left of a corridor, I predict he will turn left; when player is at right of a corridor, I predict player will turn right
  - In rooms, player would explore left side first when player can see more things on the left and explore right side first when player can see more things on the right
  - When I don't know player will turn left or right, I always choose left
 
Exploration preferences:
  - At beginning of a game, player tend to explore smaller spaces first
  - Players always want to explore new area (open doors, clear up dirt) at the beginning
  - When player already sees a door/dirt, player tend to directly explore rather than look arround and then explore

Picking up chests:
  - When player hasn't given up any green chests before, always assume they will pick up green chest when they see one
  - When player begin to give up green chest, always assume they will give up green chest if not at the end game

End games:
  - At end games, player would tend to focus on one mission
  - Assume player would take any immediate reward
  - Go to the nearest unexplored area

Other strategies:
  - when player done sth, he/she is likely to do that again
  - when player already see a newly exposed door/dirt but hesitated, player probably will not choose to explore that door/dirt

### My results

  - Bad at predicting which house player will go into at map 5 (too many seemingly equivalent choices)
  - Bad at predicting player 6's end game actions
  - Otherwise pretty good

# Observers: 

### For all observers in general
  - Player's behavior tend to change at end games, or become less rational and regulized, thus it is hard for observers to predict player actions in this case
  - It is hard for observers to perdict which door/dirt player will choose to explore when there are multiple totally new doors and/or dirts in sight.
    - Notice that prediction accuracy is very low (for both player6 and player10) on middle to end part of maze 5. This is because it is basically asking 'which new door/dirt/direction do you think player will explore' without giving observers much relevant information.  
  - Observers always think rationally, but players sometime don't, so observers tend to make wrong predictions on the same set of questions (when player is not behaving as rationally). 

### Different types of observers

  - Some tend to analyze player's overall strategy throughout the game and apply their observations to later maze's predictions, e.g. player's room search sequence & player's behavior difference in first and second half of the game
  - Some tend to notice player's preference on local rewards, e.g. door or dirt, left or right, rather than player's overall strategy.
  - Some observer immediately change their prediction strategy after making wrong predictions and notice the player is thinking in a different way, some will stick to their choices for a few more rounds before they switch strategy.
  - Some observers always take the player as a rational agent, so they can make good predictions when the player is acting rational. But those observers will predict very wrong when the player is acting rather random
  Comparatively, some observers will make random predictions or avoid the most 'rational' choice when they notice the player might not be acting systematically, so their prediction accuracy is not too low when players are acting randomly. 
