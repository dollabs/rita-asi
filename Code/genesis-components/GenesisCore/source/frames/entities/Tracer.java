//// Java run
//// Created: 23 January 2004

package frames.entities;

import java.util.*;
import java.awt.*;
import utils.logging.*;
import utils.logging.Logger;

public class Tracer {

 private static HashMap<String, Color> colors = new HashMap<String, Color>();

 /*
  * Marks entire structure below t for special coloring by viewers.
  */
 public static void trace (Entity t, String color) {
  t.addType(color, "tracers");
  if (t.functionP()) {
   Function d = (Function)t;
   trace (((Function)t).getSubject(), color);
  }
  if (t.relationP()) {
   trace (((Relation)t).getSubject(), color);
   trace (((Relation)t).getObject(), color);
  }
  if (t.sequenceP()) {
   Vector v = ((Sequence)t).getElements();
   for (int i = 0; i < v.size(); ++i) {
    trace(((Entity)(v.get(i))), color);
   }
  }
 }

 /*
  * Marks entire structure below t for special coloring by viewers.  Uses tracer
  * to mark, but uses color.
  */
 public static void trace (Entity t, String tracer, Color color) {
  Object current = colors.get(tracer);
  if (current != null) {
   Logger.fine("Tracer", "Changed color for tracer " + tracer);
  }
  colors.put(tracer, color);
  t.addType(tracer, "tracers");
  if (t.functionP()) {
   Function d = (Function)t;
   trace (((Function)t).getSubject(), tracer);
  }
  if (t.relationP()) {
   trace (((Relation)t).getSubject(), tracer);
   trace (((Relation)t).getObject(), tracer);
  }
  if (t.sequenceP()) {
   Vector v = ((Sequence)t).getElements();
   for (int i = 0; i < v.size(); ++i) {
    trace(((Entity)(v.get(i))), tracer);
   }
  }
 }

 /*
  * Remvoes all tracers.
  */
 public static void untrace (Entity t) {
  t.getBundle().removeThread(t.getThread("tracers"));
  if (t.functionP()) {
   Function d = (Function)t;
   untrace (((Function)t).getSubject());
  }
  if (t.relationP()) {
   untrace (((Relation)t).getSubject());
   untrace (((Relation)t).getObject());
  }
  if (t.sequenceP()) {
   Vector v = ((Sequence)t).getElements();
   for (int i = 0; i < v.size(); ++i) {
   untrace(((Entity)(v.get(i))));
   }
  }
 }

 /*
  * Removes specified tracer.
  */
 public static void untrace(Entity t, String tracer){
  Thread thread = t.getThread("tracers");
  if(thread == null){return;}
  thread.remove(tracer);
  if (t.functionP()) {
   Function d = (Function)t;
    untrace (((Function)t).getSubject(), tracer);
  }
  else if (t.relationP()) {
   untrace (((Relation)t).getSubject(), tracer);
   untrace (((Relation)t).getObject(), tracer);
  }
  else if (t.sequenceP()) {
   Vector v = ((Sequence)t).getElements();
   for (int i = 0; i < v.size(); ++i) {
    untrace(((Entity)(v.get(i))), tracer);
   }
  }
 }

 /*
  * Gets color associated with the tracer.  Generally called by a viewer.
  */
 public static Color getColor (Entity thing) {
  Vector tracers = thing.getThread("tracers");
  if (tracers != null) {
   String tracer = (String)(tracers.lastElement());
   Object value = colors.get(tracer);
   if (value != null) {return (Color) value;}
   if (tracer.equalsIgnoreCase("tracers")){return null;}
   else if (tracer.equalsIgnoreCase("black")) {return Color.BLACK;}
   else if (tracer.equalsIgnoreCase("blue")) {return Color.BLUE;}
   else if (tracer.equalsIgnoreCase("cyan")) {return Color.CYAN;}
   else if (tracer.equalsIgnoreCase("darkgray")) {return Color.DARK_GRAY;}
   else if (tracer.equalsIgnoreCase("gray")) {return Color.GRAY;}
   else if (tracer.equalsIgnoreCase("green")) {return Color.GREEN;}
   else if (tracer.equalsIgnoreCase("lightgray")) {return Color.LIGHT_GRAY;}
   else if (tracer.equalsIgnoreCase("magenta")) {return Color.MAGENTA;}
   else if (tracer.equalsIgnoreCase("orange")) {return Color.ORANGE;}
   else if (tracer.equalsIgnoreCase("pink")) {return Color.PINK;}
   else if (tracer.equalsIgnoreCase("red")) {return Color.RED;}
   else if (tracer.equalsIgnoreCase("white")) {return Color.WHITE;}
   else if (tracer.equalsIgnoreCase("yellow")) {return Color.YELLOW;}
   else {
    Logger.warning("ThingToViwerTranslator", "Tracer is not a known tracer");
    return Color.PINK;
   }
  }
  return null;
 }

 public static void main (String [] ignore) {
  Logger.info("Tracer", "Hello");
  Logger.info("Tracer", Color.RED.toString());
  Tracer.trace(new Entity(), "foo", Color.red);
  Tracer.trace(new Entity(), "foo", Color.red);
 }

}
