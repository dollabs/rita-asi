package matthewFay;

import java.util.ArrayList;
import java.util.Random;

public abstract class KClusterer<T> {
	private int k;
	
	@SuppressWarnings("serial")
	public static class Cluster<T> extends ArrayList<T> {
		public T Centroid;
		
		public float averageSim;
		public float variance;
	}
	
	private void genStats(Cluster<T> cluster)
	{
		float sum = 0;
		for(T t : cluster) {
			sum += sim(t,cluster.Centroid);
		}
		cluster.averageSim = sum/(float)cluster.size();
		sum = 0;
		for(T t : cluster) {
			float score = sim(t,cluster.Centroid);
			sum += (score-cluster.averageSim)*(score-cluster.averageSim);
		}
		cluster.variance = sum/((float)cluster.size());
	}
	
	public KClusterer(int k)
	{
		this.k = k;
	}
	
	public ArrayList<Cluster<T>> cluster(ArrayList<T> elts)
	{
		/////////////////////////////////////////////////////
		// Randomly Partition Elts into k initial clusters //
		/////////////////////////////////////////////////////
		//Initialization
		ArrayList<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
		int kIter = 0;
		for(kIter = 0;kIter < k;kIter++) {
			clusters.add(new Cluster<T>());
		}
		kIter = 0;
		Random r = new Random();
		//Randomly select Element and place in next cluster//
		while(!elts.isEmpty()) {
			int eltIndex = r.nextInt(elts.size());
			T toAdd = elts.get(eltIndex);
			clusters.get(kIter).add(toAdd);
			elts.remove(eltIndex);
			kIter = (kIter+1)%k;
		}
		
		//Main Loop!//
		
		//Watch for swaps//
		boolean swapOccured = true;
		
		while(swapOccured) {
		
			swapOccured = false;
			/////////////////////////////////////
			// Calculate Centroids of Clusters //
			/////////////////////////////////////
			//Loop Through each Cluster//
			for(kIter=0;kIter<k;kIter++) {
				//Calculate centroidDistance For each elt in cluster
				Cluster<T> cluster = clusters.get(kIter);
				float maxDistance = Float.NEGATIVE_INFINITY;
				for(T elt : cluster)
				{
					float distance = calculateCentroidDistance(elt,cluster);
					if(distance > maxDistance)
					{
						maxDistance = distance;
						cluster.Centroid = elt;
					}
				}
			}
			////////////////////////////////
			// Rearrange Cluster Elements //
			////////////////////////////////
			
			//Loop Through each Cluster//
			for(kIter=0;kIter<k;kIter++)
			{
				Cluster<T> cluster = clusters.get(kIter);
				//Loop Through each element in Cluster (other than Centroid?)//
				ArrayList<T> tempList = new ArrayList<T>();
				for(T elt : cluster)
				{
					tempList.add(elt);
				}
				
				for(T elt : tempList) {
					Cluster<T> currentCluster = cluster;
					if(!elt.equals(cluster.Centroid) || true) {
						float distance = sim(elt, cluster.Centroid);
						//Loop Through each OTHER cluster
						for(int kIter2=0;kIter2<k;kIter2++)
						{
							if(kIter2==kIter)
								continue;
							Cluster<T> targetCluster = clusters.get(kIter2);
							//Check if this cluster is a better fit
							float targetDistance = sim(elt, targetCluster.Centroid);
							if(targetDistance > distance)
							{
								//Swap to that Cluster
								distance = targetDistance;
								currentCluster.remove(elt);
								targetCluster.add(elt);
								currentCluster=targetCluster;
								swapOccured=true;
							}
						}
					}
				}
			}
		}
		
		for(Cluster<T> cluster : clusters)
			genStats(cluster);
		
		return clusters;
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
	
	public abstract float sim(T a, T b);
	
	public static void main(String[] args)
	{
		KClusterer<Character> clusterer = new KClusterer<Character>(5) {

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
		
		ArrayList<Cluster<Character>> clusters = clusterer.cluster(seqA);
		
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
	}
}
