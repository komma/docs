package net.enilink.komma.docs.model;

public class Entry {

	public String title;
	public String path;

	public Entry(String title, String path) {
		this.title = title;
		this.path = path;
		if (this.path.startsWith("/")) {
			this.path = path.substring(1);
		}
	}

}
