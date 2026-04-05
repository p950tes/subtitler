package se.p950tes.subtitler.tooling.filtering;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.ObjectUtils;

import se.p950tes.subtitler.model.SubtitleEntry;

public class NoDuplicatePredicate implements Predicate<SubtitleEntry> {

	private boolean used = false;
	private SubtitleEntry previousEntry;
	
	@Override
	public boolean test(SubtitleEntry currentEntry) {
		try {
			if (! used) {
				return true;
			}
			boolean duplicates = areEqual(previousEntry, currentEntry);
			return ! duplicates;
		
		} finally {
			used = true;
			previousEntry = currentEntry;
		}
	}

	private static boolean areEqual(SubtitleEntry first, SubtitleEntry second) {
		if (ObjectUtils.allNull(first, second)) {
			return true;
		}
		if (ObjectUtils.anyNull(first, second)) {
			return false;
		}
		if (Objects.equals(first.getTimestamp(), second.getTimestamp()) && Objects.equals(first.getLines(), second.getLines())) {
			return true;
		}
		return false;
	}
}
