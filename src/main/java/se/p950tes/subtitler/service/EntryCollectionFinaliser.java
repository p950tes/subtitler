package se.p950tes.subtitler.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;

import se.p950tes.subtitler.model.SubtitleEntry;

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
	
	private static class NoDuplicatePredicate implements Predicate<SubtitleEntry> {

		private SubtitleEntry previousEntry;
		
		@Override
		public boolean test(SubtitleEntry currentEntry) {
			if (ObjectUtils.anyNull(previousEntry, currentEntry)) {
				previousEntry = currentEntry;
				return true;
			}
			if (Objects.equals(previousEntry.getTimestamp(), currentEntry.getTimestamp())) {
				previousEntry = currentEntry;
				return false;
			}
			if (Objects.equals(previousEntry.getLines(), currentEntry.getLines())) {
				previousEntry = currentEntry;
				return false;
			}
			previousEntry = currentEntry;
			return true;
		}
	}
}
