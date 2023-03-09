// // Java run
// // Created: 17 January 2004

package utils.logging;

import java.util.Vector;

/*
 * This class provides a subset of the capabilities provided ordinarily by the
 * <tt>java.util.logging</tt> package. Here, the distinguishing feature is that
 * it works, whereas java.util.logging does not. <p>This documentation is
 * written like a press release; the first few lines give you the basic
 * capability; the rest add much coolness. <p>First, of course, you need to
 * import the package: <pre> import logging.*; </pre> instead of the official
 * package: <pre> import java.util.logging.*; </pre> When they fix the offical
 * logging package, you can switch back by just changing the import statement,
 * because all method names and all usage is the same. <p>The following, with
 * nothing else, is like a print statement. That is, <pre>
 * Logger.getLogger("mylogger").info("Hello World"); </pre> is equivalent to,
 * <pre> Sytem.out.println("Hello World"); </pre> The advantage of using the
 * logger is that you can install a lot of statements for debugging and then
 * arrange to have them ignored when others use your code, but they remain, when
 * you need to debug again. Here are the essential ideas: <ul> <li> Each logger
 * has a name, such as myLogger. <li> Each logger has a level. <li> Loggers have
 * various print methods, such as <tt>severe, warning, info, fine, finer</tt>
 * and <tt>finest</tt>. </ul> These methods print, or fail to print, according
 * to the level you set for the logger. </ul> Suppose, for example, that you
 * have a logger named <tt>myLogger</tt>. You create it the first time you call
 * <tt>Logger.getLogger("myLogger")</tt>. You fetch that same logger each
 * subsequent time you call <tt>Logger.getLogger("myLogger")</tt>. <p> You set
 * your logger's level as follows: <pre>
 * Logger.getLogger("myLogger").setLevel(Level.INFO); </pre> Where for INFO, you
 * can substitute various levels such as <tt>SEVERE, WARNING, INFO, FINE, FINER,
 * or FINEST</tt>. The level you set determines which methods actually print.
 * For example, if you set the logger to <tt>WARNING</tt>, only <tt>severe</tt>
 * and <tt>warning</tt> print. If you set the level to FINE, then <tt>severe,
 * warning, info</tt> and <tt>fine</tt> print. <p> So, typical usage is to use
 * <tt>fine</tt> as your basic method, realizing that the default level is INFO,
 * which means your call won't print unless you set the level to <tt>FINE</tt>
 * in your main method, which you do when you want to debug. Somebody else who
 * uses your code, but not your main method, will not be annoyed by your
 * debugging messages. <p>Now, just one more thing. If you do not set the level
 * of a logger, it will try to inherit its parent's level. What is its parent?
 * The parent-defining mechanism looks just like the package naming scheme. That
 * is, a logger named <tt>myLogger</tt> is the parent of the logger named
 * <tt>myLogger.myHack</tt>. Hence, in the bridge system, there is a logger
 * named <tt>bridge</tt>, which is the parent of the logger
 * <tt>bridge.bridgespeak</tt>, which is in turn the parent of a bunch of more
 * specific loggers. You can turn them on and off in large or small groups using
 * the parent/child heirarchy. . . Late breaking news: as of 17 January 2004 I
 * implemented a few static methods so as to simplify logging.
 * Logger.info(<i>this</i>, <i>message</i>), for example, identifies the
 * <i>this</i> class and prints the <i>message</i>. All these use the
 * "debugging" logger.
 */

// Example code follows:
//
// //Debugging section
// public static final String LOGGER_GROUP = "insert_package_here";
// public static final String LOGGER_INSTANCE = "insert_class_here";
// public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;
//
// public Logger getLogger(){
// return Logger.getLogger(LOGGER);
// }
//
// protected static void finest(Object s) {
// Logger.getLogger(LOGGER).finest(LOGGER_INSTANCE + ": " + s);
// }
// protected static void finer(Object s) {
// Logger.getLogger(LOGGER).finer(LOGGER_INSTANCE + ": " + s);
// }
// protected static void fine(Object s) {
// Logger.getLogger(LOGGER).fine(LOGGER_INSTANCE + ": " + s);
// }
// protected static void config(Object s) {
// Logger.getLogger(LOGGER).config(LOGGER_INSTANCE + ": " + s);
// }
// protected static void info(Object s) {
// Logger.getLogger(LOGGER).info(LOGGER_INSTANCE + ": " + s);
// }
// protected static void warning(Object s) {
// Logger.getLogger(LOGGER).warning(LOGGER_INSTANCE + ": " + s);
// }
// protected static void severe(Object s) {
// Logger.getLogger(LOGGER).severe(LOGGER_INSTANCE + ": " + s);
// }

public class Logger {
	String identifier = "";

	String parentIdentifier = "";

	Level level = null;

	private static Vector<Logger> loggers = new Vector<Logger>();

	private Logger(String s) {
		identifier = s;
		setParentString(identifier);
	}

	public static Logger getLogger(String name) {
		for (int i = 0; i < loggers.size(); ++i) {
			Logger logger = (Logger) (loggers.elementAt(i));
			if (logger.identifier.equalsIgnoreCase(name)) {
				return logger;
			}
		}
		Logger newLogger = new Logger(name);
		newLogger.setLevel(Level.OFF);
		loggers.add(newLogger);
		return (newLogger);
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public boolean isLoggable(Level level) {
		return (level.intValue() > getLevel().intValue());
	}

	public void log(Level level, Object s) {
		if (getLevel().intValue() > level.intValue()) {
			return;
		}
		String output = "Logger @ " + level.getName() + ": " + s.toString();
		System.out.println(output);
	}

	public void severe(Object s) {
		if (getLevel().intValue() > Level.SEVERE.intValue()) {
			return;
		}
		String output = "Logger @ severe: " + s.toString();
		System.err.println(output);
	}

	public void warning(Object s) {
		if (getLevel().intValue() > Level.WARNING.intValue()) {
			return;
		}
		String output = "Logger @ warning: " + s.toString();
		System.err.println(output);
	}

	public void info(Object s) {
		if (getLevel().intValue() > Level.INFO.intValue()) {
			return;
		}
		String output = "Logger @ info: " + s.toString();
		System.out.println(output);
	}

	public void config(Object s) {
		if (getLevel().intValue() > Level.CONFIG.intValue()) {
			return;
		}
		String output = "Logger @ config: " + s.toString();
		System.out.println(output);
	}

	public void fine(Object s) {
		if (getLevel().intValue() > Level.FINE.intValue()) {
			return;
		}
		String output = "Logger @ fine: " + s.toString();
		System.out.println(output);
	}

	public void finer(Object s) {
		if (getLevel().intValue() > Level.FINER.intValue()) {
			return;
		}
		String output = "Logger @ finer: " + s.toString();
		System.out.println(output);
	}

	public void finest(Object s) {
		if (getLevel().intValue() > Level.FINEST.intValue()) {
			return;
		}
		String output = "Logger @ finest: " + s.toString();
		System.out.println(output);
	}

	private void setParentString(String s) {
		int index = s.lastIndexOf('.');
		if (index >= 0) {
			parentIdentifier = s.substring(0, index);
		}
	}

	private Logger getParent() {
		for (int i = 0; i < loggers.size(); ++i) {
			Logger logger = (Logger) (loggers.elementAt(i));
			if (logger.identifier.equalsIgnoreCase(parentIdentifier)) {
				return logger;
			}
		}
		return null;
	}

	private Level getLevel() {
		if (level != null) {
			return level;
		}
		Logger parent = getParent();
		if (parent != null) {
			return parent.getLevel();
		}
		return Level.INFO;
	}

	// Static convenience methods

	public static void fine(Object o, Object m) {
		String identifier = (o instanceof String) ? (String) o : o.getClass().toString();
		Logger.getLogger("debugging").fine(trimClassDescription(identifier) + m.toString());
	}

	public static void info(Object o, Object m) {
		String identifier = (o instanceof String) ? (String) o : o.getClass().toString();
		Logger.getLogger("debugging").info(trimClassDescription(identifier) + m.toString());
	}

	public static void warning(Object o, Object m) {
		String identifier = (o instanceof String) ? (String) o : o.getClass().toString();
		Logger.getLogger("debugging").warning(trimClassDescription(identifier) + m.toString());
	}

	private static String trimClassDescription(String s) {
		int i = s.indexOf(' ');
		if (i >= 0) {

			return "(" + s.substring(i + 1) + ") ";
		}
		return s.trim() + " ";
	}

	public String toString() {
		return "Logger " + identifier;
	}

	public static void main(String[] ignore) {
		System.out.println("A");
		getLogger("hello").setLevel(Level.WARNING);
		getLogger("hello.world").warning("Warning level logger triggered");
		getLogger("hello.world").info("Info level logger triggered");
		getLogger("hello.world").fine("Fine level logger triggered");
		System.out.println("B");
		getLogger("hello").setLevel(Level.INFO);
		getLogger("hello.world").warning("Warning level logger triggered");
		getLogger("hello.world").info("Info level logger triggered");
		getLogger("hello.world").fine("Fine level logger triggered");
		System.out.println("C");
		getLogger("hello").setLevel(Level.FINE);
		getLogger("hello.world").warning("Warning level logger triggered");
		getLogger("hello.world").info("Info level logger triggered");
		getLogger("hello.world").fine("Fine level logger triggered");
		System.out.println("D");
		getLogger("hello.world").setLevel(Level.WARNING);
		getLogger("hello.world").warning("Warning level logger triggered");
		getLogger("hello.world").info("Info level logger triggered");
		getLogger("hello.world").fine("Fine level logger triggered");

		Logger.info(new Object(), "Shorthand test");
		Logger.info("String message", "Shorthand test");

	}

}
