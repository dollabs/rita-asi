package frames;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;

public class SmartFrameFactory {
	static private Set<String>	unframedTypes	= new HashSet<String>();
	static private List<Class>	cachedFrames	= null;

	static private List<Class> getFrames() {
		if (SmartFrameFactory.cachedFrames == null) {
			SmartFrameFactory.cachedFrames = SmartFrameFactory.findSubclasses("frames", Frame.class);
		}
		return SmartFrameFactory.cachedFrames;
	}

	static public Frame translate(Entity thing) {
		try {
			for (Class<? extends Frame> frame : SmartFrameFactory.getFrames()) {
				String type = (String) frame.getField("FRAMETYPE").get(null);
				if (thing.isA(type)) {
					return frame.getConstructor(Entity.class).newInstance(thing);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if (SmartFrameFactory.unframedTypes.add(thing.getType())) {
			//System.err.println(SmartFrameFactory.unframedTypes);
		}
		return null;
	}

	// Adapted from RTSI.java
	public static List<Class> findSubclasses(String packagename, Class tosubclass) {
		List<Class> result = new ArrayList<Class>();
		String name = new String(packagename);
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		name = name.replace('.', '/');
		URL url = SmartFrameFactory.class.getResource(name);
		assert url != null;
		File directory = new File(url.getFile());
		assert directory.exists();
		for (String element : directory.list()) {
			if (element.endsWith(".class")) {
				String classname = element.substring(0, element.length() - 6);
				try {
					Class c = Class.forName(packagename + "." + classname);
					if (tosubclass.isAssignableFrom(c)) {
						result.add(c);
					}
				} catch (ClassNotFoundException cnfex) {
					System.err.println(cnfex);
				}
			}
		}
		return result;
	}
}
