package se.p950tes.subtitler;

import picocli.CommandLine;
import se.p950tes.subtitler.cli.SubtitlerCli;
import se.p950tes.subtitler.cli.SubtitlerCliExecutor;
import se.p950tes.subtitler.util.FileManager;

public class SubtitlerApplication {

	private final FileManager fileManager = new FileManager();
	private final SubtitlerCliExecutor executor = new SubtitlerCliExecutor(fileManager);
	private final SubtitlerCli cli = new SubtitlerCli(executor);
	private final CommandLine commandLine = new CommandLine(cli);
	
	private int execute(String[] args) {
		return commandLine.execute(args);
	}

	public static void main(String[] args) {
		SubtitlerApplication subtitler = new SubtitlerApplication();
		int exitCode = subtitler.execute(args);
		System.exit(exitCode);
	}
}
