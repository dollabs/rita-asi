package genesis;

import java.io.File;
import java.net.*;
import java.util.*;

import lexicons.WorkingVocabulary;
import viz.images.ImageAnchor;
import connections.*;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Nov 11, 2007 @author phw
 */

public class PictureFinder extends AbstractWiredBox {

	private ArrayList<File> files = new ArrayList();

	private WorkingVocabulary workingVocabulary = WorkingVocabulary.getWorkingVocabulary();

	public PictureFinder() {
		super("Picture finder");
		Connections.getPorts(this).addSignalProcessor("input");
	}

	public void input(Object object) {
		if (files.size() == 0) {
			initializeFileArray();
		}
		if (object instanceof Entity) {
			ArrayList<Vector> classVectors = getClasses((Entity) object);
			if (classVectors == null || classVectors.isEmpty()) {
				return;
			}
			for (Iterator<Vector> iterator = classVectors.iterator(); iterator.hasNext();) {
				Vector<String> classes = iterator.next();
				// System.out.println("Working on " + classes);
				for (int i = classes.size() - 1; i >= 0; --i) {
					boolean done = false;
					for (int j = 0; j < files.size(); ++j) {
						String className = classes.get(i);
						String fileName = stripExtension(files.get(j).getName());
						if (className.toLowerCase().equals(fileName.toLowerCase())) {
							// System.out.println("Bingo: " + files.get(j));
							Connections.getPorts(this).transmit(files.get(j));
							done = true;
							break;
						}
					}
					if (done) {
						break;
					}
				}
			}
		}
	}

	private String stripExtension(String s) {
		int index = s.indexOf('.');
		if (index >= 0) {
			return s.substring(0, index);
		}
		return s;
	}

	private void initializeFileArray() {
		URL url = ImageAnchor.class.getResource("house.jpg");
		// System.out.println(ImageAnchor.class);
		File[] candidates = new File[0];
		try {
			// System.out.println("Url: " + url);
			candidates = new File(url.toURI()).getParentFile().listFiles();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// File [] candidates =
		for (int i = 0; i < candidates.length; ++i) {
			if (candidates[i].isFile()) {
				files.add(candidates[i]);
			}
		}
	}

	private ArrayList<Vector> getClasses(Entity thing) {
		try {
			ArrayList<Entity> things = digOutThing(thing);
			ArrayList<Vector> result = new ArrayList<Vector>();
			for (int i = 0; i < things.size(); ++i) {
				result.add(things.get(i).getTypes());
			}
			return result;
		}
		catch (RuntimeException e) {
			System.err.println("Blew out of getClasses");
			e.printStackTrace();
		}
		return new ArrayList<Vector>();
	}

	private ArrayList<Entity> digOutThing(Entity thing) {
		if (thing.entityP()) {
			String word = thing.getType();
			workingVocabulary.add(word);
			// System.out.println("Dictionary size: " + workingVocabulary.size());
			ArrayList<Entity> l = new ArrayList<Entity>();
			l.add(thing);
			return l;
		}
		else if (thing.functionP()) {
			return digOutThing(((Function) thing).getSubject());
		}
		else if (thing.relationP()) {
			ArrayList<Entity> l = new ArrayList<Entity>();
			ArrayList<Entity> subjects = digOutThing(((Relation) thing).getSubject());
			ArrayList<Entity> objects = digOutThing(((Relation) thing).getObject());
			l.addAll(subjects);
			l.addAll(objects);
			return l;
		}
		else if (thing.sequenceP()) {
			Sequence sequence = (Sequence) thing;
			Vector elements = sequence.getElements();
			ArrayList<Entity> l = new ArrayList<Entity>();
			for (int i = sequence.getElements().size() - 1; i >= 0; --i) {
				Entity element = (Entity) (elements.get(i));
				ArrayList<Entity> result = digOutThing(element);
				l.addAll(result);
				return l;
			}
		}
		return new ArrayList<Entity>();
	}

	public static void main(String[] ignore) {
		Entity thing = new Entity("Dog");
		thing.addType("Bouvier");
		Entity thing2 = new Entity("Mountain");
		Relation relation = new Relation(thing, thing2);
		PictureFinder finder = new PictureFinder();
		finder.input(thing);
	}

}
