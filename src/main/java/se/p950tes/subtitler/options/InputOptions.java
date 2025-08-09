package se.p950tes.subtitler.options;

import java.nio.file.Path;
import java.util.List;

public record InputOptions(
		List<Path> inputFiles,
		boolean stdIn
) {
	
	public static InputOptions withInputFiles(List<Path> inputFiles) {
		return new InputOptions(inputFiles, false);
	}
	public static InputOptions withStdIn() {
		return new InputOptions(List.of(), true);
	}
}
