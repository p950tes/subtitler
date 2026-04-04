package se.p950tes.subtitler.tooling.parsing;

import java.nio.file.Path;

import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;

public class SubtitleParserFactory {

	private final Logger logger;
	private final FileManager fileManager;

	public SubtitleParserFactory(Logger logger, FileManager fileManager) {
		this.fileManager = fileManager;
		this.logger = logger;
	}
	
	public SubtitleParser newParser(Path file) {
		return new SubtitleParser(logger, fileManager, file);
	}
}
