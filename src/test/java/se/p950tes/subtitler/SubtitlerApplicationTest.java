package se.p950tes.subtitler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;

class SubtitlerApplicationTest {

	private FileManager fileManager = mock(FileManager.class);
	private Logger logger = new Logger();
	
	private SubtitlerApplication application = new SubtitlerApplication(logger, fileManager);
	
	@Test
	void no_arguments() {
		int status = application.execute();
		assertEquals(2, status);
	}
	
	@Test
	void input_file_does_not_exist() {
		Path inputFile = Path.of(URI.create("file:///path/input.srt"));
		when(fileManager.isReadableFile(inputFile)).thenReturn(false);
		int status = application.execute("/path/input.srt");
		assertEquals(2, status);
	}
	
	@Test
	void scrub_file_in_place() throws Exception {
		Path file = Path.of(URI.create("file:///path/file.srt"));
		when(fileManager.isReadableFile(file)).thenReturn(true);
		when(fileManager.isWritableFile(file)).thenReturn(true);
		when(fileManager.readLinesFromFile(file)).thenReturn(listFrom(
"""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.

2
00:05:33,385 --> 00:05:36,929
[SCREAMS]
"""));
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		when(fileManager.openOutputStream(file)).thenReturn(new PrintStream(outputStream));
		
		application.execute("/path/file.srt");
		int status = application.execute("-i", "/path/file.srt");
		assertEquals(0, status);
		
		String outputFileContents = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
		assertEquals("""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.

""", outputFileContents);
	}
	
	private static List<String> listFrom(String content) {
		return List.of(content.split("\n"));
	}
}
