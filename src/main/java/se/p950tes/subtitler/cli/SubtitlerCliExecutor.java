package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import se.p950tes.subtitler.service.SubtitleMergerService;
import se.p950tes.subtitler.service.SubtitleScrubberService;
import se.p950tes.subtitler.util.FileManager;

public class SubtitlerCliExecutor {

	private boolean verbose;
	private List<Path> inputFiles;
	private Operation operation;
	private Path outputFile;
	
	// Scrub options
	private boolean inPlaceEditEnabled;
	private Optional<String> backupSuffix;
	

	int execute() {
		FileManager fileManager = new FileManager(backupSuffix, verbose);

		if (!validateInputFiles(fileManager)) {
			return -1;
		}

		switch (operation) {
		case SCRUB:
			executeScrubOperation(fileManager);
			break;
		case MERGE:
			executeMergeOperation(fileManager);
			break;
		default:
			throw new IllegalStateException("Unsupported operation: " + operation);
		}
		return 0;
	}

	private boolean validateInputFiles(FileManager fileManager) {
		boolean success = true;
		
		for (Path file : inputFiles) {
			if (!fileManager.validateInputFile(file, inPlaceEditEnabled)) {
				success = false;
			}
		}
		return success;
	}

	private void executeScrubOperation(FileManager fileManager) {
		boolean backupOriginalFile = backupSuffix.isPresent();
		SubtitleScrubberService scrubber = new SubtitleScrubberService(fileManager, inPlaceEditEnabled, backupOriginalFile, verbose);
		for (Path file : inputFiles) {
			scrubber.processFile(file);
		}
	}
	private void executeMergeOperation(FileManager fileManager) {
		SubtitleMergerService service = new SubtitleMergerService(fileManager, verbose);
		service.merge(inputFiles, outputFile);
	}


	void setInputFiles(List<Path> inputFiles) {
		this.inputFiles = inputFiles;
	}

	void setInPlaceEditEnabled(boolean inPlaceEditEnabled) {
		this.inPlaceEditEnabled = inPlaceEditEnabled;
	}

	void setBackupSuffix(Optional<String> backupSuffix) {
		this.backupSuffix = backupSuffix;
	}

	void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	void setOperation(Operation operation) {
		this.operation = operation;
	}

	void setOutputFile(Path outputFile) {
		this.outputFile = outputFile;
	}
}
