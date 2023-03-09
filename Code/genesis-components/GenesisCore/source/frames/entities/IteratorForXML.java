package frames.entities;


/**
 * Provides methods for iteratorlike operations on XML marked text.
 * Copyright 1999 Ascent Technology, Inc. All rights reserved.  Used here by
 * permission.
 * @author Patrick Winston
 */
public class IteratorForXML {
 String input;
 String start;
 String end;
 String nextResult;
 boolean test = false;
 public IteratorForXML (String i, String s, String e) {
  input = i;
  start = s;
  end = e;
  
  //System.out.println("input: " + i);
  
  next();
 }
 
 /**
  * Constructs iterator instance from string and tag.
  */
 public IteratorForXML (String i, String tag) {
  this(i, tag, tag);
 }
 /**
  * Determines if there is another bracketed string.
  */
 public boolean hasNext () {
  if (nextResult != null) {return true;} else {return false;}
 }
 /**
  * Extracts next bracketed string.
  */
 public String next () {
  String thisResult = nextResult;
  
  nextResult = Tags.untagTopLevelString(start, input);   
  
  //System.out.println("This result1 = " + thisResult);
  //System.out.println("Next result1 = " + nextResult);

  if (nextResult == null) {return thisResult;}
  
  int index = input.indexOf(nextResult) + nextResult.length();
  
  index = input.indexOf("</" + start + ">", index) + 3 + start.length();
  
  input = input.substring(index);
  
  nextResult = Tags.tag(start, nextResult);
  
  //System.out.println("This result2 = " + thisResult);
  //System.out.println("Next result2 = " + nextResult);

  return thisResult;
 }
 /**
  * Tests.
  */
 public static void main (String argv []) {
  String test = "<foo><foo>1</foo></foo> bar <foo>2</foo> baz <foo> 3";
  IteratorForXML iterator = new IteratorForXML(test, "foo");
  System.out.println(iterator.next());
  System.out.println(iterator.next());
  System.out.println(iterator.next());
  System.out.println(iterator.next());
 }
}


