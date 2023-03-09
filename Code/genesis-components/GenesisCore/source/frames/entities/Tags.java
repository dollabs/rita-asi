package frames.entities; 

import java.util.Vector;

/**
 * Provides utility methods for creating XMLlike printing.  Example:
 *  <Fly>
 *   <Bird>
 *    Bird-7
 *   </Bird>
 *   <Path>
 *    <From>
 *     <Place>
 *      <Table>
 *       Table-0
 *      </Table>
 *     </Place>
 *    </From>
 *    <To>
 *     <Place>
 *      <Tree>
 *       Tree-3
 *      </Tree>
 *     </Place>
 *    </To>
 *   </Path>
 *  </Fly>
 * 
 * Redone from scratch, 29 June 2002
 * @author Patrick Winston
 */

public class Tags {
    private static boolean CONCISE = false;
    public static boolean DEBUG = false;

    StringBuffer remainder;
    String left, right;
    String nextResult;

    /**
     * Creates tag iterator from string and tag.
     */
    public Tags (String s, String t) {
        remainder = new StringBuffer(s);
        left = "<" + t + ">";
        right = "</" + t + ">";
        next();
    }

    /**
     * Returns next result, sans marking tags; also prepares next result;
     */
    public String next() {
        String thisResult = nextResult;
        nextResult = null;
        int depth = 0;
        int anchor = remainder.indexOf(left);
        int start = anchor + 1;
        while (true) {
            int nextLeft = remainder.indexOf(left, start);
            int nextRight = remainder.indexOf(right, start);
            if (nextRight < 0) {break;}
            if (nextLeft < 0 || nextRight < nextLeft) {
                if (depth == 0) {
                    // Note new result;
                    nextResult = remainder.
                        substring(anchor + left.length(), nextRight);
                    // Remove new result from buffer
                    remainder.delete(0, nextRight + right.length());
                    // Stop iteration
                    break;
                }
                else {++depth;}
                start = nextRight + 1;
            }
            else {
                --depth;
                start = nextLeft + 1;
            }
        }
        return thisResult;
    }

    public boolean hasNext() {
        if (nextResult != null) {return true;}
        return false;
    }

    // Reading


    public static boolean hasNext (String s, String tag) {
        if (s.indexOf(tag) >= 0) {return true;}
        return false;
    }

    public static String next (String s, String tag) {
        int start = s.indexOf("<" + tag + ">");
        if (start < 0) {
            System.err.println("Bad data to Tags.next---" + 
                               tag + " not found");
        }
        return "";
    }
    /**
     * Finds specified tag and returns String from in matched pair,
     * but only if tag is at the top level.  Also shortens string by
     * diking out stuff delimited by matching tags.  This is crudely
     * coded, and should be rewritten.
     */
    public static String untagTopLevelString (String tag, String s) {
        String result = null;
        int depth = 0;
        if (s == null) {return result;}
        while (true) {
            int left = s.indexOf("<");
            int right = s.indexOf(">", left);
            if (left < 0 || right < 0) {return null;}
            String observedTag = s.substring(left + 1, right);
            if (s.charAt(left+1) == '/') {--depth;}
      
            else if (tag.equals(observedTag) && depth == 0) {
                return untagString(tag, s.substring(left));
            }
      
            else{++depth;}
      
            //System.out.println(observedTag + " " + depth);
            s = s.substring(right+1);
        }
    }
 
    /** Finds nth occurrence of specified tag. Returns null if there
     * is no such occurrence
     * @param tag The tag to be found.
     * @param s The string in which the tag is to be found.
     * @param n The number of the occurence of the tag to be found.
     * @return The string found inside the nth matched pair of tags.
     * @author M.A. Finlayson
     * @since Jan 16, 2004; JDK 1.4.2
     */
    public static String untagString(String tag, String s, int n){
        String result = null;
        if (s == null) {return result;}
        int index;
        for(int i = 0; i < n - 1; i++){
            index = s.indexOf("<" + tag + ">");
            s = s.substring(index + tag.length() + 2);
        }
        int first = s.indexOf("<" + tag + ">");
        int last = untagStringHelper(tag, s);
        if (first >= 0 && last > first) {
            result = s.substring(first + 2 + tag.length(), last).trim();
        }
        return result;
    }
 
    /**
     * Finds specified tag and returns String found inside matched
     * pair.  This needs be complex, because needs to handle embedded
     * pairs!  Tags themselves are stripped.
     */
    public static String untagString (String tag, String s) {
        String result = null;
        if (s == null) {return result;}
        int first = s.indexOf("<" + tag + ">");
        int last = untagStringHelper(tag, s);
        if (first >= 0 && last > first) {
            result = s.substring(first + 2 + tag.length(), last).trim();
        }
        return result;
    }
    public static int untagStringHelper(String tag, String s) {
        return untagStringHelper(tag, s, 0);
    }
    public static int untagStringHelper
        (String tag, String s, int initial) {
        int counter = 1;
        // No string, fail:
        if (s == null) {return -1;}
        // Check for initial tag:
        int starter = s.indexOf("<" + tag + ">", initial);
        // No initial tag, fail:
        if (starter < 0) {return -1;}
        // So start loop
        for (int index = starter + 1;;) {
            // Get final one:
            int closer = s.indexOf("</" + tag + ">", index); 
            // If none, lose:
            if (closer < 0) {return -1;}
            // Now check for another tag:
            int second = s.indexOf("<" + tag + ">", index + 1);
            // No second tag:
            if (second < 0) {
                --counter;
                index = closer + 1;
                // System.out.println("Embedding escaped"); 
            }
            // Ugh, there is an embedded tag; check it out:
            else if (second < closer) {
                ++counter;
                index = second + 1;
                // System.out.println("Embedding encountered");
            }
            // No embedded tag:
            else {
                --counter;
                index = closer + 1;
                // System.out.println("Embedding escaped");
            }
            // System.out.println("Index: " + index + ", counter: " + counter);
            // System.out.println("Stuff\n|" + s.substring(0, index) + "|\n");
            if (counter == 0) {return index - 1;}
        }
    }
    /**
     * Finds specified tag and returns boolean from matched pair.
     public static boolean untagBoolean (String tag, String s) {
     String text = untagString(tag, s);
     if (text == null) {return false;}  
     if (text.equalsIgnoreCase("true")) return true; else return false;
     }
    */
    /**
     * Finds specified tag and returns GregorianCalendar from matched pair.
     public static GregorianCalendar untagCalendar (String tag, String s) {
     String text = untagString(tag, s);
     GregorianCalendar c = new GregorianCalendar();
     try {
     Date d = format.parse(text);
     c.setTime(d);
     }
     catch (Exception e) {
     System.err.println("Could not parse " + text + " as a calendar object");
     }
     return c;
     }
    */
    /**
     * Finds specified tag and returns long from matched pair.
     public static long untagLong (String tag, String s) {
     String text = untagString(tag, s);
     if (text == null) {return 0;}  
     return Long.parseLong(text);
     }
    */
    /**
     * Finds specified tag and returns double from matched pair.
     public static double untagDouble (String tag, String s) {
     String text = untagString(tag, s);
     if (text == null) {return 0.0;}  
     return Double.parseDouble(text);
     }
    */

    // Writing

    /**
     * Tags an Object.
     */
    public static String tag (String t, Object o) {
        if (CONCISE && o == null) {return "";}
        if (o == null) {return addTag(t, "null");}
        return addTag(t, o);
    }
    /**
     * Tags a String.
     */
    public static String tag (String t, String s) {
        if (CONCISE && s.length() == 0) {return "";}
        return addTag(t, s);
    }
    /**
     * Tags a String.
     */
    public static String tagNoLine (String t, String s) {
        if (CONCISE && s.length() == 0) {return "";}
        return addTagNoLine(t, s);
    }
    /**
     * Tags a boolean.
     public static String tag (String t, boolean b) {
     if (concise && !b) {return "";}
     return addTag(t, new Boolean(b));
     }
    */
    /**
     * Tags a GregorianCalendar.

     public static String tag (String t, GregorianCalendar g) {
     return addTag(t, format.format(g.getTime()));
     }
    */
    /**
     * Tags a long.

     public static String tag (String t, long l) {
     if (concise && l == 0) {return "";}
     return addTag(t, Long.toString(l));
     }
    */
    /**
     * Tags a double.
     public static String tag (String t, double d) {
     if (concise && d == 0.0) {return "";}
     return addTag(t, Double.toString(d));
     }
    */

    /**
     * Converts vector to string representing contents.
     */
    public static String tag (Vector<?> v) {
        String result = "";
        for (int i = 0; i < v.size(); ++i) {
            result += v.elementAt(i).toString() + " ";
        }
        return result.trim();
    }


    // Private

    private static String addSpaces (String s) {
        StringBuffer b = new StringBuffer("");
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\n') {b.append("\n ");}
            else {b.append(c);}
        }
        return b.toString();
    }
    /**
     * Tags an Object.
     */
    private static String addTag (String t, Object o) {
        String middle = addSpaces(o.toString());
        if (middle.length() == 0) {return  "\n<" + t + ">" + "</" + t + ">";}
        return "\n<" + t + ">" + ((middle.charAt(0) == '\n') ? "" : "\n ") + middle + "\n</" + t + ">";
    }
    /**
     * Tags an Object.
     */
    private static String addTagNoLine (String t, Object o) {
        String middle = addSpaces(o.toString());
        if (middle.length() == 0) {return  "\n<" + t + ">" + "</" + t + ">";}
        return "\n<" + t + ">" + middle + "</" + t + ">";
    }
    /**
     * Illustrates behavior.
     */
    public static void main (String argv []) {
        Tags.CONCISE = true;
        String s = Tags.tag("thing", "foo");
        s += Tags.tag("thing", "bar");
        System.out.println(s);
        for (Tags iterator = new Tags(s, "thing"); iterator.hasNext();) {
            System.out.println(iterator.next());
        }
  
        System.out.println("Testing untagging functionality -- MAF.16.Jan.04");
        Entity t1 = new Entity("Mark");
        Entity t2 = new Entity("Steph");
        Relation r1 = new Relation("siblings", t1, t2);
        Thread d = new Thread();
        d.addType("related");
        r1.addThread(d);
        System.out.println("Relation we have is: " + r1.toString());
  
        String bundle = Tags.untagString("bundle", r1.toString());
        System.out.println("\nGetting bundle:\n\n" + bundle);
  
        String thread = Tags.untagString("thread", bundle);
        System.out.println("\nGetting thread with untagString():\n\n" + thread);
  
        thread = Tags.untagString("thread", bundle, 1);
        System.out.println("\nGetting first thread:\n\n" + thread);
  
        thread = Tags.untagString("thread", bundle, 2);
        System.out.println("\nGetting second thread:\n\n" + thread);
   
        thread = Tags.untagString("thread", bundle, 3);
        System.out.println("\nGetting third thread:\n\n" + thread);
    }
}







