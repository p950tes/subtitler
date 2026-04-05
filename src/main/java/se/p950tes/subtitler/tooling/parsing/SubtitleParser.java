package se.p950tes.subtitler.tooling.parsing;

import java.io.BufferedReader;
import java.io.IOException;
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

	
	private BufferedReader fileReader;
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
		this.fileReader = fileManager.getFileReader(file);
		
		String currentLine = readNextLine();
		while (currentLine != null) {
			String nextLine = readNextLine();
			
			if (looksLikeIndex(currentLine) && looksLikeTimestamp(nextLine)) {
				saveCurrentAndStartNewEntry();
				currentIndex = currentLine;
				lastParsed = Type.INDEX;
			
			} else if (currentIndex != null && looksLikeTimestamp(currentLine)) {
				currentTimestamp = currentLine;
				lastParsed = Type.TIMESTAMP;
				
			} else if (lastParsed == Type.TIMESTAMP || lastParsed == Type.CONTENT) {
				currentContent.add(currentLine);
				lastParsed = Type.CONTENT;
			}
			currentLine = nextLine;
		}
		saveCurrentAndStartNewEntry();
		logger.verbose("Entries found: " + entries.size());
		return new SubtitleFile(file, entries);
	}
	
	private String readNextLine() {
		try {
			String nextLine = "";
			while (nextLine.isEmpty()) {
				nextLine = fileReader.readLine();
				if (nextLine == null) {
					// End of file
					return null;
				}
				nextLine = nextLine.trim();
			}
			return nextLine;
		} catch (IOException e) {
			throw new RuntimeException("Failed to read line from file", e);
		}
	}

	private void saveCurrentAndStartNewEntry() {
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
		if (line == null) {
			return false;
		}
		return INDEX_PATTERN.matcher(line).matches();
	}
	private static boolean looksLikeTimestamp(String line) {
		if (line == null) {
			return false;
		}
		return TIMESTAMP_PATTERN.matcher(line).matches();
	}
}
