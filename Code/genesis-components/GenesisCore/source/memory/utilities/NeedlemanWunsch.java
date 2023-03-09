package memory.utilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
/**
 *  Does the Needleman-Wunsch algorithm to optimally
 *  pair up two lists of Things. <p>
 *  
 *  Read more: http://en.wikipedia.org/wiki/Needleman-Wunsch_algorithm
 *  
 *  @author sglidden
 *  
 */
public class NeedlemanWunsch {
	/**
	 * Finds the optimal pairing between Things in two lists, preserving
	 * list order.
	 * 
	 * @param l1 Thing List
	 * @param l2 Thing List
	 * @return Map Things in l1 to Things l2, optimally paired.
	 */
	public static Map<Entity, Entity> pair(List<Entity> l1, List<Entity> l2) {
		Map<Entity, Entity> mapping = new HashMap<Entity, Entity>();
		
		double d = 0;		// gap cost
		
		double[][] sm = generateSimilarityMatrix(l1, l2);
		// fill the f matrix, which is apparently what the matrix is called.
		double[][] f = new double[l1.size()+1][l2.size()+1];
		for (int i=0; i<l1.size(); i++) {
			f[i][0] = d*i;
		}
		for (int j=0; j<l2.size(); j++) {
			f[0][j] = d*j;
		}
		// first fill f matrix
		for (int i=1; i<l1.size()+1; i++) {
			for (int j=1; j<l2.size()+1; j++) {
				double choice1 = f[i-1][j-1] + sm[i-1][j-1];
				double choice2 = f[i-1][j] + d;
				double choice3 = f[i][j-1] + d;
				// go with best choice
				double best1 = Math.max(choice1, choice2);
				f[i][j] = Math.max(best1, choice3);
			}
		}
		
//		System.out.println("SM: ");
//		printMatrix(sm);
//		
//		System.out.println("F matrix: ");
//		printMatrix(f);
		
		// now, figure out how to get the best score
//		List<Thing> a = new ArrayList<Thing>();
//		List<Thing> b = new ArrayList<Thing>();
		
		int i = l1.size();
		int j = l2.size();
	
		while (i > 0 && j > 0) {
			double score = f[i][j];
			double scoreDiag = f[i-1][j-1];
//			double scoreUp = f[i][j-1];
			double scoreLeft = f[i-1][j];
			if (score == scoreDiag + sm[i-1][j-1]) {
//				a.add(0, l1.get(i-1));
//				b.add(0, l2.get(j-1));
				mapping.put(l1.get(i-1), l2.get(j-1));
				i -= 1;
				j -= 1;
			}
			else if (score == scoreLeft + d) {
//				a.add(0, l1.get(i-1));
//				b.add(0, null);
				i -= 1;
			}
			else {
//				a.add(0, null);
//				b.add(0, l2.get(j-1));
				j -= 1;
			}
		}
//		// fill out uneven length lists
		while (i>0) {
//			a.add(0, l1.get(i-1));
//			b.add(0, null);
			i -= 1;
		}
		while (j>0) {
//			a.add(0, null);
//			b.add(0, l2.get(j-1));
			j -= 1;
		}
		return mapping;
	}
	
	// generates a similarity matrix that we will use to optimally match up elements
	private static double[][] generateSimilarityMatrix(List<Entity> l1, List<Entity> l2) {
		double[][] m = new double[l1.size()][l2.size()];
		for (int i=0; i<l1.size(); i++) {
			for (int j=0; j<l2.size(); j++) {
				// uses (1-distance)^2 to fill similarity matrix
				// Note that this is somewhat arbitrary
				double temp = 1-Distances.distance(l1.get(i), l2.get(j));
				m[i][j] = temp*temp;
			}
		}
		return m;
	}
	
	private static void printMatrix(double[][] arr) {
		for (int i=0; i<arr.length; i++) {
			if (i==0) {
				// print column labels
				System.out.print("+");
				for (int j=0; j<arr[0].length; j++) {
					System.out.print("		"+j);
				}
			}
			System.out.print("\n");
			System.out.print(i+"		");
			for (int j=0; j<arr[0].length; j++) {
				Double val = arr[i][j];
				String output = val.toString();
				if (output.length()>6) {
					output = output.substring(0, 6);
				}
				System.out.print(output+ "		");
			}
		}
		System.out.println("");
		System.out.println("");
	}
}
