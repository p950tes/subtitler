package se.p950tes.subtitler.cli;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class SubtitlerCliTest {

	@Mock
	private SubtitlerCliExecutor executor;
	
	@Test
	void no_arguments() {
		assertNotEquals(0, execute());
		verifyNoInteractions(executor);
	}
	
	@Test
	void verbose_no_files() {
		execute("-v");
		verifyNoInteractions(executor);
	}
	@Test
	void verbose() {
		execute("-v", "someFile.srt");
		verify(executor).setVerbose(true);
		verify(executor).setInPlaceEditEnabled(false);
		verify(executor).setBackupSuffix(Optional.empty());
		verify(executor).setOperation(Operation.SCRUB);
		verify(executor).execute();
	}
	
	@Test
	void in_place_edit_no_backup() {
		execute("-i", "someFile.srt");
		verify(executor).setVerbose(false);
		verify(executor).setInPlaceEditEnabled(true);
		verify(executor).setBackupSuffix(Optional.empty());
		verify(executor).setOperation(Operation.SCRUB);
		verify(executor).execute();
	}
	@Test
	void in_place_edit_with_backup() {
		execute("-i.bak", "someFile.srt");
		verify(executor).setVerbose(false);
		verify(executor).setInPlaceEditEnabled(true);
		verify(executor).setBackupSuffix(Optional.of(".bak"));
		verify(executor).setOperation(Operation.SCRUB);
		verify(executor).execute();
	}
	
	@Test
	void multiple_files() {
		execute("-i.bak", "file1.srt", "file2.srt");
		verify(executor).setVerbose(false);
		verify(executor).setInPlaceEditEnabled(true);
		verify(executor).setBackupSuffix(Optional.of(".bak"));
		verify(executor).setInputFiles(pathOf("file1.srt", "file2.srt"));
		verify(executor).setOperation(Operation.SCRUB);
		verify(executor).execute();
	}
	
	@Test
	void merge_mode() {
		execute("--merge", "-o", "output.srt", "file1.srt", "file2.srt");
		verify(executor).setVerbose(false);
		verify(executor).setInputFiles(pathOf("file1.srt", "file2.srt"));
		verify(executor).setOutputFile(Path.of("output.srt"));
		verify(executor).setOperation(Operation.MERGE);
		verify(executor).execute();
	}

	private static List<Path> pathOf(String... paths) {
		return Arrays.stream(paths)
				.map(Path::of)
				.toList();
	}
	private int execute(String... args) {
		SubtitlerCli cli = new SubtitlerCli(executor);
		CommandLine commandLine = new CommandLine(cli);
    	return commandLine.execute(args);
	}
}
