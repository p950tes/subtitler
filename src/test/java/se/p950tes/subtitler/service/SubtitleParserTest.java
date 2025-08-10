package se.p950tes.subtitler.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.service.model.SubtitleEntry;
import se.p950tes.subtitler.service.model.SubtitleFile;

@ExtendWith(MockitoExtension.class)
class SubtitleParserTest {

	@Mock
	private Path file;
	@Mock
	private FileManager fileManager;
	
	@InjectMocks
	private SubtitleParser parser;
	
	@Test
	void single_entry() {
		when(fileManager.readLinesFromFile(file)).thenReturn(listFrom(
"""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.
"""));
		
		SubtitleFile result = parser.parse(file);
		assertEquals(file, result.getPath());
		assertEquals(1, result.getEntries().size());
		
		SubtitleEntry entry = result.getEntries().get(0);
		assertEquals(1, entry.getIndex());
		assertEquals("00:01:33,385 --> 00:01:36,929", entry.getTimestamp());
		assertEquals(List.of("There is no stopping in the red zone."), entry.getLines());
	}
	
	@Test
	void two_entries() {
		when(fileManager.readLinesFromFile(file)).thenReturn(listFrom(
"""
1
00:01:33,385 --> 00:01:36,929
There is no stopping in the red zone.

2
00:01:37,014 --> 00:01:42,226
The white zone is for immediate loading
and unloading of passengers only.
"""));
		
		SubtitleFile result = parser.parse(file);
		assertEquals(file, result.getPath());
		
		List<SubtitleEntry> entries = result.getEntries();
		assertEquals(2, entries.size());
		
		SubtitleEntry entry1 = entries.get(0);
		assertEquals(1, entry1.getIndex());
		assertEquals("00:01:33,385 --> 00:01:36,929", entry1.getTimestamp());
		assertEquals(List.of("There is no stopping in the red zone."), entry1.getLines());
		
		SubtitleEntry entry2 = entries.get(1);
		assertEquals(2, entry2.getIndex());
		assertEquals("00:01:37,014 --> 00:01:42,226", entry2.getTimestamp());
		assertEquals(List.of("The white zone is for immediate loading", "and unloading of passengers only."), entry2.getLines());
	}

	private static List<String> listFrom(String content) {
		return List.of(content.split("\n"));
	}
}
