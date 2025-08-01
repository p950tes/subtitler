package se.p950tes.subtitler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.p950tes.subtitler.service.model.SubtitleEntry;

class EntryScrubberTest {

	private final EntryScrubber scrubber = new EntryScrubber();

	@Test
	void junk_music() {
		newTest("music junk")
			.expect("well, I hear it's fine", "if you got the time")
			.forEntry("J“ well, I hear it's fine", "if you got the time j“");
		
		newTest("music junk")
			.expect("I might be mistaken", "hm, hm, hm")
			.forEntry("J“ I might be mistaken", "hm, hm, hm j“j“");
		
		newTest("music junk")
			.expectEmpty()
			.forEntry("[J“j“j“]");
		
		newTest("music junk")
			.expectEmpty()
			.forEntry("[Men whooping]", "{\\an2}");
	}
	
	@Test
	void html() {
		newTest("html_single_line_removed")
			.expect("Hello")
			.forEntry("<p>Hello</p><br/>");
		
		newTest("html_single_line_malformed")
			.expect("<pHello")
			.forEntry("<pHello</p><br/>");
	}
	@Test
	void square_brackets() {
		newTest("square_bracket_single_line_removed")
			.expect("Hello")
			.forEntry("[HELP]Hello[help]");
		
		newTest("square_bracket_line_malformed")
			.expect("[HELPHello[HELP")
			.forEntry("[HELPHello[HELP");
		
		newTest("multi line square bracket")
			.expectEmpty()
			.forEntry("[These are", "two lines]");
	}
	@Test
	void round_brackets() {
		newTest("round_bracket_single_line_removed")
			.expect("Hello")
			.forEntry("(HELP)Hello(help)");
		
		newTest("round_bracket_line_malformed")
			.expect("(HELPHello(HELP")
			.forEntry("(HELPHello(HELP");
		
		newTest("multi line round bracket")
			.expectEmpty()
			.forEntry("(These are", "two lines)");
		
		newTest("prefixed by dash")
			.expect("Frenchie, I need", "your help, mate.")
			.forEntry("-BUTCHER: Frenchie, I need", "your help, mate.", "-(Translucent shouts)");
		
		newTest("colon suffix")
			.expect("No. No.")
			.forEntry("(chuckles):", "No. No.");
		
		newTest("Only dash remaining")
			.expectEmpty()
			.forEntry("- (chuckles)");
	}
	
	@Test
	void empty_spaces() {
		newTest("empty_spaces_removed")
			.expect("Hello")
			.forEntry(" (HELP) Hello (help) ");
	}
	
	@Test
	void all_caps() {
		newTest("all_caps_removed")
			.expectEmpty()
			.forEntry("SCREAMING");
	}
	
	@Test
	void voice_notations() {
		newTest("all caps voice").expect("Hello").forEntry("CHRIS: Hello");
		newTest("non-caps voice").expect("Hello").forEntry("Chris: Hello");
		newTest("all caps voice with number").expect("Hello").forEntry("GUARD 1: Hello");
		newTest("non-caps voice with number").expect("Hello").forEntry("Guard 1: Hello");
		
		newTest("all caps voice").expectEmpty().forEntry("CHRIS:");
		newTest("non-caps voice").expectEmpty().forEntry("Chris:");
		newTest("all caps voice with number").expectEmpty().forEntry("GUARD 1:");
		newTest("non-caps voice with number").expectEmpty().forEntry("Guard 1:");
		
		newTest("all caps voice").expectEmpty().forEntry("CHRIS' SON:");
		newTest("all caps voice").expectEmpty().forEntry("DAN'S SON:");
		newTest("non-caps voice").expectEmpty().forEntry("Chris' son:");
		newTest("non-caps voice").expectEmpty().forEntry("Dan's son:");
		
		newTest("all caps voice").expectEmpty().forEntry("RIGGS & MURTAUGH:");
		newTest("non-caps voice").expectEmpty().forEntry("Riggs & Murtaugh:");
		
		newTest("all caps voice").expectEmpty().forEntry("- CHRIS:");
		newTest("non-caps voice").expectEmpty().forEntry("- Chris:");
		newTest("all caps voice with number").expectEmpty().forEntry("- GUARD 1:");
		newTest("non-caps voice with number").expectEmpty().forEntry("- Guard 1:");
		
		newTest("all caps voice").expectEmpty().forEntry("-CHRIS:");
		newTest("non-caps voice").expectEmpty().forEntry("-Chris:");
		newTest("all caps voice with number").expectEmpty().forEntry("-GUARD 1:");
		newTest("non-caps voice with number").expectEmpty().forEntry("-Guard 1:");
	}
	
	@Test
	void lines_with_only_junk() {
		newTest("only dash").expectEmpty().forEntry("-");
		newTest("only song").expectEmpty().forEntry("♪ ♪");
		
		newTest("only dash").expect("Hello").forEntry("Hello", "-");
		newTest("only song").expect("Hello").forEntry("♪ ♪", "Hello");
	}
	
	@Test
	void lines_with_broken_japanese_artefacts() {
		
		newTest("japanese artefacts").expectEmpty().forEntry("m 1737 191 l 1737 b -191 m 4521");
		newTest("japanese artefacts2").expectEmpty().forEntry("m 2762.39 566.43 l 2464.00 566.43 2464.00 -40.23 2762.39 -40.23");
		newTest("Contains real words").expect("m 1737 hello 191").forEntry("m 1737 hello 191");
	}
	
	@Test
	void manualTest() {
		SubtitleEntry entry = new SubtitleEntry(9, "00:00:38,038 --> 00:00:40,249", List.of("-FRENCHIE: Who is he?", "-Oh, this here", "is Hughie Campbell."));
		SubtitleEntry newEntry = scrubber.scrub(entry);
		assertEquals(List.of("Who is he?", "-Oh, this here", "is Hughie Campbell."), newEntry.getLines());
	}

	private EntryTester newTest(String message) {
		return new EntryTester(scrubber, message);
	}
	
	private static class EntryTester {
		private final EntryScrubber scrubber;
		private final String message;
		private List<String> expectedLines;

		EntryTester(EntryScrubber scrubber, String message) {
			this.scrubber = scrubber;
			this.message = message;
		}
		
		EntryTester expect(String... expectedLines) {
			this.expectedLines = List.of(expectedLines);
			return this;
		}
		EntryTester expectEmpty() {
			this.expectedLines = Collections.emptyList();
			return this;
		}
		void forEntry(String... inputLines) {
			SubtitleEntry entry = new SubtitleEntry(1, "00:00:16,141 --> 00:00:18,727", List.of(inputLines));
			SubtitleEntry newEntry = scrubber.scrub(entry);
			assertEquals(expectedLines, newEntry.getLines(), message);
		}
	}
}
