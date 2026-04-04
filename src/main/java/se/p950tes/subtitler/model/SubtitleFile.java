package se.p950tes.subtitler.model;

import java.nio.file.Path;
import java.util.List;

public class SubtitleFile {

	private final Path path;
	private final List<SubtitleEntry> entries;

	public SubtitleFile(Path path, List<SubtitleEntry> entries) {
		this.path = path;
		this.entries = entries;
	}

	public Path getPath() {
		return path;
	}

	public List<SubtitleEntry> getEntries() {
		return entries;
	}

	@Override
	public String toString() {
		return "Subtitle [\n" + 
	    "  path: " + path + "\n" +
	    "  entries: \n" + entries + "]";
	}
}
