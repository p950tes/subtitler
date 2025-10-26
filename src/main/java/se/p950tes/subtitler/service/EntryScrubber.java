package se.p950tes.subtitler.service;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.p950tes.subtitler.service.model.SubtitleEntry;

class EntryScrubber {

	private static final Pattern HTML_PATTERN = Pattern.compile("<[^<>]*>");

	// (SCREAM) [SCREAM] {SCREAM}
	private static final Pattern BRACKET_PATTERN = Pattern.compile("[\\[\\{\\(][^\\[\\]\\{\\}\\(\\)]*[\\]\\}\\)]");
	
	// junk artifacts (J“ / j“ / [M1 )
	private static final Pattern JUNK_PATTERN = Pattern.compile("("
				+ "^([jJ]“)+"
				+ "|([jJ]“)+$"
				+ "|^\\[M1$"
				+ "|^[\\-\\.:]+$"
				+ "|^[ ♪]+$"
			+ ")");
	
	// All caps, at least 3 characters
	private static final Pattern ALL_CAPS_PATTERN = Pattern.compile("^[A-Z ,\\!\\-]{3,}$");
	
	// Names followed by colon: (Guard 1: Hello there)
	private static final Pattern VOICE_INDICATORS_PATTERN = Pattern.compile("^[\\- ]*[A-Za-z]{2}[A-Za-z0-9 \'&\\-]*\\s?: *");
	private static final Pattern VOICE_INDICATORS_MID_LINE_PATTERN = Pattern.compile("[\\- ]*[A-Z]{2}[A-Z0-9&\\-]*\\s?:");
	
	// All caps, at least 3 characters
	private static final Pattern ONLY_JUNK_CHARACTERS = Pattern.compile("^[^A-Za-z0-9]*$");
	
	private static final Pattern JAPANESE_JUNK_CHARACTERS = Pattern.compile("^m [0-9mbl \\.\\-]+$");

	
	public SubtitleEntry scrub(SubtitleEntry entry) {
		
		String linesAsString = String.join("\n", entry.getLines());
		linesAsString = scrubEntry(linesAsString);

		var newLines = Arrays.stream(linesAsString.split("\n"))
				.map(this::scrubEntry)
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.toList();
		
		SubtitleEntry newEntry = new SubtitleEntry(entry.getIndex(), entry.getTimestamp(), newLines);
		newEntry.setModified(! entry.equals(newEntry));
		return newEntry;
	}
	
	private String scrubEntry(final String original) {
		String modified = original;
		String lastIteration = null;
		do {
			lastIteration = modified;
			modified = removeAll(HTML_PATTERN, modified);
			modified = removeAll(BRACKET_PATTERN, modified);
			modified = removeAll(JUNK_PATTERN, modified);
	
			modified = removeAll(VOICE_INDICATORS_PATTERN, modified);
			modified = removeAll(VOICE_INDICATORS_MID_LINE_PATTERN, modified);
			modified = removeAll(ALL_CAPS_PATTERN, modified);
			modified = removeAll(JUNK_PATTERN, modified);
			modified = removeAll(ONLY_JUNK_CHARACTERS, modified);
			modified = removeAll(JAPANESE_JUNK_CHARACTERS, modified);
			modified = modified.trim();
		} while (! Objects.equals(lastIteration, modified));
		return modified;
	}

	private static String removeAll(Pattern patternToRemove, String value) {
		var matcher = patternToRemove.matcher(value);
		return matcher.replaceAll("");
	}
}

