package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine.ExitCode;
import picocli.CommandLine.ParameterException;
import se.p950tes.subtitler.executor.SubtitlerExecutor;
import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.options.InputOptions;
import se.p950tes.subtitler.options.OutputOptions;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.options.TransformationOptions;

public class ArgumentProcessor extends SubtitlerCLI implements Callable<Integer> {

	private final FileManager fileManager;
	private final Logger logger;
	private final SubtitlerExecutor executor;
	
	public ArgumentProcessor(FileManager fileManager, Logger logger, SubtitlerExecutor executor) {
		this.fileManager = fileManager;
		this.logger = logger;
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
		if (mode == null) {
			// Make scrubMode default if none are selected
			mode = new Mode();
			mode.scrubMode = true;
		}
		TransformationOptions options = TransformationOptions.create()
				.withMergeMode(mode.mergeMode)
				.withScrubMode(mode.scrubMode)
				.withTimeShiftMode(mode.shift);
		
		return options;
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
		
		if (transformationOptions.timeShiftMode()) {
			logger.error("Time-shift operation not yet implemented");
			return false;
		}
		if (inputOptions.stdIn() && !fileManager.isStdinAvailable()) {
            throw new ParameterException(spec.commandLine(), "No input specified and stdin is empty");
		}
		if (transformationOptions.mergeMode() && outputOptions.inPlaceEdit()) {
			throw new ParameterException(spec.commandLine(), "Cannot combine merge mode with in-place edit");
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
			logger.error("File is not readable: " + file.toAbsolutePath());
			return false;
		}
		if (outputOptions.inPlaceEdit() && !fileManager.isWritableFile(file)) {
			logger.error("In-place edit is enabled but file is not writeable: " + file.toAbsolutePath());
			return false;
		}
		if (outputOptions.backupFileSuffix().isPresent()) {
			Path backupFile = fileManager.resolveBackupFile(file, outputOptions.backupFileSuffix().get());
			if (fileManager.fileExists(backupFile)) {
				logger.error("Backup file already exists: " + backupFile.toAbsolutePath());
				return false;
			} else if (! fileManager.isWritableFile(file)) {
				logger.error("Backup file is not writable: " + backupFile.toAbsolutePath());
				return false;
			}
		}
		return true;
	}
}
