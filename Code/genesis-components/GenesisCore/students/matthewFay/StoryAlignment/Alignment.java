package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.LinkedList;

import frames.entities.Entity;
import matthewFay.Utilities.Pair;

@SuppressWarnings("serial")
public class Alignment<A, B> extends LinkedList<Pair<A, B>> {
	//Pattern
	public String aName = "";
	//Datum
	public String bName = "";
	
	public float score;

	public Alignment() {
		super();
	}
	
	public ArrayList<A> getA() {
		ArrayList<A> list = new ArrayList<A>();
		for(Pair<A,B> p : this) {
			list.add(p.a);
		}
		return list;
	}
	
	public ArrayList<B> getB() {
		ArrayList<B> list = new ArrayList<B>();
		for(Pair<A,B> p : this) {
			list.add(p.b);
		}
		return list;
	}
	
	public String toPrunedString() {
		String as = "";
		for(Pair<A, B> pair : this)
		{
				Entity a = (Entity)pair.a;
				Entity b = (Entity)pair.b;
				if(a != null && b != null)
				{
					as += a.asString();
					as += " : ";
					as += b.asString();
					as += "\n";
				}
		}
		return as;
	}
	
	public int getMatchCount() {
		int count = 0;
		for(Pair<A, B> pair : this) {
			if(pair.a != null && pair.b != null) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public String toString()
	{
		String as = "";
		for(Pair<A, B> pair : this)
		{
				if(pair.a != null)
				{
					as += pair.a.toString();
				} else {
					as += "---";
				}
				as += " : ";
				if(pair.b != null)
				{
					as += pair.b.toString();
				} else {
					as += "---";
				}
				as += "\n";
		}
		return as;
	}
}