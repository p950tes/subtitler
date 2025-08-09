package se.p950tes.subtitler.options;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public record OutputOptions(
		/**
		 * If output should be written to a specific file
		 */
		Path outputFile,
		/**
		 * if output should be written to standard out.
		 */
		boolean stdOut,
		
		/**
		 * If output should be written to the input file
		 */
		boolean inPlaceEdit,
		
		/**
		 * If present a backup file should be created with the specified suffix
		 */
		Optional<String> backupFileSuffix
) {
	
	public static OutputOptions withOutputFile(Path outputFile) {
		return new OutputOptions(outputFile, false, false, Optional.empty());
	}
	public static OutputOptions withStdOut() {
		return new OutputOptions(null, true, false, Optional.empty());
	}
	public static OutputOptions withInPlaceEdit(String backupSuffix) {
		return new OutputOptions(null, false, true, Optional.ofNullable(StringUtils.trimToNull(backupSuffix)));
	}
}
