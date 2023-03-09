/* Filename: Aligner.java
 * Creator: M.A. Finlayson
 * Format: Java 2 v1.6.0
 * Date created: Feb 11, 2010
 * 
 * Modified by: Matthew Fay
 * Update Date: Mar 6, 2011
 */
package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.Mark;
import matthewFay.Utilities.Pair;

public abstract class NWAligner<A, B> {
	
	protected float midGapPenalty = -1.00f;
	protected float tailGapPenalty = -0.99f;
	
	public void setGapPenalty(float penalty){
		midGapPenalty = penalty;
	}
	
	/**
	 * TODO: Write comment
	 *
	 * @param aList
	 * @param bList
	 * @return
	 * @since nil.ucm.indications2.ui 1.0.0
	 */
	public Alignment<A, B> align(List<A> as, List<B> bs){		
		float[][] fMatrix = computeFMatrix(as, bs);
		
		Alignment<A, B> result = new Alignment<A,B>();
		
		int i = as.size();
		int j = bs.size();
		float score, scoreDiag, scoreUp, scoreLeft, scoreTotal;
		
		scoreTotal = 0;
		
//		Mark.say(Arrays.deepToString(fMatrix));
		
		while(i > 0 && j > 0){
			score = fMatrix[i][j];
			scoreDiag = fMatrix[i-1][j-1];
			scoreUp = fMatrix[i][j-1];
			scoreLeft = fMatrix[i-1][j];
			
			float sim = sim(as.get(i-1), bs.get(j-1));
			if(score == scoreLeft + midGapPenalty && j!=bs.size() || score == scoreLeft + tailGapPenalty && j==bs.size()){
				if(j==bs.size())
					scoreTotal += tailGapPenalty;
				else
					scoreTotal += midGapPenalty;
				result.addFirst(new Pair<A,B>(as.get(i-1), null));
				i--;
			}
			else if(score == scoreUp + midGapPenalty && i!=as.size()  || score == scoreUp + tailGapPenalty && i==as.size()){
				if(i==as.size())
					scoreTotal += tailGapPenalty;
				else
					scoreTotal += midGapPenalty;
				result.addFirst(new Pair<A,B>(null, bs.get(j-1)));
				j--;
			}
			else if(score == scoreDiag + sim){
				//Should Cache the sims//
				scoreTotal += sim(as.get(i-1), bs.get(j-1));
				result.addFirst(new Pair<A,B>(as.get(i-1), bs.get(j-1)));
				i--;
				j--;
			}
		}
		
		while(i > 0){
			scoreTotal += tailGapPenalty;
			result.addFirst(new Pair<A,B>(as.get(i-1), null));
			i--;
		}
		
		while(j > 0){
			scoreTotal += tailGapPenalty;
			result.addFirst(new Pair<A,B>(null, bs.get(j-1)));
			j--;
		}
		
		result.score = scoreTotal;
		return result;
	}
	
	/**
	 * TODO: Write comment
	 *
	 * @param as
	 * @param bs
	 * @return
	 * @since nil.ucm.indications2.ui 1.0.0
	 */
	public float[][] computeFMatrix(List<A> as, List<B> bs){
		
		// allocate F matrix
		float[][] fMatrix = new float[as.size()+1][bs.size()+1];
		
		// initialize matrix
		for(int i = 0; i <= as.size(); i++) fMatrix[i][0] = tailGapPenalty*i;
		for(int j = 0; j <= bs.size(); j++) fMatrix[0][j] = tailGapPenalty*j;
		
		float c1, c2, c3;
		for(int i = 1; i <= as.size(); i++){
			for(int j = 1; j <= bs.size(); j++){
				float sim = sim(as.get(i-1), bs.get(j-1));
				c1 = fMatrix[i-1][j-1] + sim;
				
				if(j == bs.size()) {
					c2 = fMatrix[i-1][j] + tailGapPenalty; 
				} else {
					c2 = fMatrix[i-1][j] + midGapPenalty;
				}
				
				if(i == as.size()) {
					c3 = fMatrix[i][j-1] + tailGapPenalty;
				} else {
					c3 = fMatrix[i][j-1] + midGapPenalty;
				}
				
				fMatrix[i][j] = Math.max(c1, Math.max(c2, c3));
			}
		}	
		return fMatrix;
	}
	
	/**
	 * TODO: Write comment
	 *
	 * @param a
	 * @param b
	 * @return
	 * @since edu.mit.story.core 1.0.0
	 */
	public abstract float sim(A a, B b);
	
	
	/**
	 * Simple test and demonstration.
	 *
	 * @param args may be <code>null</code>
	 * @since edu.mit.story.core 1.0.0
	 */
	public static void main(String[] args){
		
		// cost function is +10 for matching, 0 for not
		NWAligner<Character, Character> aligner = new NWAligner<Character, Character>(){
			public float sim(Character a, Character b) {
				return a.equals(b) ? 4 : -7;
			}
		};
		
		aligner.midGapPenalty = -7;
		
		// two strings; the second has a gap and a 
		// few different letters relative to the first
		//String strA = "AGACTAGTTAC";
		//String strB = "BGA"+"TTTAC";
		
		String strA = "AGACTAGTTAC";
		String strB = "TCGA"+"TTTAC";
		
		// create lists for alignment
		List<Character> seqA = new ArrayList<Character>(strA.length());
		for(char c : strA.toCharArray()) seqA.add(c);
		List<Character> seqB = new ArrayList<Character>(strB.length());
		for(char c : strB.toCharArray()) seqB.add(c);
		
		// align
		List<Pair<Character, Character>> result = aligner.align(seqA, seqB);
		
		// print to screen
		StringBuilder seqAalign = new StringBuilder(result.size());
		StringBuilder seqBalign = new StringBuilder(result.size());
		for(Pair<Character, Character> pair : result){
			seqAalign.append(pair.a == null ? '-' : pair.a);
			seqBalign.append(pair.b == null ? '-' : pair.b);
		}
		System.out.println(seqAalign);
		System.out.println(seqBalign);
		
	}
	
}
