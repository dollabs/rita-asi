package matthewFay;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import matthewFay.KClusterer.Cluster;
import utils.Mark;
import connections.*;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Sequence;

public class ClusterProcessor extends AbstractWiredBox {

	public static final String STORY_PORT = "story port";

	public static final String CLUSTER_PORT = "cluster port";

	public static final String RESET_PORT = "reset port";

	public static boolean useReflections = false;

	public static boolean singlePort = false;

	private static String CLUSTER_PROCESSOR = "ClusterProcessor";

	public ClusterProcessor() {
		super(CLUSTER_PROCESSOR);

		Connections.getPorts(this).addSignalProcessor(STORY_PORT, "processStory");
		Connections.getPorts(this).addSignalProcessor(CLUSTER_PORT, "doClustering");
	}

	ArrayList<Sequence> stories = new ArrayList<Sequence>();

	public void processStory(Object input) {
		if (!Radio.alignmentButton.isSelected()) return;
		if (!(input instanceof Sequence)) return;
		Sequence storySignal = (Sequence) input;

		stories.add(storySignal);
	}

	int clusterCount = 9;

	public void doClustering(Object input) {
		if (input instanceof Entity) Mark.say(((Entity) input).asString());
		Mark.say("Do clustering");
		SequenceClusterer sc = new SequenceClusterer(clusterCount);
		ArrayList<Cluster<Sequence>> clusters = sc.cluster(stories);

		Mark.say("Clustering Complete");

		for (Cluster<Sequence> cluster : clusters) {
			Mark.say("---");
			Mark.say("Average Sim: " + cluster.averageSim);
			Mark.say("Variance: " + cluster.variance);
			Mark.say("Centroid: " + cluster.Centroid.asString());
			for (Sequence s : cluster) {
				Mark.say("Elements: " + s.getNumberOfChildren() + " " + s.asString());
			}
		}
	}

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	public static void main(String[] args) {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		URL serverURL = null;
		try {
			serverURL = new URL(wireServer);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String input = "";
			ClusterProcessor.CLUSTER_PROCESSOR = "ClusterProcessorService";
			Connections.useWireServer(serverURL);
			ClusterProcessor cp = new ClusterProcessor();
			Connections.publish(cp, ClusterProcessor.CLUSTER_PROCESSOR);

			System.out.println("ClusterProcessorService started, input commands");

			while (!input.toLowerCase().equals("quit")) {
				input = in.readLine().trim().intern();
				BetterSignal b = new BetterSignal();
				String[] sigargs = input.split(" ");
				for (String s : sigargs) {
					b.add(s);
				}
			}

		}
		catch (NetWireException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
