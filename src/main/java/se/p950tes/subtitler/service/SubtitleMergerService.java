package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.ObjectUtils;

import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;
import se.p950tes.subtitler.util.FileManager;

public class SubtitleMergerService {

	private final FileManager fileManager;
	private final boolean verbose;
	
	public SubtitleMergerService(FileManager fileManager, boolean verbose) {
		this.fileManager = fileManager;
		this.verbose = verbose;
	}

	public void merge(List<Path> inputFiles, Path outputFile) {
		print("Processing: " + inputFiles);

		List<SubtitleFile> parsedFiles = inputFiles.stream()
				.map(this::parse)
				.toList();
		
		List<SubtitleEntry> combinedEntries = parsedFiles.stream()
				.flatMap(file -> file.getEntries().stream())
				.sorted(Comparator.comparing(SubtitleEntry::getTimestamp))
				.filter(new NoDuplicatePredicate())
				.toList();
		
		correctIndexes(combinedEntries);
		
		printVerbose("Writing output file: " + outputFile);
		
		try (PrintStream fileOutputStream = fileManager.openPrintOutputStream(outputFile)) {
			writeSubtitleContents(combinedEntries, fileOutputStream);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to write output file", e);
		}
		
		print("Input files: ");
		parsedFiles.forEach(file -> {
			print(file.getPath() + ": " + file.getEntries().size());
		});
		print("Output file:");
		print(outputFile + ": " + combinedEntries.size());
	}
	
	private SubtitleFile parse(Path inputFile) {
		SubtitleParser parser = new SubtitleParser(fileManager);
		return parser.parse(inputFile);
	}
	
	private void writeSubtitleContents(List<SubtitleEntry> entries, PrintStream outputStream) {
		for (SubtitleEntry entry : entries) {
			outputStream.println(entry.toFormattedEntry());
			outputStream.println();
		}
	}

	private void correctIndexes(List<SubtitleEntry> entries) {
		for (int i = 0; i < entries.size(); i++) {
			entries.get(i).setIndex(i + 1);
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
	
	private static class NoDuplicatePredicate implements Predicate<SubtitleEntry> {

		private SubtitleEntry previousEntry;
		
		@Override
		public boolean test(SubtitleEntry currentEntry) {
			if (ObjectUtils.anyNull(previousEntry, currentEntry)) {
				previousEntry = currentEntry;
				return true;
			}
			if (Objects.equals(previousEntry.getTimestamp(), currentEntry.getTimestamp())) {
				previousEntry = currentEntry;
				return false;
			}
			if (Objects.equals(previousEntry.getLines(), currentEntry.getLines())) {
				previousEntry = currentEntry;
				return false;
			}
			previousEntry = currentEntry;
			return true;
		}
	}
}
