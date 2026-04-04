package se.p950tes.subtitler.tooling.parsing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.model.SubtitleEntry;
import se.p950tes.subtitler.model.SubtitleFile;

public class SubtitleParser {

	private static final Pattern INDEX_PATTERN = Pattern.compile("^\\d+$");
	private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^[\\d:,]+\\s+" + Pattern.quote("-->") + "\\s+[\\d:,]+$");

	private static enum Type {
		INDEX, TIMESTAMP, CONTENT;
	}

	private final List<SubtitleEntry> entries = new ArrayList<>();
	private final FileManager fileManager;
	private final Logger logger;
	private final Path file;

	private String currentIndex;
	private String currentTimestamp;
	private List<String> currentContent = new ArrayList<>();
	private Type lastParsed;

	public SubtitleParser(Logger logger, FileManager fileManager, Path file) {
		this.fileManager = fileManager;
		this.logger = logger;
		this.file = file;
	}

	public SubtitleFile parse() {
		logger.verbose("Parsing: " + file);
		List<String> lines = fileManager.readLinesFromFile(file);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			String nextLine = i < (lines.size() - 1) ? lines.get(i + 1).trim() : null;

			if (line.isEmpty()) {
				continue;
			}
			if (nextLine != null && looksLikeIndex(line) && looksLikeTimestamp(nextLine)) {
				reset();
				currentIndex = line;
				lastParsed = Type.INDEX;
				continue;
			}

			if (currentIndex != null && looksLikeTimestamp(line)) {
				currentTimestamp = line;
				lastParsed = Type.TIMESTAMP;
				continue;
			}

			if (lastParsed == Type.TIMESTAMP || lastParsed == Type.CONTENT) {
				currentContent.add(line);
				lastParsed = Type.CONTENT;
				continue;
			}
			reset();
		}
		reset();
		logger.verbose("Entries found: " + entries.size());
		return new SubtitleFile(file, entries);
	}

	private void reset() {
		if (currentIndex != null && currentTimestamp != null) {
			var entry = new SubtitleEntry(Integer.parseInt(currentIndex), currentTimestamp, currentContent);
			entries.add(entry);
		}
		lastParsed = null;
		currentIndex = null;
		currentTimestamp = null;
		currentContent = new ArrayList<>();
	}
	private static boolean looksLikeIndex(String line) {
		return INDEX_PATTERN.matcher(line).matches();
	}
	private static boolean looksLikeTimestamp(String line) {
		return TIMESTAMP_PATTERN.matcher(line).matches();
	}
}
