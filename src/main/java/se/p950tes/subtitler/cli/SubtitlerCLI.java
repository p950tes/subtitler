package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import se.p950tes.subtitler.cli.processing.DisableSpaceSeparatorPreprocessor;
import se.p950tes.subtitler.cli.processing.DurationTypeConverter;

@Command(
		name = "subtitler", 
		description = "Manage and process SRT subtitle files (scrub, merge, shift timings).",
		mixinStandardHelpOptions = true,
		sortOptions = false
	)
abstract class SubtitlerCLI {

    @ArgGroup(exclusive = true, multiplicity = "1", heading = "Operation mode (exactly one required):%n")
    Mode mode;
    
    static class Mode {
    	@Option(names = { "--scrub" }, 
    			description = "Scrub mode. Scrub all intput files from junk", 
    			defaultValue = "false")
    	boolean scrubMode;
    	
    	@Option(names = { "--merge" }, 
    			description = "Merge mode. Merge all intput files into one output file", 
    			defaultValue = "false")
    	boolean mergeMode;

        @Option(names = { "--shift" },
                description = "Shift subtitle timings by TIME (e.g., +2.5s, -1500ms, 1.2m, 0.5h).%n"
                        + "Supported units: ms (milliseconds), s (seconds), m (minutes), h (hours).",
                paramLabel = "TIME",
                converter = DurationTypeConverter.class)
        Duration shift;
    }
	
	@Option(names = { "-i", "--in-place" }, 
			description = "Edit files in place. Optional SUFFIX for backup, e.g., -i.bak.", 
			paramLabel = "backupSuffix", 
			required = false, 
			arity = "0..1", 
			fallbackValue = "", 
			preprocessor = DisableSpaceSeparatorPreprocessor.class)
	Optional<String> inPlaceEdit;

	@Option(names = { "-v", "--verbose" }, 
			description = "Verbose mode", 
			defaultValue = "false")
	boolean verbose;
	
	@Option(names = {"-o", "--output-file"},
			paramLabel = "outputFile", 
			description = "Write output to FILE. Use '-' for stdout (default).", 
			required = false, 
			arity = "0..1")
	Path outputFile;

	@Parameters(paramLabel = "inputFiles", 
			description = "Subtitle file(s) to process. If omitted, reads from stdin.", 
			arity = "0..*")
	List<Path> inputFiles;
	
}
