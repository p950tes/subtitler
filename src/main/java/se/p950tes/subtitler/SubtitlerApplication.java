package se.p950tes.subtitler;

import picocli.CommandLine;
import se.p950tes.subtitler.cli.ArgumentProcessor;
import se.p950tes.subtitler.executor.SubtitlerExecutor;
import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.logging.Logger;

public class SubtitlerApplication {

	private final CommandLine commandLine;
	
	public SubtitlerApplication(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	int execute(String... args) {
		return commandLine.execute(args);
	}
	
	public static void main(String[] args) {
		var fileManager = new FileManager();
		var logger = new Logger();
		var executor = new SubtitlerExecutor(fileManager, logger);
		var argumentProcessor = new ArgumentProcessor(fileManager, logger, executor);
		var commandLine = new CommandLine(argumentProcessor);
		var application = new SubtitlerApplication(commandLine);
		
		int exitCode = application.execute(args);
		System.exit(exitCode);
	}
}
