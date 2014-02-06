package net.enilink.komma.docs.util;

public class Path {

	public static String get(int level) {
		String path = "";
		for (int i = 0; i < level; i++) {
			path = path + "../";
		}
		return path;
	}

}
