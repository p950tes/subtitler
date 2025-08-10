package se.p950tes.subtitler.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import picocli.CommandLine;
import se.p950tes.subtitler.executor.SubtitlerExecutor;
import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.options.SubtitlerArguments;

@ExtendWith(MockitoExtension.class)
class ArgumentProcessorTest {

	@Captor
	private ArgumentCaptor<SubtitlerArguments> captor;
	
	private FileManager fileManager = mock(FileManager.class);
	private SubtitlerExecutor executor = mock(SubtitlerExecutor.class);;
	
	private ArgumentProcessor processor = new ArgumentProcessor(fileManager, executor);
	private CommandLine commandLine = new CommandLine(processor);
	
	@Test
	void no_arguments() {
		assertNotEquals(0, commandLine.execute());
		verifyNoInteractions(executor);
	}
	
	@Test
	void verbose_no_files() {
		commandLine.execute("-v");
		verifyNoInteractions(executor);
	}
	@Test
	void verbose() {
		when(fileManager.isReadableFile(Path.of("someFile.srt"))).thenReturn(true);
		commandLine.execute("-v", "--scrub", "someFile.srt");
		verifyArgs(args -> {
			assertTrue(args.verbose(), "verbose");
		});
	}
	
	@Test
	void in_place_scrub_no_backup() {
		when(fileManager.isReadableFile(Path.of("someFile.srt"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("someFile.srt"))).thenReturn(true);
		
		commandLine.execute("-i", "--scrub", "someFile.srt");
		verifyArgs(args -> {
			assertFalse(args.verbose(), "verbose");
			assertTrue(args.outputOptions().inPlaceEdit(), "In place edit");
			assertTrue(args.outputOptions().backupFileSuffix().isEmpty(), "backupFileSuffix");
			assertTrue(args.transformationOptions().scrubMode(), "scrubMode");
			assertFalse(args.transformationOptions().mergeMode(), "mergeMode");
		});
	}
	@Test
	void in_place_scrub_with_backup() {
		when(fileManager.isReadableFile(Path.of("someFile.srt"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("someFile.srt.bak"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("someFile.srt"))).thenReturn(true);
		
		commandLine.execute("-i.bak", "--scrub", "someFile.srt");
		verifyArgs(args -> {
			assertFalse(args.verbose(), "verbose");
			assertTrue(args.outputOptions().inPlaceEdit(), "In place edit");
			assertEquals(pathsOf("someFile.srt"), args.inputOptions().inputFiles());
			assertEquals(Optional.of(".bak"), args.outputOptions().backupFileSuffix());
			assertTrue(args.transformationOptions().scrubMode(), "scrubMode");
			assertFalse(args.transformationOptions().mergeMode(), "mergeMode");
		});
	}
	
	@Test
	void multiple_files() {
		when(fileManager.isReadableFile(Path.of("file1.srt"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("file1.srt"))).thenReturn(true);
		when(fileManager.isReadableFile(Path.of("file2.srt"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("file2.srt"))).thenReturn(true);
		
		commandLine.execute("-i", "--scrub", "file1.srt", "file2.srt");
		
		verifyArgs(args -> {
			assertFalse(args.verbose(), "verbose");
			assertTrue(args.outputOptions().inPlaceEdit(), "In place edit");
			assertEquals(args.outputOptions().backupFileSuffix(), Optional.empty());
			assertEquals(pathsOf("file1.srt", "file2.srt"), args.inputOptions().inputFiles());
			assertTrue(args.transformationOptions().scrubMode(), "scrubMode");
		});
	}
	
	@Test
	void merge_mode() {
		when(fileManager.isReadableFile(Path.of("file1.srt"))).thenReturn(true);
		when(fileManager.isReadableFile(Path.of("file2.srt"))).thenReturn(true);
		when(fileManager.isWritableFile(Path.of("output.srt"))).thenReturn(true);
		
		
		commandLine.execute("--merge", "-o", "output.srt", "file1.srt", "file2.srt");
		
		verifyArgs(args -> {
			assertFalse(args.verbose(), "verbose");
			assertEquals(pathsOf("file1.srt", "file2.srt"), args.inputOptions().inputFiles());
			assertEquals(Path.of("output.srt"), args.outputOptions().outputFile());
			assertTrue(args.transformationOptions().mergeMode(), "mergeMode");
			assertFalse(args.transformationOptions().scrubMode(), "scrubMode");
		});
	}

	private static List<Path> pathsOf(String... paths) {
		return Arrays.stream(paths)
				.map(Path::of)
				.toList();
	}
	private void verifyArgs(Consumer<SubtitlerArguments> verification) {
		verify(executor).execute(captor.capture());
		verification.accept(captor.getValue());
	}
}
