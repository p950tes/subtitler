package se.p950tes.subtitler.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "subtitler", description = "Scrubs HI notations and other junk data from subtitle files")
public class SubtitlerCli implements Callable<Integer> {

	private final SubtitlerCliExecutor executor;

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

	@Option(names = { "--scrub" }, 
			description = "Scrub mode. Scrub all intput files from junk", 
			defaultValue = "false")
	private boolean scrubMode;
	
	@Option(names = { "--merge" }, 
			description = "Merge mode. Merge all intput files into one output file", 
			defaultValue = "false")
	private boolean mergeMode;
	
	@Option(names = {"-o", "--output-file"},
			paramLabel = "outputFile", 
			description = "Output file. Only relevant in merge mode", 
			required = false, 
			arity = "0..1")
	private Path outputFile;

	@Parameters(paramLabel = "inputFiles", 
			description = "Input file(s)", 
			arity = "1..*")
	private List<Path> inputFiles;
	

	public SubtitlerCli(SubtitlerCliExecutor executor) {
		this.executor = executor;
	}

	@Override
	public Integer call() {
		executor.setOperation(resolveOperation());
		executor.setInputFiles(inputFiles);
		executor.setOutputFile(outputFile);
		executor.setInPlaceEditEnabled(inPlaceEdit.isPresent());
		executor.setBackupSuffix(resolveBackupFileSuffix(inPlaceEdit));
		executor.setVerbose(verbose);
		return executor.execute();
	}

	private Operation resolveOperation() {
		if (mergeMode) {
			return Operation.MERGE;
		}
		// Default operation is scrub
		return Operation.SCRUB;
	}

	private static Optional<String> resolveBackupFileSuffix(Optional<String> inPlaceEdit) {
		if (inPlaceEdit.isEmpty()) {
			return Optional.empty();
		}
		String backupSuffix = StringUtils.trimToNull(inPlaceEdit.get());
		return Optional.ofNullable(backupSuffix);
	}
}
