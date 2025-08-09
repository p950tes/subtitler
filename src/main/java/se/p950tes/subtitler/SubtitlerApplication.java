package se.p950tes.subtitler;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import se.p950tes.subtitler.cli.SubtitlerCliExecutor;
import se.p950tes.subtitler.cli.argprocessing.DisableSpaceSeparatorPreprocessor;
import se.p950tes.subtitler.cli.argprocessing.DurationConverter;
import se.p950tes.subtitler.options.InputOptions;
import se.p950tes.subtitler.options.OutputOptions;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.options.TransformationOptions;
import se.p950tes.subtitler.util.FileManager;

@Command(
		name = "subtitler", 
		description = "Manage and process SRT subtitle files (scrub, merge, shift timings).",
		mixinStandardHelpOptions = true,
		sortOptions = false
	)
public class SubtitlerApplication implements Callable<Integer> {

	@Option(names = { "--scrub" }, 
			description = "Scrub mode. Scrub all intput files from junk", 
			defaultValue = "false")
	private boolean scrubMode;
	
	@Option(names = { "--merge" }, 
			description = "Merge mode. Merge all intput files into one output file", 
			defaultValue = "false")
	private boolean mergeMode;

    @Option(names = { "--shift" },
            description = "Shift subtitle timings by TIME (e.g., +2.5s, -1500ms).",
            paramLabel = "TIME",
            converter = DurationConverter.class)
    private Duration shift;
	
	@Option(names = { "-i", "--in-place" }, 
			description = "Edit files in place. If a suffix parameter is specified then the original file will be kept behind with the specified suffix.", 
			paramLabel = "backupSuffix", 
			required = false, 
			arity = "0..1", 
			fallbackValue = "", 
			preprocessor = DisableSpaceSeparatorPreprocessor.class)
	private Optional<String> inPlaceEdit;

	@Option(names = { "-v", "--verbose" }, 
			description = "Verbose mode", 
			defaultValue = "false")
	private boolean verbose;
	
	@Option(names = {"-o", "--output-file"},
			paramLabel = "outputFile", 
			description = "Write output to FILE. Use '-' for stdout (default).", 
			required = false, 
			arity = "0..1")
	private Path outputFile;

	@Parameters(paramLabel = "inputFiles", 
			description = "Subtitle file(s) to process. If omitted, reads from stdin.", 
			arity = "0..*")
	private List<Path> inputFiles;
	
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
				.withMergeMode(mergeMode)
				.withScrubMode(scrubMode)
				.withTimeShiftMode(shift);
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
