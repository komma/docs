package net.enilink.komma.docs;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

import java.io.File;
import java.io.FileReader;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.DocumentHeader;
import org.asciidoctor.Options;
import org.asciidoctor.Placement;
import org.asciidoctor.internal.IOUtils;

public class Engine {
	private final Asciidoctor engine = Asciidoctor.Factory.create();
	private final Attributes attributes = attributes().backend("html5")
			.showTitle(true).skipFrontMatter(false)
			.tableOfContents(Placement.TOP).tableOfContents(true)
			.sectionNumbers(true).get();
	private final Options options = options().attributes(attributes).get();

	public String render(File input) {
		try {
			return engine.render(IOUtils.readFull(new FileReader(input)),
					options);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public DocumentHeader getHeader(File input) {
		return engine.readDocumentHeader(input);
	}
}
