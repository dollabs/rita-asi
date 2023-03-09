package expert;

import java.util.*;

import utils.Mark;

/*
 * Created on Jun 29, 2015
 * @author phw
 */

public class StatisticsCalculator {

	private static void computeBoth(int l, int b, int r, int n) {
		// double ri = computeMultipleR(l, b, r, n);
		double f = computeF(l, b, r);
		// Mark.say("Rand and F for", l, b, r, n, "are", ri, f);

	}

	public static double computeR(int leftNotRight, int leftAndRight, int rightNotLeft, int neither) {
		double slowPairResult = computeSlowPairR(leftNotRight, leftAndRight, rightNotLeft, neither);
		return slowPairResult;
	}

	public static double computeR(List<Integer> computedClusters, List<Integer> groundTruthClusters) {
		// Class membership, will be 0 if out, 1 if in
		int iLeft, jLeft, iRight, jRight;
		int s00 = 0;
		// Number of pairs in different class on both left and right
		int s11 = 0;
		// Check data, should also check to be sure breakpoints are monotonically ascending
		if (computedClusters.size() != groundTruthClusters.size()) {
			Mark.err("Elements in clusters not same size!");
		}
		int total = computedClusters.size();
		// Iterate over pairs
		for (int i = 0; i < total; ++i) {
			for (int j = i + 1; j < total; ++j) {
				// Entity is in left set if between 0 and leftNotRight + leftAndRight
				iLeft = computedClusters.get(i);
				jLeft = computedClusters.get(j);
				iRight = groundTruthClusters.get(i);
				jRight = groundTruthClusters.get(j);
				// Ok, now we can tell if in same class or different
				if (iLeft == jLeft && iRight == jRight) {
					// Mark.say(i, j, " ", iLeft, jLeft, " ", iRight, jRight, "in same class on both sides");
					++s00;
				}
				else if (iLeft != jLeft && iRight != jRight) {
					// Mark.say(i, j, " ", iLeft, jLeft, " ", iRight, jRight, "in different class on both sides");
					++s11;
				}
				else {
					// Mark.say(i, j, " ", iLeft, jLeft, " ", iRight, jRight, "not consistent");
				}
			}
		}
		return 2.0 * (s00 + s11) / (total * (total - 1));
	}

	private static double computeSlowPairR(int leftNotRight, int leftAndRight, int rightNotLeft, int neither) {
		// Convert input numbers to classifications in a sequence
		ArrayList<Integer> computedClusters = new ArrayList<>();
		ArrayList<Integer> groundTruthClusters = new ArrayList<>();
		for (int i = 0; i < leftNotRight + leftAndRight; ++i) {
			computedClusters.add(1);
		}
		for (int i = 0; i < rightNotLeft + neither; ++i) {
			computedClusters.add(0);
		}
		for (int i = 0; i < leftNotRight; ++i) {
			groundTruthClusters.add(0);
		}
		for (int i = 0; i < leftAndRight + rightNotLeft; ++i) {
			groundTruthClusters.add(1);
		}
		for (int i = 0; i < neither; ++i) {
			groundTruthClusters.add(0);
		}

		return computeR(computedClusters, groundTruthClusters);
	}

	private static double computeFastPairR(int leftNotRight, int leftAndRight, int rightNotLeft, int neither) {
		int total = leftNotRight + rightNotLeft + leftAndRight + neither;
		// Class membership, will be 0 if out, 1 if in
		int iLeft, jLeft, iRight, jRight;
		// Number of pairs in same class on both left and right
		int s00 = 0;
		// Number of pairs in different class on both left and right
		int s11 = 0;
		// Iterate over pairs
		for (int i = 0; i < total; ++i) {
			for (int j = i + 1; j < total; ++j) {
				// Entity is in left set if between 0 and leftNotRight + leftAndRight
				if (i == j) {
					continue;
				}
				if (i < leftNotRight + leftAndRight) {
					iLeft = 1;
				}
				else {
					iLeft = 0;
				}
				if (j < leftNotRight + leftAndRight) {
					jLeft = 1;
				}
				else {
					jLeft = 0;
				}
				// Offset of right such that overlap is correct
				if (i >= leftNotRight && i < leftNotRight + leftAndRight + rightNotLeft) {
					iRight = 1;
				}
				else {
					iRight = 0;
				}
				if (j >= leftNotRight && j < leftNotRight + leftAndRight + rightNotLeft) {
					jRight = 1;
				}
				else {
					jRight = 0;
				}
				if (iLeft == jLeft && iRight == jRight) {
					// Mark.say(i, j, "same class on both sides");
					++s00;
				}
				else if (iLeft != jLeft && iRight != jRight) {
					// Mark.say(i, j, "not in same class on both sides");
					++s11;
				}
			}
		}
		double fastPairResult = 2.0 * (s00 + s11) / (total * (total - 1));
		return fastPairResult;
	}

	private static double computeSuspectR(int leftNotRight, int leftAndRight, int rightNotLeft, int neither, double slowPairResult, double fastPairResult) {
		double truePositives = leftAndRight;
		double falsePositives = leftNotRight;
		double falseNegatives = rightNotLeft;
		double trueNegatives = neither;
		double result = (truePositives + trueNegatives) / (truePositives + falsePositives + falseNegatives + trueNegatives);

		return result;
	}

	private static double computeMultipleR(int leftNotRight, int leftAndRight, int rightNotLeft, int neither) {

		double slowPairResult = computeR(leftNotRight, leftAndRight, rightNotLeft, neither);

		// Second try, still work with pairs, but try to be fast

		double fastPairResult = computeFastPairR(leftNotRight, leftAndRight, rightNotLeft, neither);

		// Third try, calculate using precision and recall, very, very suspect

		double result = computeSuspectR(leftNotRight, leftAndRight, rightNotLeft, neither, slowPairResult, fastPairResult);

		Mark.say("Rand for", leftNotRight, leftAndRight, rightNotLeft, neither, "\nSlow   ", slowPairResult, "\nFast   ", slowPairResult, "\nSuspect", result);
		return result;
	}

	public static double computeF(int leftNotRight, int leftAndRight, int rightNotLeft) {
		double truePositives = leftAndRight;
		double falsePositives = leftNotRight;
		double falseNegatives = rightNotLeft;

		double precision = truePositives / (truePositives + falsePositives);
		double recall = truePositives / (truePositives + falseNegatives);
		double f = 2 * precision * recall / (precision + recall);
		Mark.say("F for", leftNotRight, leftAndRight, rightNotLeft, f);
		return f;
	}

	public static void main(String[] ignore) {
		Integer[] first = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		Integer[] second = { 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 2, 0, 0, 2, 2, 2 };
		List<Integer> firstL = Arrays.asList(first);
		List<Integer> secondL = Arrays.asList(second);

		double result = StatisticsCalculator.computeR(firstL, secondL);

		Mark.say("Web check:", result);

		computeBoth(0, 12, 0, 12);
		computeBoth(1, 10, 1, 12);
		computeBoth(5, 2, 5, 12);
		computeBoth(10, 2, 0, 12);
		computeBoth(11, 1, 0, 12);

		Mark.say("\n");

		computeBoth(2, 10, 0, 12);
		computeBoth(0, 10, 2, 12);
		computeBoth(10, 2, 0, 12);
		computeBoth(20, 20, 24, 72);

		computeBoth(0, 12, 0, 12);

		computeBoth(25, 0, 25, 100);
		computeBoth(25, 5, 25, 100);

		computeBoth(20, 20, 24, 72);

		computeBoth(1, 0, 1, 0);
		computeBoth(1, 2, 1, 2);

		computeBoth(12, 0, 12, 0);

		computeBoth(10, 3, 9, 0);

		computeBoth(6, 8, 4, 0);

		computeBoth(7, 7, 6, 0);

	}
}
