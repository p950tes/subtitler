package se.p950tes.subtitler.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import se.p950tes.subtitler.model.SubtitleEntry;
import se.p950tes.subtitler.tooling.filtering.NoDuplicatePredicate;

public class EntryCollectionFinaliser {

	public static List<SubtitleEntry> finalise(List<SubtitleEntry> entryList) {
		return finalise(entryList.stream());
	}
	
	public static List<SubtitleEntry> finalise(Stream<SubtitleEntry> entryStream) {
		var entryList = entryStream.sorted(Comparator.comparing(SubtitleEntry::getTimestamp))
			.filter(SubtitleEntry::isNotEmpty)
			.sorted(Comparator.comparing(SubtitleEntry::getTimestamp))
			.filter(new NoDuplicatePredicate())
			.toList();

		correctIndexes(entryList);
		return entryList;
	}
	
	private static void correctIndexes(List<SubtitleEntry> entries) {
		for (int i = 0; i < entries.size(); i++) {
			entries.get(i).setIndex(i + 1);
		}
	}
}
