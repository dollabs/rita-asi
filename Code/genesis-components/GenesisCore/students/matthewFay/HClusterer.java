package matthewFay;

import java.util.ArrayList;

public abstract class HClusterer<T> {
	
	@SuppressWarnings("serial")
	public static class Cluster<T> extends ArrayList<T> {
		public Cluster<T> parent = null;
		public ArrayList<Cluster<T>> children = new ArrayList<HClusterer.Cluster<T>>();
	
		public T centroid;
		
		public ArrayList<Cluster<T>> allChildren() {
			ArrayList<Cluster<T>> ac = new ArrayList<HClusterer.Cluster<T>>();
			ac.addAll(children);
			for(Cluster<T> child : children) {
				if(child != null)
					return ac;
				return ac;
			}
			
			// Bug fix
			return ac;
		}
	}
	
	public HClusterer() {
		
	}
	
	public abstract float sim(T a, T b);
	
	public Cluster<T> cluster(ArrayList<T> elts) {
		ArrayList<Cluster<T>> topClusters = new ArrayList<Cluster<T>>();
		for(int x=0;x<elts.size();x++) {
			Cluster<T> cluster = new Cluster<T>();
			T X = elts.get(x);
			cluster.add(X);
			topClusters.add(cluster);
		}
		
		while(topClusters.size() > 1) {
			//Find closest clusters//
			int maxX = -1;
			int maxY = -1;
			float maxSim = Float.NEGATIVE_INFINITY;
			
			for(int x=0;x<topClusters.size()-1;x++) {
				Cluster<T> X = topClusters.get(x);
				for(int y=x+1;y<topClusters.size();y++) {
					Cluster<T> Y = topClusters.get(y);
					float sim = simClusters(X,Y);
					if(sim > maxSim) {
						maxX = x;
						maxY = y;
						maxSim = sim;
					}
				}
			}
			
			Cluster<T> A = topClusters.get(maxX);
			Cluster<T> B = topClusters.get(maxY);
			Cluster<T> parent = mergeClusters(A,B);
			A.parent = parent;
			B.parent = parent;
			parent.children.add(A);
			parent.children.add(B);
			topClusters.add(parent);
			topClusters.remove(A);
			topClusters.remove(B);
		}
		
		return topClusters.get(0);
	}
	
	public Cluster<T> mergeClusters(Cluster<T> A, Cluster<T> B) {
		Cluster<T> parent = new Cluster<T>();
		parent.addAll(A);
		parent.addAll(B);
		if(parent.size() > 2) {
			//Calculate Centroid//
			float maxSim = Float.NEGATIVE_INFINITY;
			for(T elt : parent)
			{
				float sim = calculateCentroidDistance(elt,parent);
				if(sim > maxSim)
				{
					maxSim = sim;
					parent.centroid = elt;
				}
			}
		} else {
			parent.centroid = parent.get(0);
		}
		return parent;
	}
	
	private float calculateCentroidDistance(T a, ArrayList<T> elts)
	{
		float distance = 0;
		for (T elt : elts)
		{
			if(elt != a)
				distance += sim(a,elt);
		}
		return distance;
	}
	
	public float simClusters(Cluster<T> A, Cluster<T> B) {
		float maxSim = Float.NEGATIVE_INFINITY;
		if(A.size() > 2) {
			if(B.size() > 2) {
				maxSim = sim(A.centroid,B.centroid);
			} else {
				for(T b : B) {
					float sim = sim(A.centroid,b);
					if(sim > maxSim)
						maxSim = sim;
				}
			}
		} else {
			for(T a : A) {
				if(B.size() > 2) {
					float sim = sim(a,B.centroid);
					if(sim > maxSim)
						maxSim = sim;
				} else {
					for(T b : B) {
						float sim = sim(a,b);
						if(sim > maxSim)
							maxSim = sim;
					}
				}
			}
		}
		return maxSim;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		HClusterer<Character> clusterer = new HClusterer<Character>() {

			@Override
			public float sim(Character a, Character b) {
				float aFloat = a.charValue();
				float bFloat = b.charValue();
				float diff = (aFloat-bFloat);
				float sim = -Math.abs(diff);
				
				return sim;
			}
		};
		
		// two strings; the second has a gap and a 
		// few different letters relative to the first
		String strA = "ABCFGHIJKLMOPQSTUVWXYZ";
		
		// create lists for alignment
		ArrayList<Character> seqA = new ArrayList<Character>(strA.length());
		for(char c : strA.toCharArray()) seqA.add(c);
		
		Cluster<Character> topCluster = clusterer.cluster(seqA);
		
		/*
		String out = "Clustering Output\n";
		for(Cluster<Character> cluster : clusters)
		{
			out += "Cluster, avg, var"+cluster.averageSim+", "+cluster.variance+"\n";
			out += "Centroid: "+cluster.Centroid+"\n";
			for(Character c : cluster)
			{
				out += c;
			}
			out += "\n";
		}
		
		
		System.out.println(out);
		*/
	}
}
