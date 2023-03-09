package utils;

import java.sql.Timestamp;
import java.util.*;

import javax.swing.AbstractButton;

import connections.*;
import connections.signals.BetterSignal;

public class Mark {

    public static Boolean USE_TIME_STAMP = false;
    public static String TIME_STAMP = "";
    
    // added by Zhutian for coloring Mark.say for easier viewing
    // install plugin by following instructions at 
    //     https://marketplace.eclipse.org/content/ansi-escape-console
    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_SAY = "\u001B[30m";
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_PURPLE = "\u001B[35m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_WHITE = "\u001B[37m";
    
    public static final String ANSI_SHOW = "\033[0m\u001B[47m";
    public static final String ANSI_BLACK = "\u001B[40m\033[37m";
    public static final String ANSI_MIT = "\u001B[41m\033[37m";
    

    public static void a(Object... objects) {
        sayLabel("A", objects);
    }

    public static void b(Object... objects) {
        sayLabel("B", objects);
    }

    public static void c(Object... objects) {
        sayLabel("C", objects);
    }

    public static void d(Object... objects) {
        sayLabel("D", objects);
    }

    public static void sayLabel(String label, Object... objects) {
        String mark = "Mark " + label + (objects.length > 0 ? ":" : "");
        List<Object> l = new ArrayList<Object>(Arrays.asList(objects));
        l.add(0, mark);
        say(l.toArray());
    }

    private static String format(List<Object> args, int first) {
        String result = ">>> ";
        for (int i = first; i < args.size(); ++i) {
            result += " " + args.get(i);
        }
//        result += " " + link();
        // modified by Zhutian for coloring Mark.say for easier viewing
        // install plugin by following instructions at 
        //     https://marketplace.eclipse.org/content/ansi-escape-console
        result += " " + ANSI_WHITE + link() + ANSI_RESET;
        return result;
    }

    // the disadvantage is that Mark cannot have a main() fcn
    private static String link() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        for (int i = 2;; i++) {
            if (st[i].getClassName() == Mark.class.getName()) {
                continue;
            }
            else {
                // return prepareLunaPointer(st[i]);
                return ": " + prepareMarsPointer(st[i]);
            }
        }
    }

    private static String prepareLunaPointer(StackTraceElement st) {
        return "(" + st.getClassName() + ".java:" + st.getLineNumber() + ")";
    }

    private static String prepareMarsPointer(StackTraceElement st) {
        String s = st.getClassName();
        int index = s.lastIndexOf('.');
        String result = st.getClassName() + "." + st.getMethodName();
        result += "(" + s.substring(index + 1) + ".java:" + st.getLineNumber() + ")";
        return result;
    }

    public static void err(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) {
                return;
            }
            else {
                System.err.println(format(args, 1));
            }
        }
        else {
            System.err.println(format(args, 0));
        }
    }

    public static void say(Object... objects) {
    	
    	if (USE_TIME_STAMP) {
        	Date date= new Date();
        	Timestamp ts = new Timestamp(date.getTime());
//        	System.out.println("Time: " + ts);
        	TIME_STAMP = ts.toString() + ": ";
    	}
    	
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) {
                return;
            }
            else {
                System.out.println(TIME_STAMP + format(args, 1));
            }
        }
        else {
            System.out.println(TIME_STAMP + format(args, 0));
        }
    }
    
    public static void red(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_RED));
        }
        else System.out.println(format_color(args, 0, ANSI_RED));
    }
    public static void green(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_GREEN));
        }
        else System.out.println(format_color(args, 0, ANSI_GREEN));
    }
    public static void yellow(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_YELLOW));
        }
        else System.out.println(format_color(args, 0, ANSI_YELLOW));
    }
    public static void blue(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_BLUE));
        }
        else System.out.println(format_color(args, 0, ANSI_BLUE));
    }
    public static void purple(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_PURPLE));
        }
        else System.out.println(format_color(args, 0, ANSI_PURPLE));
    }
    public static void cyan(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_CYAN));
        }
        else System.out.println(format_color(args, 0, ANSI_CYAN));
    }
    public static void night(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_BLACK));
        }
        else System.out.println(format_color(args, 0, ANSI_BLACK));
    }
    public static void show(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_SHOW));
        }
        else System.out.println(format_color(args, 0, ANSI_SHOW));
    }
    public static void mit(Object... objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) return;
            else System.out.println(format_color(args, 1, ANSI_MIT));
        }
        else System.out.println(format_color(args, 0, ANSI_MIT));
    }
    
    private static String format_color(List<Object> args, int first, String ansi_color) {
        String result = ansi_color + ">>> ";
        for (int i = first; i < args.size(); ++i) {
            result += " " + args.get(i);
        }
        // install plugin by following instructions at 
        //     https://marketplace.eclipse.org/content/ansi-escape-console
        result += " " + ANSI_RESET + ANSI_WHITE + link() + ANSI_RESET;
        return result;
    }
    
    

    public static void comment(Object box, Object port, Object location, Object tab, Object[] objects) {
        List<Object> args = Arrays.asList(objects);
        Object leadObject = args.get(0);
        int start = 0;
        if (leadObject instanceof Boolean) {
            start = 1;
            if (!((Boolean) leadObject)) {
                return;
            }
        }
        String message = "";
        for (int i = start; i < args.size(); ++i) {
            message += args.get(i).toString() + " ";
        }
		if (location != null) {
			Connections.getPorts((WiredBox) box).transmit((String) port, new BetterSignal((String) location, (String) tab, message + "<br/>"));
		}
		else {
			Connections.getPorts((WiredBox) box).transmit((String) port, new BetterSignal((String) tab, message + "<br/>"));
		}
    }

    public static void par(Object... objects) {
        Object leadObject = objects[0];
        if (leadObject instanceof Boolean) {
            if (!((Boolean) leadObject)) {
                return;
            }
        }

        ArrayList<Object> args = new ArrayList<>();
        args.addAll(Arrays.asList(objects));

        if (leadObject instanceof Boolean) {
            args.add(0, "\n>>> ");
            System.out.println(format(args, 1));
        }
        else {
            args.add(0, "\n>>> ");
            System.out.println(format(args, 0));
        }
    }

    // With check boxes
    // ADK: consolidated many into one vararg method, and
    // removed dependence on JCheckBox in favor of AbstractButton

    public static void say(AbstractButton b, Object... o) {
        if (b.isSelected()) {
            say(o);
        }
    }

}


