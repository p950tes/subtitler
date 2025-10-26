package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;

public class SubtitleMergerService {

	private final FileManager fileManager;
	private final Logger logger;
	
	public SubtitleMergerService(FileManager fileManager, Logger logger) {
		this.fileManager = fileManager;
		this.logger = logger;
	}

	public void merge(List<Path> inputFiles, Path outputFile) {
		logger.print("Processing: " + inputFiles);

		List<SubtitleFile> parsedFiles = inputFiles.stream()
				.map(this::parse)
				.toList();
		
		Stream<SubtitleEntry> combinedEntryStream = parsedFiles.stream()
				.flatMap(file -> file.getEntries().stream());
		
		List<SubtitleEntry> combinedEntries = EntryCollectionFinaliser.finalise(combinedEntryStream);
		
		logger.verbose("Writing output file: " + outputFile);
		
		try (PrintStream fileOutputStream = fileManager.openOutputStream(outputFile)) {
			writeSubtitleContents(combinedEntries, fileOutputStream);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to write output file", e);
		}
		
		logger.print("Input files: ");
		parsedFiles.forEach(file -> {
			logger.print(file.getPath() + ": " + file.getEntries().size());
		});
		logger.print("Output file:");
		logger.print(outputFile + ": " + combinedEntries.size());
	}
	
	private SubtitleFile parse(Path inputFile) {
		SubtitleParser parser = new SubtitleParser(fileManager, logger);
		return parser.parse(inputFile);
	}
	
	private void writeSubtitleContents(List<SubtitleEntry> entries, PrintStream outputStream) {
		for (SubtitleEntry entry : entries) {
			outputStream.println(entry.toFormattedEntry());
			outputStream.println();
		}
	}
}
