package net.enilink.komma.docs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.enilink.komma.docs.model.Entry;
import net.enilink.komma.docs.model.Node;

import org.asciidoctor.DocumentHeader;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

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

	class Options extends Entry {
		public String contents;

		public Options(String title, String path, String contents) {
			super(title, path);
			this.contents = contents;
		}

		public String getRootPath() {
			StringBuilder sb = new StringBuilder();
			Path parentPath = Paths.get(path).getParent();
			if (!parentPath.equals(outputPath)) {
				Path relative = outputPath.relativize(parentPath);
				for (int i = 0; i < relative.getNameCount(); i++) {
					sb.append("../");
				}
			}
			return sb.toString();
		}

		public String getIndexPath() {
			if (!"index.html".equals(outputPath.relativize(Paths.get(path))
					.toString())) {
				return getRootPath() + "index.html";
			}
			return null;
		}
	}

	private Engine engine = new Engine();

	private final Path outputPath;
	private final Path inputPath;

	public Generator(String inputDir, String outputDir) {
		this.inputPath = Paths.get(inputDir);
		this.outputPath = Paths.get(outputDir);
	}

	public void generate() {
		createIndexDocument(createContentDocuments());
	}

	private void createIndexDocument(Node root) {
		/*
		 * Handle this manually
		 */
	}

	private Node createContentDocuments() {
		Node root = new Node("Root");
		try {
			createContentDocuments(root, inputPath.toFile());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return root;
	}

	private void createContentDocuments(Node head, File dir) throws IOException {
		Node node = new Node(dir.getName());
		head.children.add(node);
		for (File directory : dir.listFiles(DIRECTORIES)) {
			createContentDocuments(node, directory);
		}
		STGroup contentSTGroup = new STGroupFile("./resources/content.stg");
		for (File input : dir.listFiles(ADOC_FILES)) {
			DocumentHeader header = engine.getHeader(input);
			String contents = engine.render(input);

			Path outPath = getOutputFilePath(input.toPath(), "html");
			Entry entry = new Entry(header.getDocumentTitle(),
					outPath.toString());
			Files.createDirectories(outPath.getParent());

			ST st = contentSTGroup.getInstanceOf("main");
			st.add("opts", new Options(entry.title, entry.path, contents));
			try (BufferedWriter writer = Files.newBufferedWriter(outPath,
					Charset.forName("UTF-8"))) {
				writer.write(st.render());
			}
			node.entries.add(entry);
		}

		for (File input : dir.listFiles(REST)) {
			Files.copy(input.toPath(),
					outputPath.resolve(inputPath.relativize(input.toPath())));
		}
	}

	private Path getOutputFilePath(Path srcFile, String suffix) {
		String out = outputPath.resolve(inputPath.relativize(srcFile))
				.toString();
		out = out.replaceAll("\\..*$", "." + suffix);
		Path outPath = Paths.get(out);
		return outPath;
	}
}