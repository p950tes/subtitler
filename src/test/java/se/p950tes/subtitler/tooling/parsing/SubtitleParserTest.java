package se.p950tes.subtitler.tooling.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.p950tes.subtitler.io.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.model.SubtitleEntry;
import se.p950tes.subtitler.model.SubtitleFile;

@ExtendWith(MockitoExtension.class)
class SubtitleParserTest {

	@Mock
	private Path file;
	@Mock
	private FileManager fileManager;
	@Mock
	private Logger logger;
	
	@InjectMocks
	private SubtitleParserFactory factory;
	
	@Test
	void index_only() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""
1
"""));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		assertEquals(0, result.getEntries().size());
	}
	
	@Test
	void single_entry() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.
"""));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		assertEquals(1, result.getEntries().size());
		assertEquals(new SubtitleEntry(1, "00:01:33,385 --> 00:01:36,929", List.of("There is no stopping in the red zone.")), result.getEntries().get(0));
	}
	
	@Test
	void two_entries() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.

2
00:01:37,014 --> 00:01:42,226
The white zone is for immediate loading
and unloading of passengers only.
"""));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		
		List<SubtitleEntry> entries = result.getEntries();
		assertEquals(2, entries.size());
		assertEquals(new SubtitleEntry(1, "00:01:33,385 --> 00:01:36,929", List.of("There is no stopping in the red zone.")), result.getEntries().get(0));
		assertEquals(new SubtitleEntry(2, "00:01:37,014 --> 00:01:42,226", List.of("The white zone is for immediate loading", "and unloading of passengers only.")), result.getEntries().get(1));
	}
	
	@Test
	void junk_at_beginning() {
		when(fileManager.getFileReader(file)).thenReturn(new BufferedReader(new StringReader(
"""
junk
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.
""")));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		assertEquals(1, result.getEntries().size());
		assertEquals(new SubtitleEntry(1, "00:01:33,385 --> 00:01:36,929", List.of("There is no stopping in the red zone.")), result.getEntries().get(0));
	}
	
	@Test
	void empty_entries() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""
1
00:00:01,000 --> 00:00:02,000
2
00:00:02,000 --> 00:00:03,000
There is no stopping in the red zone.
3
00:00:03,000 --> 00:00:04,000
4
00:00:04,000 --> 00:00:05,000
The white zone is for immediate loading
and unloading of passengers only.
5
00:00:05,000 --> 00:00:06,000
"""));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		List<SubtitleEntry> entries = result.getEntries();
		assertEquals(5, entries.size());
		
		assertEquals(new SubtitleEntry(1, "00:00:01,000 --> 00:00:02,000", List.of()), entries.get(0));
		assertEquals(new SubtitleEntry(2, "00:00:02,000 --> 00:00:03,000", List.of("There is no stopping in the red zone.")), entries.get(1));
		assertEquals(new SubtitleEntry(3, "00:00:03,000 --> 00:00:04,000", List.of()), entries.get(2));
		assertEquals(new SubtitleEntry(4, "00:00:04,000 --> 00:00:05,000", List.of("The white zone is for immediate loading", "and unloading of passengers only.")), entries.get(3));
		assertEquals(new SubtitleEntry(5, "00:00:05,000 --> 00:00:06,000", List.of()), entries.get(4));
	}
	
	@Test
	void whitespace() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""

1
00:00:01,000 --> 00:00:02,000


2
00:00:02,000 --> 00:00:03,000
There is no stopping in the red zone.


3
00:00:05,000 --> 00:00:06,000



"""));
		
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		List<SubtitleEntry> entries = result.getEntries();
		assertEquals(3, entries.size());
		
		assertEquals(new SubtitleEntry(1, "00:00:01,000 --> 00:00:02,000", List.of()), entries.get(0));
		assertEquals(new SubtitleEntry(2, "00:00:02,000 --> 00:00:03,000", List.of("There is no stopping in the red zone.")), entries.get(1));
		assertEquals(new SubtitleEntry(3, "00:00:05,000 --> 00:00:06,000", List.of()), entries.get(2));
	}
	
	@Test
	void whitespace_in_single_entry() {
		when(fileManager.getFileReader(file)).thenReturn(readerFrom(
"""

1


00:00:01,000 --> 00:00:02,000


The white zone is for immediate loading


and unloading of passengers only.


"""));
		SubtitleFile result = factory.newParser(file).parse();
		assertEquals(file, result.getPath());
		List<SubtitleEntry> entries = result.getEntries();
		assertEquals(1, entries.size());
		
		assertEquals(new SubtitleEntry(1, "00:00:01,000 --> 00:00:02,000", List.of("The white zone is for immediate loading", "and unloading of passengers only.")), entries.get(0));
	}

	private static BufferedReader readerFrom(String content) {
		return new BufferedReader(new StringReader(content));
	}
}
