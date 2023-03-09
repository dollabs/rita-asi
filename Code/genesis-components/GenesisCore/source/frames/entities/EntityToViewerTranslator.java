package frames.entities;

import java.awt.Color;
import java.util.*;

import utils.Mark;
import constants.Markers;
import frames.classic.FrameBundle;

public class EntityToViewerTranslator {

	public static final Object[] SHOW_ALL_THREADS = { new Object() };

	// public static final Object[] SHOW_DEFAULT_THREADS = {"thing",
	// "description"};
	public static final Object[] SHOW_DEFAULT_THREADS = { "thing", Markers.DESCRIPTOR, Entity.MARKER_DESCRIPTION, Entity.MARKER_FEATURE, "action",
	        Entity.MARKER_OWNERS };

	public static final Object[] SHOW_NO_THREADS = {};

	private static boolean showIDNumber = true;

	/**
	 * Converts model-side object to view-side object, a complex translation job. Things, Derivatives, Relations, and
	 * Sequences are translated into FrameBundle instances, which the view knows how to display.
	 */
	public static FrameBundle translate(Entity thing) {
		return translate(thing, SHOW_DEFAULT_THREADS);
	}

	/**
	 * Copy of previous translate() method which allows the user to specify which threads to display. MAF.12.1.03
	 */
	public static FrameBundle translate(Entity entity, Object[] types) {
		if (entity == null) {
			return null;
		}
		String name = entity.getType();
		Vector<String> threads = new Vector<String>();

		String decorations = "";
		ArrayList<Object> features = entity.getFeatures();

		if (features.size() > 0) {
			decorations += "Features:";
		}
		for (Object o : features) {
			decorations += " ";
			if (o instanceof Entity) {
				decorations += ((Entity) o).getType();
			}
			else {
				decorations += o.toString();
			}
		}
		for (String key : entity.getKeys()) {
			if (key.equals(Markers.FEATURE)) {
				continue;
			}
			Object o = entity.getProperty(key);

			if (o == null) {
				continue;
			}
			else {
				decorations += " (" + key + ": ";
			}
			if (o instanceof Entity) {
				decorations += ((Entity) o).getType();
			}
			else {
				decorations += o.toString();
			}
			decorations += ")";
		}

		// threads.add("Horseshit (bull shit) red blue\n");
		// if (thing.entityP()) {threads.add(thing.getIdentifier());}
		if (showIDNumber || entity.entityP()) {
			threads.add(entity.getIdentifier());
		}

		threads.add(decorations);

		if (types == SHOW_ALL_THREADS) {
			for (int i = 0; i < entity.getBundle().size(); ++i) {
				Thread thread = (Thread) (entity.getBundle().elementAt(i));
				threads.add(thread.getString().trim());
			}
		}
		else {
			// Modified by phw to show threads in specified order
			for (int i = 0; i < SHOW_DEFAULT_THREADS.length; ++i) {
				try {
					for (int j = 0; j < entity.getBundle().size(); ++j) {
						Thread thread = (Thread) (entity.getBundle().elementAt(j));
						if (thread.contains((String) SHOW_DEFAULT_THREADS[i])) {
							threads.add(thread.getString().trim());
						}
					}
				}
				catch (Exception e) {
					Mark.err("Blunder in getting viewing information");
				}
			}
			/*
			 * for (int i = 0; i < thing.getBundle().size(); ++i) { Thread thread = (Thread)
			 * (thing.getBundle().elementAt(i)); if (thread.contains(types)) { threads.add(thread.getString().trim()); }
			 * }
			 */
		}
		FrameBundle bundle = new FrameBundle(name, threads, entity.hasFeature(Markers.NOT));
		if (types == SHOW_NO_THREADS) {
			bundle.setShowNoThreads(true);
		}
		if (entity instanceof Sequence) {
			bundle.setBarColor(Color.black);
			Sequence sequence = (Sequence) entity;
			Vector v = sequence.getElements();
			for (int i = 0; i < v.size(); ++i) {
				bundle.addFrameBundle(translate((Entity) (v.elementAt(i)), types));
			}
		}
		else if (entity instanceof Relation) {
			bundle.setBarColor(Color.red);
			Relation relation = (Relation) entity;
			bundle.addFrameBundle(translate((Entity) (relation.getSubject()), types));
			bundle.addFrameBundle(translate((Entity) (relation.getObject()), types));
		}
		else if (entity instanceof Function) {
			bundle.setBarColor(Color.blue);
			Function derivative = (Function) entity;
			bundle.addFrameBundle(translate(derivative.getSubject(), types));
		}
		else if (entity instanceof Entity) {
			bundle.setBarColor(Color.gray);
		}
		else {
			System.err.println("Unrecognized object in Thing to FrameBundle translator");
		}
		Color tracerColor = Tracer.getColor(entity);
		if (tracerColor != null) {
			bundle.setBarColor(tracerColor);
		}
		return bundle;
	}

	/**
	 * Translates vector of things to a vector of bundles.
	 */
	public static Vector translate(Vector variables) {
		Vector<FrameBundle> bundles = new Vector<FrameBundle>();
		for (int i = 0; i < variables.size(); ++i) {
			Entity thing = (Entity) (variables.elementAt(i));
			bundles.add(EntityToViewerTranslator.translate(thing));
		}
		return bundles;
	}

}
