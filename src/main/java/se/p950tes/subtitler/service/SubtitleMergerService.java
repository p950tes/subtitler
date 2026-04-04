package se.p950tes.subtitler.service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.model.SubtitleEntry;
import se.p950tes.subtitler.model.SubtitleFile;
import se.p950tes.subtitler.tooling.parsing.SubtitleParser;
import se.p950tes.subtitler.tooling.parsing.SubtitleParserFactory;

public class SubtitleMergerService {

	private final FileManager fileManager;
	private final Logger logger;
	private final SubtitleParserFactory subtitleParserFactory;
	
	public SubtitleMergerService(Logger logger, FileManager fileManager, SubtitleParserFactory subtitleParserFactory) {
		this.logger = logger;
		this.fileManager = fileManager;
		this.subtitleParserFactory = subtitleParserFactory;
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
		SubtitleParser parser = subtitleParserFactory.newParser(inputFile);
		return parser.parse();
	}
	
	private void writeSubtitleContents(List<SubtitleEntry> entries, PrintStream outputStream) {
		for (SubtitleEntry entry : entries) {
			outputStream.println(entry.toFormattedEntry());
			outputStream.println();
		}
	}
}
