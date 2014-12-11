package ch.hsr.osminabox.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Util {

	public static <T> List<T> asList(T... ts) {
		List<T> list = new ArrayList<T>();
		for (T t : ts) {
			list.add(t);
		}
		return list;
	}

	public static <T> Set<T> asSet(T... setEntries) {
		Set<T> set = new TreeSet<T>();
		for (T t : setEntries) {
			set.add(t);
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> asMap(K firstKey, V firstValue,
			Object... objects) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(firstKey, firstValue);
		if (objects.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Expected a list with objects with an even number");
		}
		for (int i = 0; i < objects.length ; i+=2) {
			Object key = objects[i];
			Object value = objects[i + 1];
			if (!key.getClass().equals(firstKey.getClass())) {
				throw new IllegalArgumentException("Key at " + (1 + (i * 2))
						+ " must be of type " + firstKey.getClass());
			}
			if (!value.getClass().equals(firstValue.getClass())) {
				throw new IllegalArgumentException("Value at " + (2 + (i * 2))
						+ " must be of type " + firstKey.getClass());
			}
			map.put((K) key, (V) value);
		}
		return map;
	}

	public static String readFileAsString(String filePath) {
		try {
			byte[] buffer = new byte[(int) new File(filePath).length()];
			BufferedInputStream f = null;

			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
			if (f != null)
				f.close();
			return new String(buffer);
		} catch (Exception e) {
			throw new RuntimeException(e.getClass()+": "+e.getMessage());
		}
	}

	public static void writeStringToFile(String filename, String content) throws IOException{
		File file = new File(filename);
		file.delete();
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileWriter out = new FileWriter(file);
		out.write(content);
		out.flush();
		out.close();
	}
	public static <T>T[] asArray(T ...ts ) {
		return ts;
	}
}
