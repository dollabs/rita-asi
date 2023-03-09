# Local Knowledge Base

All the files exist in folder `corpora/stories/Knowledge/`

## Common commonsense knowledge

```
Insert file Relations knowledge.
Insert file Estonia commonsense knowledge.
Insert file Sellout knowledge.
Insert file Core knowledge.
```

## Relations knowledge

```
James is a person.  George is a person.  Mary is a person.  Elizabeth is a person.

James is George's relation because James is George's brother.
James is George's relation because James is George's father.
James is George's relation because James is George's son.
Mary is George's relation because Mary is George's sister.
Mary is George's relation because Mary is George's mother.
Mary is George's relation because Mary is George's daughter.
James is George's relation because James is George's child.
Mary is George's relation because Mary is George's wife.
George is Mary's relation because George is Mary's husband.

Mary is Elizabeth's sister because Elizabeth is Mary's sister.

xx is a person. yy is a person. zz is a person.

If xx is yy's wife, then yy is xx's relation.
If xx is yy's wife, then xx is yy's relation.
If xx is yy's husband, then yy is xx's relation.
If xx is yy's husband, then xx is yy's relation.

If xx is yy's relation and zz harms yy then zz harms xx.
```


## Estonia commonsense knowledge

```
xx is an entity.
England, Germany, and France are countries.

If England harms France then England angers France.
If England attacks an artifact and France owns the artifact then England attacks France.
If England attacks an army unit and France owns the army unit then England attacks France.
If England attacks France then England harms France.

If England harmed France and France is Germany's ally, then England harmed Germany.
If xx is my ally, then xx is my friend.
If England harmed France and France is my friend, then England harmed me.

If England insults France, then England harms France.

If England harms France, then France wants to harm England.

If England damages France's artifact, France owns France's artifact, and France believes France's artifact is valuable, then England harms France.

England believed artifact is valuable because England built artifact.
England may build artifact because England believed artifact is valuable.

England owns England's artifact because England built England's artifact.

England wants to damage France's artifact because England wanted to harm France and because France believes France's artifact is valuable.

England damaged France's artifact because England wants to damage France's artifact and because someone damaged France's artifact.

If England wants to damage France's artifact and someone attacked France's artifact, then England attacked France's artifact.

England became happy because England harmed France and because England wanted to harm France.

England wants to harm France because France angers England.

England may want to harm France because France does not respect England and because France angers England.

France harmed England because France spurned England.

England may fight France because France spurned England.
England may fight France because France insulted England.

England became unhappy because France damaged England's artifact and because England owns artifact.
England became unhappy because France harmed England.
England became happy because England owns artifact and artifact is valuable.


//////// Additional commonsense knowledge needed to handle Georgia case

ww is an entity. xx is an entity. yy is an entity. zz is an entity.

// Part of
If zz damages yy and yy is part of xx, then zz damages xx.
zz wants to damage yy because zz wants to damage xx and yy is part of xx.

// Knowledge about allies.
ww is an ally of xx because xx supported ww.
xx harmed yy because xx tried to control ww and ww is an ally of yy.

// Knowledge linking belief in importance to belief in value
If xx believes its artifact is important, then xx believes its artifact is valuable.

// Knowledge about parts
If zz attacks yy and yy is part of xx, then zz attacks xx.
If zz damages yy and yy is part of xx, then zz damages xx.
zz wants to attack yy because zz wants to attack xx and yy is part of xx.
zz wants to damage yy because zz wants to damage xx and yy is part of xx.
If zz wants to damage yy, then zz wants to attack yy.

// Knowledge about attacks
If xx attacks yy's zz and yy owns yy's zz, then xx harms yy.

//////// End of new additional knowledge.
```

## Sellout

```
// Additional commonsense knowledge for Lost in translation

// yy assists xx because xx helps yy.

If zz is an enemy of xx and yy is a friend of zz and yy assists xx, then yy angers zz.
If zz is an enemy of xx and yy assists xx, then yy angers zz.
If yy angers zz, then zz may harm yy.
If xx is an ally of yy then xx is yy's friend.
If I am an ally of yy then I am yy's friend.
If xx helps yy then yy may assist xx.
```

## Core

```
WW, XX, YY, and ZZ are entities.

// Reasons to kill.
// YY may kill WW because YY is crazy and YY likes WW.
// WW may want to kill YY because WW is angry at YY.

// Friends and relatives
If YY harmed XX and XX is WW's friend, then YY harmed WW.
If YY is the wife of XX, then XX is the husband of YY.
If YY harmed XX and XX is WW's husband, then YY harmed WW.
If YY harmed XX and XX is WW's wife, then YY harmed WW.
// Generator won't work so well on this
// If YY is the wife of XX, then YY is a relative of XX.

If xx is an enemy of zz and yy is an enemy of zz then xx is a friend of yy.
If xx is an enemy of zz and yy is an enemy of zz then yy is a friend of xx.

// Succession.
If XX is godfather and WW is XX's successor and XX becomes dead, then WW becomes godfather.
If XX is king and WW is XX's successor and XX becomes dead, then WW becomes king.
// ZZ becomes the queen because XX becomes the king and ZZ is XX's wife.
YY becomes happy because YY becomes the king and YY wants to become the king.
YY becomes happy because YY became the queen and YY wants to become the queen.

// Wants
//YY becomes happy because YY wants an action and an action occurs.

// Harm.
If YY harms WW, then WW becomes unhappy.
If YY harms WW then YY angers WW.

// Murder killing, and harming
If xx kills yy, then yy becomes dead.
If xx becomes dead, then xx becomes incapacitated.

// Pause.
YY harms WW because YY kills WW.
YY harms WW because YY attacks WW.
YY harms WW because YY fights WW.
YY may attack WW because WW harms YY.
YY may fight WW because WW attacks YY.

// Proxy
if xx asks yy to kill zz and yy kills zz, then xx kills zz.
if xx asks yy to murder zz and yy murder zz, then xx murders zz because xx asks yy to murder zz.

// Greed
ZZ may want to become the queen because zz is greedy.

// Persuasion
// If ZZ wants an action, then ZZ may persuade YY to commit the action.
// If ZZ persuades YY to act, then YY acts.

// Coercion
If an entity forces an action, then the action occurs.
// Ok, its awkward, but it works
// If an entity persuades an action to occur, then the action occurs.

xx is a person.

// Miscellaneous explanations
If xx becomes distraught, xx may kill xx.

// Censors
Dead is a kind of property.
If YY becomes dead, then YY cannot become happy.
// If YY becomes dead and XX is an entity, then XX cannot harm YY.
If YY becomes dead and XX is a person, then XX cannot anger YY.
If YY becomes dead, then YY cannot become unhappy.
If YY becomes dead and XX is an entity, then XX cannot anger YY.
// If YY murders WW, then YY cannot anger WW.
// If YY kills WW, then YY cannot anger WW.

// Persuasion
XX may persuade YY to attack ZZ because XX wants to harm ZZ.
XX may persuade YY to murder ZZ because XX wants to murder ZZ.
XX may persuade YY to murder ZZ because XX wants ZZ to become dead.
If XX persuades YY to attack ZZ, then YY attacks ZZ.
If XX persuades YY to murder ZZ, then YY murders ZZ.
If XX wants YY to die, then XX wants YY to become dead.

// Harm
// If YY attacks ZZ, then YY harms ZZ.
If XX persuades YY to attack ZZ, then XX harms ZZ.
If XX angers ZZ, then ZZ may want to harm XX.
If xx harms yy, then yy may persuade zz to attack xx.
```

## General reflective

```
xx is an entity.
yy is an entity.
zz is an entity.

/*
Don't want this in core knowledge because screw up Macbeth two-culture example

Start description of "Revenge".
xx is an entity.
yy is a entity.
xx's harming yy leads to yy's harming xx.
xx must not equal yy.
The end.
*/

Start description of "Pyrrhic victory".
xx is an entity.
vv is an action.
xx's wanting vv leads to xx's becoming happy.
xx's wanting vv leads to xx's becoming incapacitated.
xx becomes incapacitated after xx becomes happy.
The end.

/*
Start description of "Pyrrhic victory".
xx is an entity.
vv is an action.
yy is an entity.
xx's performing vv leads to xx's becoming happy.
xx's performing vv leads to yy's incapacitating xx.
yy incapacitates xx after xx becomes happy.
The end.
*/

Start description of "Mistake because harmed".
xx is an entity.
yy is an entity.
aa is an action.
xx's wanting aa leads to yy's harming xx.
The end.

Start description of "Mistake because harmed".
xx is an entity.
yy is an entity.
xx's insulting yy leads to yy's harming xx.
The end.

Start description of "Mistake because unhappy".
xx is an entity.
aa is an action.
xx's wanting aa leads to xx's becoming unhappy.
The end.

Start description of "Suicide".
xx is an entity.
xx kills xx.
The end.

Start description of "Success".
xx is an entity.
aa is an action.
xx's wanting aa leads to aa.
aa leads to xx's becoming happy.
The end.

Start description of "Regicide".
xx is an entity.
yy is a king.
xx murders yy.
The end.

Start description of "Answered prayer".
xx is an entity.
aa is an action.
xx's wanting aa leads to aa.
The end.

/*
Part of Macbeth culture demo, leave out

Start description of "Insane violence".
xx is an entity.
yy is a entity.
xx's being not sane leads to xx's killing yy.
The end.
*/

Start description of "Insane Suicide".
xx is an entity.
xx's becoming crazy leads to xx's killing xx.
The end.

Start description of "Retribution".
xx is an entity.
yy is an entity.
zz's assisting xx leads to yy's harming zz.
The end.

Start description of "Sell out".
xx is an entity.
yy is a entity.
zz is a entity.
yy is an enemy of zz.
yy help xx.
I am a friend of yy.
zz's helping xx leads to xx's angering yy.
The end.

Start description of "Misguided retaliation".
xx is an entity.
yy is a entity.
xx is my friend.
xx's angering yy leads to yy's harming xx.
The end.

Start description of "Teaching a lesson".
xx is an entity.
yy is a entity.
yy is my friend.
xx's angering yy leads to yy's harming xx.
The end.
```
