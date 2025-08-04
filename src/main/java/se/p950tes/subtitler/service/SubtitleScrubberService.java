package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;
import se.p950tes.subtitler.util.FileManager;

public class SubtitleScrubberService {

	private final FileManager fileManager;
	private final boolean inPlaceEdit;
	private final Optional<String> backupSuffix;
	private final boolean verbose;
	
	public SubtitleScrubberService(FileManager fileManager, boolean inPlaceEdit, Optional<String> backupSuffix, boolean verbose) {
		this.fileManager = fileManager;
		this.inPlaceEdit = inPlaceEdit;
		this.backupSuffix = backupSuffix;
		this.verbose = verbose;
	}

	public void processFile(Path file) {
		print("Processing: " + file);

		SubtitleParser parser = new SubtitleParser(fileManager);
		SubtitleFile subtitle = parser.parse(file);

		EntryScrubber scrubber = new EntryScrubber();
		
		var newEntryStream = subtitle.getEntries().stream()
				.map(scrubber::scrub);

		List<SubtitleEntry> newEntries = EntryCollectionFinaliser.finalise(newEntryStream);
		
		if (inPlaceEdit) {
			if (backupSuffix.isPresent()) {
				Path backupFile = fileManager.resolveBackupFile(file, backupSuffix.get());
				printVerbose("Backing up original file to " + backupFile);
				fileManager.copy(file, backupFile);
			}
			replaceInputFile(file, newEntries);
		} else {
			writeSubtitleContents(newEntries, System.out);
		}
		
		printSummary(subtitle.getEntries(), newEntries);
	}

	private void printSummary(List<SubtitleEntry> oldEntries, List<SubtitleEntry> newEntries) {
		print(" * Entries modified: " + newEntries.stream().filter(SubtitleEntry::isModified).count());
		print(" * Entries removed: " + (oldEntries.size() - newEntries.size()));
		print(" * Entries remaining: " + newEntries.size());
	}

	private void replaceInputFile(Path originalPath, List<SubtitleEntry> entries) {
		printVerbose("Overwriting original file: " + originalPath);
		try (PrintStream fileOutputStream = fileManager.openOutputStream(originalPath)) {
			
			writeSubtitleContents(entries, fileOutputStream);
			
		} catch (Exception e) {
			throw new IllegalStateException("Failed to overwrite original file", e);
		}
	}
	
	private void writeSubtitleContents(List<SubtitleEntry> entries, PrintStream outputStream) {
		for (SubtitleEntry entry : entries) {
			outputStream.println(entry.toFormattedEntry());
			outputStream.println();
		}
	}

	private void print(String line) {
		System.out.println(line);
	}
	private void printVerbose(String line) {
		if (verbose) {
			print(line);
		}
	}
}
