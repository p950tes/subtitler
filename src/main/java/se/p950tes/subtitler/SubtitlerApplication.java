package se.p950tes.subtitler;

import picocli.CommandLine;
import se.p950tes.subtitler.cli.ArgumentProcessor;
import se.p950tes.subtitler.executor.SubtitlerExecutor;
import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;

public class SubtitlerApplication {

	private final CommandLine commandLine;
	
	public SubtitlerApplication(Logger logger, FileManager fileManager) {
		var executor = new SubtitlerExecutor(fileManager, logger);
		var argumentProcessor = new ArgumentProcessor(fileManager, logger, executor);
		this.commandLine = new CommandLine(argumentProcessor);
	}

	protected int execute(String... args) {
		return commandLine.execute(args);
	}
	
	public static void main(String[] args) {
		var application = new SubtitlerApplication(new Logger(), new FileManager());
		int exitCode = application.execute(args);
		System.exit(exitCode);
	}
}
