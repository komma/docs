package net.enilink.komma.docs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

import net.enilink.komma.docs.model.Entry;
import net.enilink.komma.docs.model.Node;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.asciidoctor.DocumentHeader;
import org.asciidoctor.internal.IOUtils;
import org.rythmengine.Rythm;

public class Generator {

	private static final FileFilter DIRECTORIES = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};

	private static final FileFilter ADOC_FILES = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return !file.isDirectory() && file.getName().endsWith(".adoc");
		}
	};

	private static final FileFilter REST = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return !file.isDirectory() && !file.getName().endsWith(".adoc");
		}
	};

	private Engine engine = new Engine();

	private String output;
	private String input;

	private String getBasePath() {
		try {
			return getBasePathFile().getCanonicalPath();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private File getBasePathFile() {
		return new File(input);
	}

	public Generator(String inputDir, String outputDir) {
		this.input = inputDir;
		this.output = outputDir;
	}

	public void generate() {
		createIndexDocument(createContentDocuments());
	}

	private void createIndexDocument(Node root) {
		/*
		 * Handle this manually
		 */

		// try {
		// IOUtils.writeFull(new FileWriter("./output/index.html"),
		// Rythm.render(new File("./resources/toc.html"), root));
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }
	}

	private Node createContentDocuments() {
		Node root = new Node("Root");
		createContentDocuments(root, getBasePathFile(), 0);
		return root;
	}

	private void createContentDocuments(Node head, File file, int level) {
		Node node = new Node(file.getName());
		head.children.add(node);
		for (File directory : file.listFiles(DIRECTORIES)) {
			createContentDocuments(node, directory, level + 1);
		}

		for (File input : file.listFiles(ADOC_FILES)) {
			DocumentHeader header = engine.getHeader(input);
			String documentContent = engine.render(input);
			try {
				IOUtils.writeFull(
						new FileWriter(getRelatedOutputFile(input, ".html")),
						Rythm.render(new File("./resources/content.html"),
								level, !input.getName().equals("index.adoc")
										|| level > 0,
								header.getDocumentTitle(), documentContent));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			Entry entry = new Entry(header.getDocumentTitle(),
					getRelatedFilePath(input, ".html"));
			node.entries.add(entry);
		}

		for (File input : file.listFiles(REST)) {
			try {
				FileUtils.copyFile(
						input,
						getRelatedOutputFile(
								input,
								"."
										+ FilenameUtils.getExtension(input
												.getName())));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private File getRelatedOutputFile(File file, String suffix) {
		File output = new File(getRelatedOutputFilePath(file, suffix));
		if (!output.exists()) {
			output.getParentFile().mkdirs();
			try {
				output.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return output;
	}

	private String getRelatedOutputFilePath(File file, String suffix) {
		return output + "/" + getRelatedFilePath(file, suffix);
	}

	private String getRelatedFilePath(File file, String suffix) {
		String path = getRelativeDirectoryPath(file);
		String string = path + "/"
				+ file.getName().substring(0, file.getName().lastIndexOf("."))
				+ suffix;
		return string;
	}

	private String getRelativeDirectoryPath(File file) {
		try {
			return file.getParentFile().getCanonicalPath()
					.substring(getBasePath().length());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
