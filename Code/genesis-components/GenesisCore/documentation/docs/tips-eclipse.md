# Tips on Debugging in Eclipse

## Mark.say()

For easier debugging, we do not use ordinary print statements in genesis.

`Mark.say()`, our fancy print statements print on the Eclipse console with a clicable pointer to the place in the code wher the fancy print statement lies.

Hence:
```
 // No good, cannot easily find the location of the printing statement.
 System.out.println("Hello" + " " + "world");

 // Great, one click takes you to the location of the printing statement.
 Mark.say("Hello", "world");
```
