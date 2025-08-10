package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;

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
		
		Stream<SubtitleEntry> combinedEntryStream = parsedFiles.stream()
				.flatMap(file -> file.getEntries().stream());
		
		List<SubtitleEntry> combinedEntries = EntryCollectionFinaliser.finalise(combinedEntryStream);
		
		printVerbose("Writing output file: " + outputFile);
		
		try (PrintStream fileOutputStream = fileManager.openOutputStream(outputFile)) {
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

	private void print(String line) {
		System.out.println(line);
	}
	private void printVerbose(String line) {
		if (verbose) {
			print(line);
		}
	}
}
