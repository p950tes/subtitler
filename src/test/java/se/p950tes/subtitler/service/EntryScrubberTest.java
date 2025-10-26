package se.p950tes.subtitler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.service.model.SubtitleEntry;

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
					"if you got the time");
		
		newTest("music junk")
			.forEntry(
					"J“ I might be mistaken", 
					"hm, hm, hm j“j“")
			.expect("I might be mistaken", 
					"hm, hm, hm");
		
		newTest("music junk")
			.forEntry("[J“j“j“]")
			.expectEmpty();
		
		newTest("music junk")
			.forEntry("[Men whooping]", "{\\an2}")
			.expectEmpty();
	}
	
	@Test
	void html() {
		newTest("html_single_line_removed")
			.forEntry("<p>Hello</p><br/>")
			.expect("Hello");
		
		newTest("html_single_line_malformed")
			.forEntry("<pHello</p><br/>")
			.expect("<pHello");
	}
	@Test
	void square_brackets() {
		newTest("square_bracket_single_line_removed")
			.forEntry("[HELP]Hello[help]")
			.expect("Hello");
		
		newTest("square_bracket_line_malformed")
			.forEntry("[HELPHello[HELP")
			.expect("[HELPHello[HELP");
		
		newTest("multi line square bracket")
			.forEntry("[These are", "two lines]")
			.expectEmpty();
	}
	@Test
	void round_brackets() {
		newTest("round_bracket_single_line_removed")
			.forEntry("(HELP)Hello(help)")
			.expect("Hello");
		
		newTest("round_bracket_line_malformed")
			.forEntry("(HELPHello(HELP")
			.expect("(HELPHello(HELP");
		
		newTest("multi line round bracket")
			.forEntry("(These are", "two lines)")
			.expectEmpty();
		
		newTest("prefixed by dash")
			.forEntry(
					"-BUTCHER: Frenchie, I need", 
					"your help, mate.", 
					"-(Translucent shouts)")
			.expect("Frenchie, I need", 
					"your help, mate.");
		
		newTest("colon suffix")
			.forEntry("(chuckles):", "No. No.")
			.expect("No. No.");
		
		newTest("Only dash remaining")
			.forEntry("- (chuckles)")
			.expectEmpty();
	}
	
	@Test
	void empty_spaces() {
		newTest("empty_spaces_removed")
			.forEntry(" (HELP) Hello (help) ")
			.expect("Hello");
	}
	
	@Test
	void all_caps() {
		newTest("all_caps_removed")
			.forEntry("SCREAMING")
			.expectEmpty();
		
		newTest("all caps should be removed even if multi-line")
			.forEntry(
					"Hello, Sanjeev.",
					"CAVEMAN-LIKE GRUNTING")
			.expect("Hello, Sanjeev.");
	}
	
	@Test
	void voice_notations() {
		newTest("all caps voice").forEntry("CHRIS: Hello").expect("Hello");
		newTest("non-caps voice").forEntry("Chris: Hello").expect("Hello");
		newTest("all caps voice with number").forEntry("GUARD 1: Hello").expect("Hello");
		newTest("non-caps voice with number").forEntry("Guard 1: Hello").expect("Hello");
		
		newTest("all caps voice").forEntry("CHRIS:").expectEmpty();
		newTest("non-caps voice").forEntry("Chris:").expectEmpty();
		newTest("all caps voice with number").forEntry("GUARD 1:").expectEmpty();
		newTest("non-caps voice with number").forEntry("Guard 1:").expectEmpty();
		
		newTest("all caps voice").forEntry("CHRIS' SON:").expectEmpty();
		newTest("all caps voice").forEntry("DAN'S SON:").expectEmpty();
		newTest("non-caps voice").forEntry("Chris' son:").expectEmpty();
		newTest("non-caps voice").forEntry("Dan's son:").expectEmpty();
		
		newTest("all caps voice").forEntry("RIGGS & MURTAUGH:").expectEmpty();
		newTest("non-caps voice").forEntry("Riggs & Murtaugh:").expectEmpty();
		
		newTest("all caps voice").forEntry("- CHRIS:").expectEmpty();
		newTest("non-caps voice").forEntry("- Chris:").expectEmpty();
		newTest("all caps voice with number").forEntry("- GUARD 1:").expectEmpty();
		newTest("non-caps voice with number").forEntry("- Guard 1:").expectEmpty();
		
		newTest("all caps voice").forEntry("-CHRIS:").expectEmpty();
		newTest("non-caps voice").forEntry("-Chris:").expectEmpty();
		newTest("all caps voice with number").forEntry("-GUARD 1:").expectEmpty();
		newTest("non-caps voice with number").forEntry("-Guard 1:").expectEmpty();
		
		newTest("all caps voice in the middle of a line")
			.forEntry("of another horn. MAISIE: Oh.")
			.expect("of another horn. Oh.");
		
		newTest("multi-line all-caps voice")
			.forEntry(
					"HIGH-PITCHED: ..little Alex Horne!", 
					"Hi, thank you.")
			.expect(
				"..little Alex Horne!", 
				"Hi, thank you.");
	}
	
	@Test
	void lines_with_only_junk() {
		newTest("only dash").forEntry("-").expectEmpty();
		newTest("only song").forEntry("♪ ♪").expectEmpty();
		
		newTest("only dash").forEntry("Hello", "-").expect("Hello");
		newTest("only song").forEntry("♪ ♪", "Hello").expect("Hello");
	}
	
	@Test
	void lines_with_broken_japanese_artefacts() {
		
		newTest("japanese artefacts")
			.forEntry("m 1737 191 l 1737 b -191 m 4521")
			.expectEmpty();
		
		newTest("japanese artefacts2")
			.forEntry("m 2762.39 566.43 l 2464.00 566.43 2464.00 -40.23 2762.39 -40.23")
			.expectEmpty();
		
		newTest("Contains real words")
			.forEntry("m 1737 hello 191")
			.expect("m 1737 hello 191");
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
		private List<String> inputLines;
		private List<String> expectedLines;

		EntryTester(EntryScrubber scrubber, String message) {
			this.scrubber = scrubber;
			this.message = message;
		}
		
		EntryTester forEntry(String... inputLines) {
			this.inputLines = List.of(inputLines);
			return this;
		}
		void expect(String... expectedLines) {
			this.expectedLines = List.of(expectedLines);
			execute();
		}
		void expectEmpty() {
			this.expectedLines = Collections.emptyList();
			execute();
		}
		void execute() {
			SubtitleEntry entry = new SubtitleEntry(1, "00:00:16,141 --> 00:00:18,727", this.inputLines);
			SubtitleEntry newEntry = scrubber.scrub(entry);
			assertEquals(expectedLines, newEntry.getLines(), message);
		}
	}
}
