package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;

public class SubtitleScrubberService {

	private final FileManager fileManager;
	private final boolean inPlaceEdit;
	private final Optional<String> backupSuffix;
	private final Logger logger;
	
	public SubtitleScrubberService(FileManager fileManager, Logger logger, boolean inPlaceEdit, Optional<String> backupSuffix) {
		this.fileManager = fileManager;
		this.logger = logger;
		this.inPlaceEdit = inPlaceEdit;
		this.backupSuffix = backupSuffix;
	}

	public void processFile(Path file) {
		logger.print("Processing: " + file);

		SubtitleParser parser = new SubtitleParser(fileManager, logger);
		SubtitleFile subtitle = parser.parse(file);

		EntryScrubber scrubber = new EntryScrubber(logger);
		
		var newEntryStream = subtitle.getEntries().stream()
				.map(scrubber::scrub);

		List<SubtitleEntry> newEntries = EntryCollectionFinaliser.finalise(newEntryStream);
		
		if (inPlaceEdit) {
			if (backupSuffix.isPresent()) {
				Path backupFile = fileManager.resolveBackupFile(file, backupSuffix.get());
				logger.verbose("Backing up original file to " + backupFile);
				fileManager.copy(file, backupFile);
			}
			replaceInputFile(file, newEntries);
		} else {
			writeSubtitleContents(newEntries, System.out);
		}
		
		printSummary(subtitle.getEntries(), newEntries);
	}

	private void printSummary(List<SubtitleEntry> oldEntries, List<SubtitleEntry> newEntries) {
		logger.print(" * Entries modified: " + newEntries.stream().filter(SubtitleEntry::isModified).count());
		logger.print(" * Entries removed: " + (oldEntries.size() - newEntries.size()));
		logger.print(" * Entries remaining: " + newEntries.size());
	}

	private void replaceInputFile(Path originalPath, List<SubtitleEntry> entries) {
		logger.verbose("Overwriting original file: " + originalPath);
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
}
