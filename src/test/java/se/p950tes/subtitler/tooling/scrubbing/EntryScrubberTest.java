package se.p950tes.subtitler.tooling.scrubbing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.model.SubtitleEntry;

class EntryScrubberTest {

	private final EntryScrubber scrubber = new EntryScrubber(new Logger());

	@Test
	void music_junk() {
		newTest("music junk")
			.forEntry(
					"J“ well, I hear it's fine", 
					"if you got the time j“")
			.expect(
					"well, I hear it's fine", 
					"if you got the time")
			.expectModified(true)
			.run();
		
		newTest("music junk")
			.forEntry(
					"J“ I might be mistaken", 
					"hm, hm, hm j“j“")
			.expect("I might be mistaken", 
					"hm, hm, hm")
			.expectModified(true)
			.run();
		
		newTest("music junk")
			.forEntry("[J“j“j“]")
			.expectEmpty()
			.expectModified(true)
			.run();
		
		newTest("music junk")
			.forEntry("[Men whooping]", "{\\an2}")
			.expectEmpty()
			.expectModified(true)
			.run();
	}
	
	@Test
	void html() {
		newTest("html_single_line_removed")
			.forEntry("<p>Hello</p><br/>")
			.expect("Hello")
			.expectModified(true)
			.run();
		
		newTest("html_single_line_malformed")
			.forEntry("<pHello</p><br/>")
			.expect("<pHello")
			.expectModified(true)
			.run();
	}
	@Test
	void square_brackets() {
		newTest("square_bracket_single_line_removed")
			.forEntry("[HELP]Hello[help]")
			.expect("Hello")
			.expectModified(true)
			.run();
		
		newTest("square_bracket_line_malformed")
			.forEntry("[HELPHello[HELP")
			.expectModified(false)
			.run();
		
		newTest("multi line square bracket")
			.forEntry("[These are", "two lines]")
			.expectEmpty()
			.expectModified(true)
			.run();
	}
	@Test
	void round_brackets() {
		newTest("round_bracket_single_line_removed")
			.forEntry("(HELP)Hello(help)")
			.expect("Hello")
			.expectModified(true)
			.run();
		
		newTest("round_bracket_line_malformed")
			.forEntry("(HELPHello(HELP")
			.expectModified(false)
			.run();
		
		newTest("multi line round bracket")
			.forEntry("(These are", "two lines)")
			.expectEmpty()
			.expectModified(true)
			.run();
		
		newTest("prefixed by dash")
			.forEntry(
					"-BUTCHER: Frenchie, I need", 
					"your help, mate.", 
					"-(Translucent shouts)")
			.expect("Frenchie, I need", 
					"your help, mate.")
			.expectModified(true)
			.run();
		
		newTest("colon suffix")
			.forEntry("(chuckles):", "No. No.")
			.expect("No. No.")
			.expectModified(true)
			.run();
		
		newTest("Only dash remaining")
			.forEntry("- (chuckles)")
			.expectEmpty()
			.expectModified(true)
			.run();
	}
	
	@Test
	void empty_spaces() {
		newTest("empty_spaces_removed")
			.forEntry(" (HELP) Hello (help) ")
			.expect("Hello")
			.expectModified(true)
			.run();
	}
	
	@Test
	void all_caps() {
		newTest("all_caps_removed")
			.forEntry("SCREAMING")
			.expectEmpty()
			.expectModified(true)
			.run();
		
		newTest("all caps should be removed even if multi-line")
			.forEntry(
					"Hello, Sanjeev.",
					"CAVEMAN-LIKE GRUNTING")
			.expect("Hello, Sanjeev.")
			.expectModified(true)
			.run();
	}
	
	@Test
	void voice_notations() {
		newTest("all caps voice").forEntry("CHRIS: Hello").expect("Hello").expectModified(true).run();
		newTest("all caps voice with number").forEntry("GUARD 1: Hello").expect("Hello").expectModified(true).run();
		
		newTest("all caps voice").forEntry("CHRIS:").expectEmpty().expectModified(true).run();
		newTest("all caps voice with number").forEntry("GUARD 1:").expectEmpty().expectModified(true).run();
		
		newTest("all caps voice").forEntry("CHRIS' SON:").expectEmpty().expectModified(true).run();
		newTest("all caps voice").forEntry("DAN'S SON:").expectEmpty().expectModified(true).run();
		
		newTest("all caps voice").forEntry("RIGGS & MURTAUGH:").expectEmpty().expectModified(true).run();
		
		newTest("all caps voice").forEntry("- CHRIS:").expectEmpty().expectModified(true).run();
		newTest("all caps voice with number").forEntry("- GUARD 1:").expectEmpty().expectModified(true).run();
		
		newTest("all caps voice").forEntry("-CHRIS:").expectEmpty().expectModified(true).run();
		newTest("all caps voice with number").forEntry("-GUARD 1:").expectEmpty().expectModified(true).run();
		
		newTest("all caps voice in the middle of a line")
			.forEntry("of another horn. MAISIE: Oh.")
			.expect("of another horn. Oh.")
			.expectModified(true)
			.run();
		
		newTest("multi-line all-caps voice")
			.forEntry(
					"HIGH-PITCHED: ..little Alex Horne!", 
					"Hi, thank you.")
			.expect(
				"..little Alex Horne!", 
				"Hi, thank you.")
			.expectModified(true)
			.run();
		
		newTest("non-voice line ending with colon")
			.forEntry("He said something like:")
			.expect("He said something like:")
			.expectModified(false)
			.run();
	}
	
	@Test
	void lines_with_only_junk() {
		newTest("only dash").forEntry("-").expectEmpty().expectModified(true).run();
		newTest("only song").forEntry("♪ ♪").expectEmpty().expectModified(true).run();
		
		newTest("only dash").forEntry("Hello", "-").expect("Hello").expectModified(true).run();
		newTest("only song").forEntry("♪ ♪", "Hello").expect("Hello").expectModified(true).run();
	}
	
	@Test
	void lines_with_broken_japanese_artefacts() {
		
		newTest("japanese artefacts")
			.forEntry("m 1737 191 l 1737 b -191 m 4521")
			.expectEmpty()
			.expectModified(true)
			.run();
		
		newTest("japanese artefacts2")
			.forEntry("m 2762.39 566.43 l 2464.00 566.43 2464.00 -40.23 2762.39 -40.23")
			.expectEmpty()
			.expectModified(true)
			.run();
		
		newTest("Contains real words")
			.forEntry("m 1737 hello 191")
			.expectModified(false)
			.run();
	}
	
	@Test
	void manualTest() {
		SubtitleEntry entry = new SubtitleEntry(9, "00:00:38,038 --> 00:00:40,249", List.of("-FRENCHIE: Who is he?", "-Oh, this here", "is Hughie Campbell."));
		SubtitleEntry newEntry = scrubber.scrub(entry);
		assertEquals(List.of("Who is he?", "-Oh, this here", "is Hughie Campbell."), newEntry.getLines());
		assertTrue(newEntry.isModified(), "Expected entry to be modified");
	}
	
	private EntryTester newTest(String message) {
		return new EntryTester(scrubber, message);
	}
	
	private static class EntryTester {
		private final EntryScrubber scrubber;
		private final String message;
		private List<String> inputLines;
		private List<String> expectedLines;
		private Boolean expectedModified;

		EntryTester(EntryScrubber scrubber, String message) {
			this.scrubber = scrubber;
			this.message = message;
		}
		
		EntryTester forEntry(String... inputLines) {
			this.inputLines = List.of(inputLines);
			return this;
		}
		EntryTester expectModified(boolean expectedModified) {
			this.expectedModified = expectedModified;
			return this;
		}
		EntryTester expect(String... expectedLines) {
			this.expectedLines = List.of(expectedLines);
			return this;
		}
		EntryTester expectEmpty() {
			this.expectedLines = Collections.emptyList();
			return this;
		}
		void run() {
			SubtitleEntry entry = new SubtitleEntry(1, "00:00:16,141 --> 00:00:18,727", this.inputLines);
			SubtitleEntry newEntry = scrubber.scrub(entry);
			if (expectedLines != null) {
				assertEquals(expectedLines, newEntry.getLines(), message);
			}
			if (expectedModified != null) {
				assertEquals(expectedModified, newEntry.isModified(), "Expected entry to be modified");
			}
		}
	}
}
