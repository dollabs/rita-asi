package utils;

import java.io.*;
import java.net.URL;
import java.util.zip.*;

import com.thoughtworks.xstream.XStream;

public class Serializer<T> {
	private static XStream xml = new XStream();

	public String toXML(T t) {
		return xml.toXML(t);
	}

	@SuppressWarnings("unchecked")
	public T fromXML(String s) {
		return (T) xml.fromXML(s);
	}

	public void save(T t, File file) {
		try {
			GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(file));

			os.write(toXML(t).getBytes("UTF-8"));

			os.finish();
			os.close();
		} catch (FileNotFoundException x) {
			System.err.println("Serializer " + x.getMessage());
		} catch (IOException x) {
			System.err.println("Serializer " + x.getMessage());
		}
	}

	public T load(URL url) {
		try {
			System.out.println("URL = " + url);

			GZIPInputStream zipStream = new GZIPInputStream(url.openStream());
			InputStream in = new BufferedInputStream(zipStream);
			
			StringBuilder sb = new StringBuilder();
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				sb.append(new String(buf, 0, len, "UTF-8"));
			}

			return fromXML(sb.toString());

		} catch (FileNotFoundException x) {
			System.err.println("Serializer: " + x.getMessage());
		} catch (IOException x) {
			System.err.println("Serializer: " + x.getMessage());
		}
		return null;
	}
}
