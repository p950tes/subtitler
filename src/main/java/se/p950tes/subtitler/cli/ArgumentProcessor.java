package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.ExitCode;
import se.p950tes.subtitler.executor.SubtitlerExecutor;
import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.options.InputOptions;
import se.p950tes.subtitler.options.OutputOptions;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.options.TransformationOptions;

public class ArgumentProcessor extends SubtitlerCLI implements Callable<Integer> {

	private final FileManager fileManager;
	private final SubtitlerExecutor executor;
	
	public ArgumentProcessor(FileManager fileManager, SubtitlerExecutor executor) {
		this.fileManager = fileManager;
		this.executor = executor;
	}

	@Override
	public Integer call() {
		SubtitlerArguments arguments = parseArgs();
		if (! validateArguments(arguments)) {
			return ExitCode.USAGE;
		}
		executor.execute(arguments);
		return ExitCode.OK;
	}
	
	private SubtitlerArguments parseArgs() {
		return SubtitlerArguments.create()
				.withTransformationOptions(createTransformationOptions())
				.withInputOptions(createInputOptions())
				.withOutputOptions(createOutputOptions())
				.withVerbose(verbose);
	}

	private TransformationOptions createTransformationOptions() {
		return TransformationOptions.create()
				.withMergeMode(mode.mergeMode)
				.withScrubMode(mode.scrubMode)
				.withTimeShiftMode(mode.shift);
	}
	
	private InputOptions createInputOptions() {
		if (inputFiles == null || inputFiles.isEmpty()) {
			return InputOptions.withStdIn();
		}
		return InputOptions.withInputFiles(inputFiles);
	}
	
	private OutputOptions createOutputOptions() {
		if (outputFile != null) {
			return OutputOptions.withOutputFile(outputFile);
		}
		if (inPlaceEdit.isPresent()) {
			return OutputOptions.withInPlaceEdit(inPlaceEdit.get());
		}
		return OutputOptions.withStdOut();
	}
	
	private boolean validateArguments(SubtitlerArguments args) {
		var inputOptions = args.inputOptions();
		var outputOptions = args.outputOptions();
		var transformationOptions = args.transformationOptions();
		
		if (inputOptions.stdIn() && !fileManager.isStdinAvailable()) {
			System.err.println("No input specified and stdin is empty");
			return false;
		}
		if (transformationOptions.mergeMode() && outputOptions.inPlaceEdit()) {
			System.err.println("Cannot combine merge mode with in-place edit");
			return false;
		}
		
		for (Path file : inputOptions.inputFiles()) {
			if (! validateInputFile(file, args)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean validateInputFile(Path file, SubtitlerArguments args) {
		var outputOptions = args.outputOptions();
		if (! fileManager.isReadableFile(file)) {
			System.err.println("File is not readable: " + file.toAbsolutePath());
			return false;
		}
		if (outputOptions.inPlaceEdit() && !fileManager.isWritableFile(file)) {
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
}
