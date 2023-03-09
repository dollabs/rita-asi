package frames.entities;

import utils.logging.Logger;

/**
 * A utility class that provides a unique string of the form "-n" via a
 * static method.
 * @author Patrick Winston
 */
public class NameGenerator {
 protected static int memory = 0;
 /**
  * Get a unique string of the form "-n".
  */
 public static String getNewName() {
  String result = "-" + memory;
  increment();
  return result;
 }
 
 /* @author M.A.Finlayson
  * @since Dec 6, 2003 */
 public static int getNewID() {
 	int result = memory;
 	increment();
 	return result;
 }
 
 private static void increment(){
	fine("Incrementing memory from " + memory);
 	++memory;
 	
 }
 
 public static void clearNameMemory() {
  memory = 0;
 }
 public static void setNameMemory(int i) {
  memory = Math.max(memory, i);
 }

 // this is to make sure that when loading a data-file, the names
 // get incremented correctly.
 public static void registerLoadedThing(String suffix) {
  Integer instancenum = new Integer(suffix);
  int inum = -instancenum.intValue();
  if(inum+1>memory) memory=inum+1;
 }
 
 //	Debugging section, added MAF.7.Jan.04
 public static final String LOGGER_GROUP = "thing";
 public static final String LOGGER_INSTANCE = "NameGenerator";
 public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;
	
 protected static void finest(Object s) {
  Logger.getLogger(LOGGER).finest(LOGGER_INSTANCE + ": " + s);
 }
 protected static void finer(Object s) {
  Logger.getLogger(LOGGER).finer(LOGGER_INSTANCE + ": " + s);
 }
 protected static void fine(Object s) {
  Logger.getLogger(LOGGER).fine(LOGGER_INSTANCE + ": " + s);
 }
 protected static void config(Object s) {
  Logger.getLogger(LOGGER).config(LOGGER_INSTANCE + ": " + s);
 }
 protected static void info(Object s) {
  Logger.getLogger(LOGGER).info(LOGGER_INSTANCE + ": " + s);
 }
 protected static void warning(Object s) {
  Logger.getLogger(LOGGER).warning(LOGGER_INSTANCE + ": " + s);
 }
 protected static void severe(Object s) {
  Logger.getLogger(LOGGER).severe(LOGGER_INSTANCE + ": " + s);
 }

public static int extractIDFromName(String name) {
    if (name == null) {
        return -1;
    } // Added to avoid nullPointerException --MAF.18.Feb.04
    int index = name.lastIndexOf('-');
    if (index >= 0) {
        return Integer.parseInt(name.substring(index + 1));
    }
    return -1;
}

public static String extractSuffixFromName(String name) {
    if (name == null) {
        return null;
    } // Added to avoid nullPointerException --MAF.18.Feb.04
    int index = name.lastIndexOf('-');
    if (index >= 0) {
        return (name.substring(index));
    }
    return null;
}
}

