package utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is intended to provide convenience and formatting utilities for strings.
 * 
 * @author Keith Bonawitz
 */
public class StringUtils {
	
	/**
	 * PrettyPrint formatter for system objects.  Currently only modifies the 
	 *   formatting of a Set to be a vertical output rather than a horizontal
	 *   output.
	 */	
	public static String prettyPrint(Object o) {
		if (o instanceof Set) {
			Set s = (Set) o;
			boolean firstEntry = true;
			String accum = "[ ";
			Iterator iEntries = s.iterator();
			while (iEntries.hasNext()) {
				Object element = (Object) iEntries.next();
				String elString = prettyPrint(element);
				if (firstEntry) {
					accum = accum + indent(elString, 2, 0) + ",\n";										
					firstEntry = false;
				} else {
					accum = accum + indent(elString, 2) + ",\n";
				}
			}
			accum = accum + "]";	
			return accum;
		} else if (o instanceof Map) {
			Map m = (Map) o;
			String accum = "[ ";
			boolean firstEntry = true;
			Iterator iEntries = m.entrySet().iterator();
			while (iEntries.hasNext()) {
				Map.Entry entry = (Map.Entry) iEntries.next();
				String keyString = prettyPrint(entry.getKey());
				String elString = indentWithHeader(prettyPrint(entry.getValue()), keyString + " => ");
				if (firstEntry) {
					accum = accum + indent(elString, 2, 0) + "\n";
					firstEntry = false;
				} else {
					accum = accum + indent(elString, 2) + "\n";
				}
			}
			accum = accum + "]";
			return accum;			
		} else if (o instanceof List) {
			List l = (List) o;
			String accum = "[ ";
			boolean firstEntry = true;
			Iterator iL = l.iterator();
			while (iL.hasNext()) {
				Object item = iL.next();
				String elString = prettyPrint(item);
				if (firstEntry) {
					accum = accum + indent(elString, 2, 0) + "\n";
					firstEntry = false;
				} else {
					accum = accum + indent(elString, 2) + "\n";
				}
			}
			accum = accum + "]";
			return accum;			
			
		} else if (o instanceof Pair) {
			PairLISP p = (PairLISP) o;
			String accum = "(";
			accum = accum + indent(prettyPrint(p.car()), 2, 1) + ",\n";
			accum = accum + indent(prettyPrint(p.cdr()), 2) + "\n";
			accum = accum + ")";
			return accum;
		} else if (o instanceof int[]) {
			int[] a = (int[]) o;
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			for (int i=0; i < a.length; i++) {
				buffer.append(a[i]);
				if (i < a.length - 1) buffer.append(",");				
			}
			buffer.append("]");
			return buffer.toString();
		} else {
			return o.toString();
		}
	}
	
	
	/**
	 * prepends each line of the string toIndent with a number of spaces
	 *   specified by indentSize.  
	 * 
	 * @param toIndent is the string to be indented
	 * @param indentSize the number of space to prepend
	 * @return a string with the indentations added
	 * 
	 * @see indent(String toIndent, String indentString)
	 */
	public static String indent(String toIndent, int indentSize) {
		String indentString = repeat(" ", indentSize);
		return indent(toIndent, indentString, indentString, "", true, false);
	}
	
	public static String repeat(String toRepeat, int repCount) {
		String accum = "";
		for (int i=0; i < repCount; i++) {
			accum = accum + toRepeat;
		}
		return accum;
	}
	
	public static String indentWithHeader(String toIndent, String header) {
		int indentSize = tailLength(header);
		return header + indent(toIndent, indentSize, 0);
	}
	
	/**
	 * prepends each line of the string toIndent with a number of spaces
	 *   specified by indentSize, but allows a special indentation for the
	 *   first line.
	 * 
	 * @param toIndent is the string to be indented
	 * @param indentSize the number of space to prepend
	 * @param firstLineIndentSize overrides indentSize for the first line
	 * @return a string with the indentations added
	 * 
	 * @see indent(String toIndent, String indentString)
	 */
	public static String indent(String toIndent, int indentSize, int firstLineIndentSize) {
		String indentString = repeat(" ", indentSize);
		String firstLineIndentString = repeat(" ", firstLineIndentSize);
		return indent(toIndent, indentString, firstLineIndentString, "", true, false);
	}
	

	private static String indentHelper(String whitespace, String text, String indentString, String eolIndent, boolean afterLeadingWhitespace, boolean indentBlankLines) {
		if ((! indentBlankLines) && (text.equals(""))) { 
			return whitespace + text;
		}
		
		if (afterLeadingWhitespace) {
			return whitespace + indentString + text + eolIndent;
		} else {
			return indentString + whitespace + text + eolIndent;
		}
	}
	
	/**
	 * prepends each line of the string toIndent with the string indentString.
	 *   the indentString will be added ex
	 * 
	 * @param toIndent is the string to be indented
	 * @param indentString is the string to prepend to each line
	 * @param firstLineIndentString overrides indentString for only the first line
	 * @param afterLeadingWhitespace determines whether the indentation string is added before or after any leading whitespace on the line.
	 * @param indentBlankLines determines whether the indentString is prepended to blank lines
	 * @return a string with the indentations added
	 * @see indent(String toIndent, int indentSize)
	 */
	public static String indent(String toIndent, String indentString, String firstLineIndentString, String eolIndent, boolean afterLeadingWhitespace, boolean indentBlankLines) {
		// WARNING: Profiling indicates that this may be HORRIBLY inefficient.
		// 0.52 seconds / invocation.
		// consider at LEAST converting to string buffers instead of String
		// concatenation.  may need to rewrite altogether. 
		String indented ="";
		StringTokenizer st = new StringTokenizer(toIndent, "\n\r\t ", true);
	
		String leadingWhitespace = "";
		String text = "";
		String useIndentString = firstLineIndentString;
		
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			
			if (token.equals("\n") || token.equals("\r")) {
				// newline
				indented = 
					indented 
					+ indentHelper(leadingWhitespace, text, useIndentString, eolIndent, afterLeadingWhitespace, indentBlankLines) 
					+ token;
				leadingWhitespace = "";
				text = "";
				useIndentString = indentString;
			} else if (token.equals("\t") || token.equals(" ")) {
				// whitespace
				if (text.equals("")) {
					leadingWhitespace = leadingWhitespace + token;
				} else {
					text = text + token;
				}
			} else {
				// text
				text = text + token;
			}
		}

		// save the last line, if it doesn't end in a newline character
		if (! (leadingWhitespace.equals("") && text.equals(""))) {
			indented = 
				indented
				+ indentHelper(leadingWhitespace, text, useIndentString, eolIndent, afterLeadingWhitespace, indentBlankLines);
		}
		
		return indented;		
	}
	
	/**
	 * Takes the input string and splits it at all the newline characters (\n and \r).
	 * @return a List containing each line in its own String
	 */
	public static List getLines(String toBeSplit) {
		String lines[]=toBeSplit.split("\n\r|\n|\r");
		List results = Arrays.asList(lines);
		return results;
	}
	
	protected static DecimalFormat twoDigitFormatter = null;
	public static String formatDurationMilliseconds(long milliseconds) {
		if (twoDigitFormatter == null) twoDigitFormatter= new DecimalFormat("00");
		String sign = "";
		if (milliseconds < 0) {
			sign = "-";
			milliseconds = -milliseconds;
		}
		
		if (milliseconds < 1000) {
			return sign + milliseconds + " ms";
		}
		
		if (milliseconds < 10000) {
			return sign + (((double) milliseconds) / 1000.0) + " s";
		}
		
		long seconds = milliseconds / 1000;
		if (seconds < 60) {
			return sign + milliseconds / 1000 + " s";			
		}
		
		long minutes = seconds / 60;
		long secRemain = seconds - minutes * 60;
		long hours = minutes / 60;
		long minuteRemain = minutes - hours * 60;
		long days = hours / 24;
		long hoursRemain = hours - 24 * days;
		
		String daysString="";
		if (days > 0) {
			daysString = days + " days ";
		}
		return daysString 
			+ twoDigitFormatter.format(hoursRemain) 
			+ ":" + twoDigitFormatter.format(minuteRemain) 
			+ ":" + twoDigitFormatter.format(secRemain);
	}
	
	public static void main(String args[]) {
		System.out.println(getLines("Hello.  How\n are\n\ryou \ron\r\nthis \n\n\r\nfine day?"));
	}
	
	/**
	 * gets the length of the last line of a multi line string.
	 * @param s
	 * @return
	 */
	public static int tailLength(String s) {
		List lines = getLines(s);
		String line = (String) lines.get(lines.size()-1);
		return line.length();
	}


	public static String join(Object toJoin[], String glue) {
		return join(Arrays.asList(toJoin), glue);
	}
	
	public static String join(double[] toJoin, String glue) {
	    Double[] doubles = new Double[toJoin.length];
	    for(int i = 0; i < toJoin.length; i++){
	        doubles[i] = new Double(toJoin[i]);
	    }
		return join(doubles, glue);
	}
	

		/**
	 * @param accum
	 * @param string
	 * @return
	 */
	public static String join(Collection toJoin, String glue) {
		StringBuffer accum = new StringBuffer();
		Iterator iToJoin = toJoin.iterator();
		Object joinee;
		while (iToJoin.hasNext()) {
			joinee = iToJoin.next();
			accum.append(joinee.toString());
			if (iToJoin.hasNext()){accum.append(glue);}
		}
		return accum.toString();
	}
	
	public static String table(String table[][], String justification, int firstDataRow, int firstDataColumn) {		
		StringBuffer result = new StringBuffer();
		
		// calculate table dimensions
		int nRows = table.length;
		int nColumns;
		if (nRows == 0) {
			nColumns = 0;
		} else {
			nColumns = table[0].length;
		}
		
		assert nColumns <= justification.length() : "Not enough justification information provided";
		
		// calculate column widths
		int columnWidths[] = new int[nColumns];
		for (int iColumn = 0; iColumn < nColumns; iColumn++) {
			columnWidths[iColumn] = 0;
			for (int iRow = 0; iRow < nRows; iRow++) {
				String cell = table[iRow][iColumn];
				columnWidths[iColumn] = Math.max(cell.length(), columnWidths[iColumn]);
			}
		}
		
		for (int iRow = 0; iRow <= nRows; iRow++) {
			
			// first, check to see if we need to insert the horizontal table border
			if (iRow == firstDataRow) {
				for (int iColumn = 0; iColumn <= nColumns; iColumn++) {
					if (iColumn == firstDataColumn) {
						result.append("+-");					
					}
					
					if (iColumn == nColumns) continue;
					result.append(repeat("-", columnWidths[iColumn]+1));
				}
				result.append("\n");
			}
			if (iRow == nRows) continue;
			
			// now, work column by column inserting the data
			for (int iColumn = 0; iColumn <= nColumns; iColumn++) {
				
				// first check whether we need to insert the vertical table border
				if (iColumn == firstDataColumn) {
					result.append("| ");					
				}				
				if (iColumn == nColumns) continue;
				
				// insert the data, justified appropriately
				String cell = table[iRow][iColumn];
				int underfill = columnWidths[iColumn] - cell.length();

				char justify = justification.charAt(iColumn);
				
				if (justify == 'r') {
					result.append(repeat(" ", underfill));
					result.append(cell);
				} else if (justify == 'c') {
					int half = underfill / 2;
					result.append(repeat(" ", half));
					result.append(cell);
					result.append(repeat(" ", underfill - half));
				} else if (justify == 'l') {
					result.append(cell);
					result.append(repeat(" ", underfill));
				}		
				result.append(" ");
			}
			result.append("\n");
		}
		
		return result.toString();
	}


    /**
     * Test to determine if a particular type is in an array of types.
     */
    public static boolean testType(String s, String[] a) {
        for (int i = 0; i < a.length; ++i) {
            if (s.equalsIgnoreCase(a[i])) {
                return true;
            }
        }
        return false;
    }
    
    /** Takes a string and an integer, and returns the string padded
     * with spaces until the length of the result is equal to the integer.
     * @author M.A. Finlayson
     * @since Feb 21, 2005; JDK 1.4.2
     */
    public static String convertToFixedWidth(String string, int width){
        StringBuffer result = new StringBuffer();
        result.append(string);
        int space = width - result.length();
        if(space > 0){
            for(int i = 0; i < space; i++){
                result.append(" ");
            }
        }
        return result.toString();
    }


    public static String systemInReadln() {
        BufferedReader stdin = new BufferedReader(new InputStreamReader (System.in)); 
        try {
            return stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
//	/**
//	 * 
//	 * @param table a map, keyed by the elements of rows.  Each entry is a map, keyed by the elements of columns.  
//	 * @param rows
//	 * @param columns
//	 * @param defaultString
//	 * @return
//	 */
//	public static String table(Map table, List rows, List columns, boolean verticalColumnLabels, String defaultString) {
//		StringBuffer accum = new StringBuffer();
//
//		IdentityHashMap columnWidths = new IdentityHashMap();
//		int columnLabelWidth = 0;
//		int rowLabelWidth = 0;
//		
//		// compute the width for each column label
//		Iterator iColumns = columns.iterator();
//		while (iColumns.hasNext()) {
//			Object columnKey = (Object) iColumns.next();
//			columnLabelWidth = Math.max(columnLabelWidth, columnKey.toString().length());
//			
//			if (verticalColumnLabels) {
//				columnWidths.put(columnKey, new Integer(1));
//			} else {
//				columnWidths.put(columnKey, new Integer(columnKey.toString().length()));				
//			}
//		}
//		
//		// compute the widths for each column, and the row labels
//		Iterator iRows = rows.iterator();
//		while (iRows.hasNext()) {
//			Object rowKey = (Object) iRows.next();
//			rowLabelWidth = Math.max(rowLabelWidth, rowKey.toString().length());
//			
//			Map rowMap = (Map) table.get(rowKey);
//			iColumns = columns.iterator();
//			while (iColumns.hasNext()) {
//				Object columnKey = (Object) iColumns.next();
//				
//				Object cell = rowMap.get(columnKey);
//				String cellString;
//				if (cell == null) {
//					cellString = defaultString;
//				} else {
//					cellString = cell.toString();
//				}
//				
//				int oldWidth= ((Integer) columnWidths.get(columnKey)).intValue();
//				int newWidth = Math.max(oldWidth, cellString.length());
//				columnWidths.put(columnKey, new Integer(newWidth));
//			}
//		}
//
//		int tabelWidth = rowLabelWidth;
//		iColumns = columns.iterator();
//		while (iColumns.hasNext()) {
//			Object columnKey = (Object) iColumns.next();
//			tabelWidth += ((Integer) columnWidths.get(columnKey)).intValue() + 1;
//		}
//		
//		// draw the column labels
//		String rowLabelMargin =StringUtils.repeat(" ", rowLabelWidth);	
//		if (verticalColumnLabels) {
//			for (int i=0; i < columnLabelWidth; i++) {
//				accum.append(rowLabelMargin);
//				
//				iColumns = columns.iterator();
//				while (iColumns.hasNext()) {
//					Object columnKey = (Object) iColumns.next();
//					String columnKeyString = columnKey.toString();
//					int columnKeyWidth = columnKeyString.length();
//					int columnKeyPadding = columnLabelWidth - columnKeyWidth;
//					if (i < columnKeyPadding) {
//						accum.append(" ");						
//					} else {
//						accum.append(columnKeyString.charAt(i-columnKeyPadding));
//					}
//					
//					int columnWidth = ((Integer) columnWidths.get(columnKey)).intValue();
//					accum.append(repeat(" ", columnWidth));
//				}
//				accum.append("\n");
//			}			
//		} else {
//			accum.append(rowLabelMargin).append("   ").append(join(columns, " ")).append("\n");
//		}
//		accum.append(rowLabelMargin).append(" +-").append(repeat("-", tabelWidth - rowLabelWidth)).append("\n");
//		
//		// draw the rows
//		iRows = rows.iterator();
//		while (iRows.hasNext()) {
//			Object rowKey = (Object) iRows.next();
//			String rowLabel = rowKey.toString();
//			
//			accum.append(repeat(" ", rowLabelWidth-rowLabel.length()))
//				 .append(rowLabel)
//				 .append(" | ");
//			
//			Map rowMap = (Map) table.get(rowKey);
//			iColumns = columns.iterator();
//			while (iColumns.hasNext()) {
//				Object columnKey = (Object) iColumns.next();
//				
//				Object cell = rowMap.get(columnKey);
//				String cellString;
//				if (cell == null) {
//					cellString = defaultString;
//				} else {
//					cellString = cell.toString();
//				}
//				
//				int columnWidth = ((Integer) columnWidths.get(columnKey)).intValue();
//				accum.append(cellString)
//					 .append(repeat(" ", columnWidth-cellString.length()))
//					 .append(" ");
//			}
//			
//			accum.append("\n");
//		}
//		
//		return accum.toString();
//	}
}
