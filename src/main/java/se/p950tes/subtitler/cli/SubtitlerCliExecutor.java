package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import se.p950tes.subtitler.service.SubtitleMergerService;
import se.p950tes.subtitler.service.SubtitleScrubberService;
import se.p950tes.subtitler.util.FileManager;

public class SubtitlerCliExecutor {

	private final FileManager fileManager;

	private boolean verbose;
	private List<Path> inputFiles;
	private Operation operation;
	private Path outputFile;
	
	// Scrub options
	private boolean inPlaceEditEnabled;
	private Optional<String> backupSuffix;
	
	public SubtitlerCliExecutor(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	int execute() {

		if (! validateInputFiles(fileManager)) {
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
			if (! validateInputFile(file, fileManager)) {
				success = false;
			}
		}
		return success;
	}
	
	private boolean validateInputFile(Path file, FileManager fileManager) {
		
		if (! fileManager.isReadableFile(file)) {
			System.err.println("File is not readable: " + file.toAbsolutePath());
			return false;
		}
		if (inPlaceEditEnabled && fileManager.isWritableFile(file)) {
			System.err.println("In-place edit is enabled but file is not writeable: " + file.toAbsolutePath());
			return false;
		}
		if (backupSuffix.isPresent()) {
			Path backupFile = fileManager.resolveBackupFile(file, backupSuffix.get());
			if (fileManager.fileExists(backupFile)) {
				System.err.println("Backup file already exists: " + backupFile.toAbsolutePath());
				return false;
			} else if (! fileManager.isWritableFile(file)) {
				System.err.println("Backup file is not writable: " + backupFile.toAbsolutePath());
				return false;
			}
		}
		return true;
	}

	private void executeScrubOperation(FileManager fileManager) {
		SubtitleScrubberService scrubber = new SubtitleScrubberService(fileManager, inPlaceEditEnabled, backupSuffix, verbose);
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
