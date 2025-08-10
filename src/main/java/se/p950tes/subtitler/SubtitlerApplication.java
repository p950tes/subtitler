package se.p950tes.subtitler;

import picocli.CommandLine;
import se.p950tes.subtitler.cli.SubtitlerCliExecutor;
import se.p950tes.subtitler.options.InputOptions;
import se.p950tes.subtitler.options.OutputOptions;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.options.TransformationOptions;
import se.p950tes.subtitler.util.FileManager;

public class SubtitlerApplication extends SubtitlerCLI {

	private final SubtitlerCliExecutor executor;
	
	public SubtitlerApplication(SubtitlerCliExecutor executor) {
		this.executor = executor;
	}

	int execute(String... args) {
		CommandLine commandLine = new CommandLine(this);
		return commandLine.execute(args);
	}
	
	@Override
	public Integer call() {
		SubtitlerArguments arguments = parseArgs();
		return executor.execute(arguments);
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
	
	public static void main(String[] args) {
		var executor = new SubtitlerCliExecutor(new FileManager());
		var subtitler = new SubtitlerApplication(executor);
		int exitCode = subtitler.execute(args);
		System.exit(exitCode);
	}
}
