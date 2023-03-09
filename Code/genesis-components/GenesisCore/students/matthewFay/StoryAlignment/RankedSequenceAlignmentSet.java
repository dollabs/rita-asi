package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import frames.entities.Entity;
import matthewFay.Utilities.Pair;

@SuppressWarnings("serial")
public class RankedSequenceAlignmentSet<Pattern, Datum> extends ArrayList<SequenceAlignment> {
	
	public RankedSequenceAlignmentSet() {
	}
	
	public int getMaxLength() {
		int maxLength = 0;
		for(SequenceAlignment alignment : this) {
			maxLength = alignment.size() > maxLength ? alignment.size() : maxLength;
		}
		return maxLength;
	}
	
	/**
	 * Undoes changes made by Global alignment (removes empty pairs in alignments)
	 */
	public void removeGlobalAlignment() {
		for(SequenceAlignment alignment : this) {
			int elementIterator=0;
			while(elementIterator < alignment.size()) {
				if(alignment.get(elementIterator).a == null && alignment.get(elementIterator).a == null) {
					alignment.remove(elementIterator);
				} else {
					elementIterator++;
				}
			}
		}
	}
	
	/**
	 * Globally aligns all of the patterns with the datum
	 * Dangerous function, adds null entries to shorter matches
	 * in order to make all same length
	 * DO NOT CALL IF DATUMS MAY BE DIFFERENT!
	 */
	public void globalAlignment() {
		
		//Max distance of current datum from the start
		int maxDatumDistance = 0;
		// Current alignment in iteration
		SequenceAlignment currentAlignment = null;
		//Iterate over a pattern in a specific alignment
		int[] patternIterators = new int[this.size()];
		//Iterate over the set of all alignments
		int alignmentIterator = 0;
		
		boolean finished = false;
		while(!finished) {
			//Move up each alignment iterator to the next datum element	
			for(alignmentIterator = 0;alignmentIterator<this.size();alignmentIterator++) {
				currentAlignment = get(alignmentIterator);
				//Move Iterator forward until either at end or at next datum
				while(patternIterators[alignmentIterator] < currentAlignment.size() &&
				      currentAlignment.get(patternIterators[alignmentIterator]).b == null) {
					patternIterators[alignmentIterator]++;
				}
				if(patternIterators[alignmentIterator] > maxDatumDistance) {
					maxDatumDistance = patternIterators[alignmentIterator];
				}
			}
			
			//Correct each alignment to have current datum at least at maxDatumDistance//
			for(alignmentIterator = 0;alignmentIterator<this.size();alignmentIterator++) {
				currentAlignment = get(alignmentIterator);
				while(patternIterators[alignmentIterator] < maxDatumDistance) {
					currentAlignment.add(patternIterators[alignmentIterator], new Pair<Entity, Entity>(null, null));
					patternIterators[alignmentIterator]++;
				}
			}

			// Check if all pattern iterators are at the end //
			finished = true;
			for(alignmentIterator = 0;alignmentIterator<size();alignmentIterator++) {
				if(patternIterators[alignmentIterator] < get(alignmentIterator).size()) {
					finished = false;
					patternIterators[alignmentIterator]++;
				}
			}
		}
	}
	
	/**
	 * Sorts from Largest to smallest!
	 */
	public void sort() {
		Collections.sort(this, new Comparator<SequenceAlignment>() {
			@Override
			public int compare(SequenceAlignment o1,
					SequenceAlignment o2) {
				if(o1.score < o2.score)
					return 1;
				if(o1.score > o2.score)
					return -1;
				return 0;
			}
		});
	}
}
