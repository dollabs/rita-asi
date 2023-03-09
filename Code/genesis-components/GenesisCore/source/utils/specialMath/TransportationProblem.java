package utils.specialMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author adk
 * provides some helpful math tricks, notably the Hungarian Algorithm for solving linear assignment
 * problems and the Earth Mover's Distance, a popular machine-vision metric (not yet implemented)
 * 
 * These are indispensible in comparing Bundles of threads, which is itself essential to the matching /
 * replacement problem I've encountered (deciding which remembered tree is a good match for an input tree,
 * and deciding how to modify the representation of the memory to reflect the perceived event)
 *
 */
public class TransportationProblem {
	private static final double EPSILON = 0.000000001;
	public static Tableau doHungarian(Tableau input){
		input = input.clone();
		assert(input.getRowLength() == input.getColLength());
		hung1(input);
		return input;
	}
	// many comments below are respectfully pilfered from 
	// David Doty's webpage: http://www.public.iastate.edu/~ddoty/HungarianAlgorithm.html
	private static void hung1(Tableau t){
		// For each row of the matrix, find the smallest element and
		// subtract it from every element in its row.  Go to Step 2.
		int dim = t.getColLength();
		for(int i=0;i<dim;i++){
			double smallest = -1; // negative costs will never happen
			for (int j=0; j<dim;j++){
				assert(t.get(i,j)>=0); //mmm... yeah.
				if(smallest < 0 || t.get(i, j)<smallest) 
					smallest = t.get(i, j);
			}
			for(int j = 0;j<dim;j++){
				t.set(i, j, t.get(i, j)-smallest);
			}
		}
		hung2(t,dim);
	}
	private static void hung2(Tableau t,int dim){
		//Find a zero (Z) in the resulting matrix.  If there is no
		//starred zero in its row or column, star Z. Repeat for each
		//element in the matrix. Go to Step 3.
		HashMap<Integer,Boolean> rowHash = new HashMap<Integer,Boolean>();
		HashMap<Integer,Boolean> colHash = new HashMap<Integer,Boolean>();
		for(int i=0;i<dim;i++){
			if(rowHash.containsKey(i)) continue;
			for(int j=0;j<dim;j++){
				if(colHash.containsKey(j)) continue;
				boolean rowOK = !t.rowHasStarredCell(i);
				boolean colOK = !t.colHasStarredCell(j);
				if(rowOK && colOK){
					if(t.get(i,j)<EPSILON){
						t.star(i, j);
						colHash.put(j, true);
						rowHash.put(i, true);
					}
				}
				else if(rowOK){
					colHash.put(j, true);
				}
				else if(colOK){
					rowHash.put(i, true);
				}
				else {
					rowHash.put(i, true);
					colHash.put(j, true);
				}
			}
		}
		hung3(t,dim);
	}
	private static void hung3(Tableau t,int dim){
		//Cover each column containing a starred zero.  If K columns
		//are covered, the starred zeros describe a complete set of 
		//unique assignments.  In this case, Go to DONE, otherwise, 
		//Go to Step 4.
		int markCount=0;
		for(int j=0;j<dim;j++){
			if(t.colHasStarredCell(j)){
				markCount++;
				t.markCol(j);
			}
		}
		if(markCount==dim)return;//done!
		hung4(t,dim); //javac ought to be smart enough to convert tail recursion into iteration that doesn't
		              //burden the stack unnecessarily.  Is it?
	}
	private static void hung4(Tableau t,int dim){
		//Find a noncovered zero and prime it.  If there is no starred 
		//zero in the row containing this primed zero, Go to Step 5.  
		//Otherwise, cover this row and uncover the column containing 
		//the starred zero. Continue in this manner until there are no 
		//uncovered zeros left. Save the smallest uncovered value and 
		//Go to Step 6.
		int coords[] = null;
		do {
			coords = findUnmarkedZero(t,dim);
			if(coords!= null){
				t.prime(coords[0], coords[1]);
				int starredCoords[] = findStarredCoords(coords[0],t,dim);
				if(starredCoords !=  null){
					t.markRow(coords[0]);
					t.unmarkCol(starredCoords[1]);
				}
				else {
					hung5(t,dim,coords[0],coords[1]);
					return;
				}
			}
		} while(coords != null);
		//System.out.print(t);
		hung6(t,dim,getSmallestUnmarkedVal(t,dim));
	}
	private static double getSmallestUnmarkedVal(Tableau t,int dim){
		double smallest = Double.POSITIVE_INFINITY;
		for(int i=0;i<dim;i++){
			if(t.rowIsMarked(i))continue;
			for (int j=0;j<dim;j++){
				if(t.colIsMarked(j))continue;
				smallest = Math.min(smallest,t.get(i, j));
			}
		}
		return smallest;
	}
	private static int[] findStarredCoords(int rowNum,Tableau t,int dim){
		for(int j=0;j<dim;j++){
			if(t.isStarred(rowNum,j)){
				return new int[]{rowNum,j};
			}
		}
		return null;
	}
	private static int[] findUnmarkedZero(Tableau t,int dim){
		for(int i=0;i<dim;i++){
			if(t.rowIsMarked(i))continue;
			for(int j=0;j<dim;j++){
				if(t.colIsMarked(j))continue;
				if(t.get(i,j)<EPSILON){
					return new int[]{i,j};
				}
			}
		}
		return null;
	}
	private static void hung5(Tableau t,int dim,int zeroI, int zeroJ){
		//Construct a series of alternating primed and starred zeros as 
		//follows.  Let Z0 represent the uncovered primed zero found in 
		//Step 4.  Let Z1 denote the starred zero in the column of Z0 
		//(if any). Let Z2 denote the primed zero in the row of Z1 (there 
		//will always be one).  Continue until the series terminates at a 
		//primed zero that has no starred zero in its column.  Unstar each 
		//starred zero of the series, star each primed zero of the series,
		//erase all primes and uncover every line in the matrix.  Return 
		//to Step 3.
		List<int[]> series = new ArrayList<int[]>();
		int k=0;
		int last[] = new int[]{zeroI,zeroJ};
		do{
			series.add(last);
			if(k%2==0){
				last = getStarredCoordsInCol(last[1],t,dim);
			} else {
				last = getPrimedCoordsInRow(last[0],t,dim);
			}
			k++;
		} while(last != null);
		//unstar each starred zero in the series
		for(int coords[]:series){
			if(t.isStarred(coords[0], coords[1]))t.unstar(coords[0], coords[1]);
		}
		//star each primed
		for(int coords[]:series){
			if(t.isPrimed(coords[0], coords[1]))t.star(coords[0], coords[1]);
		}
		//erase all primes
		for(int i=0;i<dim;i++){
			for(int j=0;j<dim;j++){
				t.unprime(i,j);
			}
		}
		//clear all marked rows & cols
		t.clearAllMarks();
		hung3(t,dim);
	}
	private static int[] getStarredCoordsInCol(int j,Tableau t,int dim){
		for (int i = 0;i<dim;i++){
			if(t.isStarred(i, j))return new int[]{i,j};
		}
		return null;
	}
	private static int[] getPrimedCoordsInRow(int i,Tableau t,int dim){
		for (int j = 0;j<dim;j++){
			if(t.isPrimed(i, j))return new int[]{i,j};
		}
		return null;
	}
	private static void hung6(Tableau t,int dim,double smallest){
		//System.out.println("smallest: "+smallest);
		//Add the value found in Step 4 to every element of each covered
		//row, and subtract it from every element of each uncovered column.  
		//Return to Step 4 without altering any stars, primes, or covered 
		//lines.
		for(int i=0;i<dim;i++){
			boolean rm = t.rowIsMarked(i);
			for(int j=0;j<dim;j++){
				boolean cnm = !t.colIsMarked(j);
				if(rm){
					if(!cnm){
						t.set(i,j,t.get(i, j)+smallest);
					}
				} else if(cnm){
					t.set(i, j, t.get(i, j)-smallest);
				}
			}
		}
		//System.out.print(t);
		hung4(t,dim);
	}
	
	public static void main(String args[]){
		Tableau t = new Tableau(3,3);
		t.set(0,0, 1);
		t.set(0,1, 3);
		t.set(0,2, 4);
		t.set(1,0, 6);
		t.set(1,1, 5);
		t.set(1,2, 7);
		t.set(2,0, 10);
		t.set(2,1, 11);
		t.set(2,2, 42);
		System.out.println(t);
		System.out.println(doHungarian(t));
		t.set(0,0, 1);
		t.set(0,1, 2);
		t.set(0,2, 3);
		t.set(1,0, 3);
		t.set(1,1, 5);
		t.set(1,2, 6);
		t.set(2,0, 7);
		t.set(2,1, 8);
		t.set(2,2, 9);
		System.out.println(t);
		System.out.println(doHungarian(t));
	}
}
