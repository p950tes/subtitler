package se.p950tes.subtitler.cli;

import java.io.IOException;
import java.nio.file.Path;

import picocli.CommandLine;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.service.SubtitleMergerService;
import se.p950tes.subtitler.service.SubtitleScrubberService;
import se.p950tes.subtitler.util.FileManager;

public class SubtitlerCliExecutor {

	private final FileManager fileManager;

	private SubtitlerArguments args;
	
	public SubtitlerCliExecutor(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public int execute(SubtitlerArguments arguments) {
		this.args = arguments; 
		
		if (! validateArguments()) {
			return CommandLine.ExitCode.USAGE;
		}

		if (args.transformationOptions().scrubMode()) {
			executeScrubOperation();
		}
		if (args.transformationOptions().mergeMode()) {
			executeMergeOperation();
		}
		return CommandLine.ExitCode.OK;
	}

	private boolean validateArguments() {
		var inputOptions = args.inputOptions();
		var outputOptions = args.outputOptions();
		var transformationOptions = args.transformationOptions();
		
		if (inputOptions.stdIn() && !isStdinAvailable()) {
			System.err.println("No input specified and stdin is empty");
			return false;
		}
		if (transformationOptions.mergeMode() && outputOptions.inPlaceEdit()) {
			System.err.println("Cannot combine merge mode with in-place edit");
			return false;
		}
		
		for (Path file : inputOptions.inputFiles()) {
			if (! validateInputFile(file)) {
				return false;
			}
		}
		return true;
	}
	
    private boolean isStdinAvailable() {
        try {
            if (System.console() != null) {
                return false; // interactive terminal --> no piped input
            }
            // If something is already buffered in stdin
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }
	
	private boolean validateInputFile(Path file) {
		var outputOptions = args.outputOptions();
		if (! fileManager.isReadableFile(file)) {
			System.err.println("File is not readable: " + file.toAbsolutePath());
			return false;
		}
		if (outputOptions.inPlaceEdit() && fileManager.isWritableFile(file)) {
			System.err.println("In-place edit is enabled but file is not writeable: " + file.toAbsolutePath());
			return false;
		}
		if (outputOptions.backupFileSuffix().isPresent()) {
			Path backupFile = fileManager.resolveBackupFile(file, outputOptions.backupFileSuffix().get());
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

	private void executeScrubOperation() {
		SubtitleScrubberService scrubber = new SubtitleScrubberService(fileManager, args.outputOptions().inPlaceEdit(), args.outputOptions().backupFileSuffix(), args.verbose());
		for (Path file : args.inputOptions().inputFiles()) {
			scrubber.processFile(file);
		}
	}
	private void executeMergeOperation() {
		SubtitleMergerService service = new SubtitleMergerService(fileManager, args.verbose());
		service.merge(args.inputOptions().inputFiles(), args.outputOptions().outputFile());
	}
}
