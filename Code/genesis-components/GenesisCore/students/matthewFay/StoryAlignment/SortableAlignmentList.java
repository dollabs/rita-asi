package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import utils.Mark;

@SuppressWarnings({ "serial", "rawtypes" })
public class SortableAlignmentList extends ArrayList<Alignment> {
	
	/**
	 * Sorts largest to smallest
	 */
	public void sort() {
		Collections.sort(this, new Comparator<Alignment>() {
			@Override
			public int compare(Alignment o1,
					Alignment o2) {
				if(o1.score < o2.score)
					return 1;
				if(o1.score > o2.score)
					return -1;
				return 0;
			}
		});
	}
	
	public static void main(String args[]) {
		SortableAlignmentList list = new SortableAlignmentList();
		Alignment<Float, Float> a = new Alignment<Float,Float>();
		a.score = 500f;
		Alignment<Float, Float> b = new Alignment<Float, Float>();
		b.score = 200f;
		Alignment<Float, Float> c = new Alignment<Float, Float>();
		c.score = 700f;
		Alignment<Float, Float> d = new Alignment<Float, Float>();
		d.score = 0f;
		
		list.add(a);
		list.add(b);
		list.add(c);
		list.add(d);
		
		list.sort();
		for(Alignment x : list) {
			Mark.say(x.score);
			Mark.say(x);
		}
	}
}
