package net.enilink.komma.docs.model;

import java.util.ArrayList;
import java.util.List;

public class Node {

	public List<Node> children = new ArrayList<Node>();

	public List<Entry> entries = new ArrayList<Entry>();

	public String title;

	public Node(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return title;
	}

	public void add(Node node) {
		children.add(node);
	}

}
