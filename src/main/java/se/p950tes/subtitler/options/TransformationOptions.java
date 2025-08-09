package se.p950tes.subtitler.options;

import java.time.Duration;
import java.util.Optional;

public record TransformationOptions(
		/**
		 * Scrub mode, cleaning files of unwanted content
		 */
		boolean scrubMode,
		
		/**
		 * Merge mode, merge multiple input files into a single output
		 */
		boolean mergeMode,
		
		/**
		 * Time shift mode, move the subtitles forwards or backwards in time
		 */
		boolean timeShiftMode,
		
		/**
		 * The amount to move the subtitles
		 */
		Optional<Duration> timeShift
) {
	public static TransformationOptions create() {
		return new TransformationOptions(false, false, false, Optional.empty());
	}
	
	public TransformationOptions withScrubMode(boolean scrubMode) {
		return new TransformationOptions(scrubMode, mergeMode, timeShiftMode, timeShift);
	}
	public TransformationOptions withMergeMode(boolean mergeMode) {
		return new TransformationOptions(scrubMode, mergeMode, timeShiftMode, timeShift);
	}
	public TransformationOptions withTimeShiftMode(Duration timeShift) {
		return new TransformationOptions(scrubMode, mergeMode, timeShift != null, Optional.ofNullable(timeShift));
	}
}
