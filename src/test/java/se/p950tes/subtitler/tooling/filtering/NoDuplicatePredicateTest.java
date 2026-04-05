package se.p950tes.subtitler.tooling.filtering;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import se.p950tes.subtitler.model.SubtitleEntry;

class NoDuplicatePredicateTest {

	private static final SubtitleEntry NULL = null;
	
	@Test
	void nulls() {
		var result = listOf(NULL).stream()
				.filter(new NoDuplicatePredicate())
				.toList();
		assertEquals(listOf(NULL), result, "Single null should be left alone");
		
		result = listOf(NULL, NULL).stream()
				.filter(new NoDuplicatePredicate())
				.toList();
		assertEquals(listOf(NULL), result, "Consecutive nulls are duplictes");
		
		result = listOf(NULL, new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))).stream()
				.filter(new NoDuplicatePredicate())
				.toList();
		assertEquals(listOf(NULL, new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))), result, "Single null should be left alone");
		
		result = listOf(new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")), NULL).stream()
				.filter(new NoDuplicatePredicate())
				.toList();
		assertEquals(listOf(new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")), NULL), result, "Single null should be left alone");
	}
	
	@Test
	void single() {
		var result = Stream.of(new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")))
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))), result);
	}

	@Test
	void two_identical() {
		var result = Stream.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))
			)
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))), result);
	}
	
	@Test
	void two_distinct() {
		var result = Stream.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(2, "00:00:12,100 --> 00:00:01,000", List.of("Bye"))
			)
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(2, "00:00:12,100 --> 00:00:01,000", List.of("Bye"))), result);
	}
	
	@Test
	void same_text_different_timestamp() {
		var result = Stream.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(2, "00:00:10,000 --> 00:00:10,000", List.of("Hello"))
			)
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(2, "00:00:10,000 --> 00:00:10,000", List.of("Hello"))), result);
	}
	@Test
	void same_timestamp_different_text() {
		var result = Stream.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Bye"))
				)
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Bye"))), result);
	}
	
	@Test
	void indexes_should_not_matter() {
		var result = Stream.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello")),
				new SubtitleEntry(2, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))
			)
				.filter(new NoDuplicatePredicate())
				.toList();
		
		assertEquals(List.of(
				new SubtitleEntry(1, "00:00:00,000 --> 00:00:01,000", List.of("Hello"))), result);
	}
	
	@SafeVarargs
	private static List<SubtitleEntry> listOf(SubtitleEntry... elements) {
		return Arrays.asList(elements);
	}
}
