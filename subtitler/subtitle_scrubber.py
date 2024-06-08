
from subtitle_entities import SubtitleFile, SubtitleEntry
import re

class SubtitleScrubber:

    # (SCREAM)
    ROUND_BRACKET_PATTERN = re.compile(r'\([^)]+\)')
    # [SCREAM]
    SQUARE_BRACKET_PATTERN = re.compile(r'\[[^]]+\]')
    # {SCREAM}
    CURLY_BRACKET_PATTERN = re.compile(r'\{[^}]+\}')    
    # <SCREAM>
    HTML_TAG_PATTERN = re.compile(r'<[^>]+>')
    
    # music junk artifacts (J“ / j“)
    MUSIC_JUNK_PATTERN = re.compile(r'(^([jJ]“)+|([jJ]“)+$)')

    # Names followed by colon: (Guard 1: Hello there)
    VOICE_INDICATORS_PATTERN = re.compile(r'^[A-Za-z]{2}[A-Za-z0-9 ]*\s?: ')

    # All caps, at least 3 characters
    ALL_CAPS_PATTERN = re.compile(r'^[A-Z ,\!]{3,}$')


    def scrub(self, subtitle_file: SubtitleFile):

        entries = subtitle_file.entries

        for entry in entries:
            self.scrub_entry(entry)

        entries = self.__filter_empty_entries(entries)
        self.__correct_indexes(entries)
        subtitle_file.entries = entries
    
    def scrub_entry(self, entry: SubtitleEntry):

        lines = entry.lines
        lines = [self.scrub_line(line) for line in lines]

        # Remove empty lines
        #lines = [line for line in lines if line]
        lines = [line.strip() for line in lines if line and line.strip()]
        entry.lines = lines

    def scrub_line(self, line: str) -> str:

        line = self.__apply_regex(self.HTML_TAG_PATTERN, line)
        line = self.__apply_regex(self.ROUND_BRACKET_PATTERN, line)
        line = self.__apply_regex(self.SQUARE_BRACKET_PATTERN, line)
        line = self.__apply_regex(self.CURLY_BRACKET_PATTERN, line)
        line = self.__apply_regex(self.MUSIC_JUNK_PATTERN, line)
        line = self.__apply_regex(self.VOICE_INDICATORS_PATTERN, line)
        line = self.__apply_regex(self.VOICE_INDICATORS_PATTERN, line)

        return line

    @staticmethod
    def __apply_regex(pattern: re.Pattern, line: str) -> str:
        line = pattern.sub('', line)
        line = line.strip()
        return line

    def __filter_empty_entries(self, entries: list[SubtitleEntry]) -> list[SubtitleEntry]:
        return [entry for entry in entries if not entry.is_empty()]

    def __correct_indexes(self, entries: list[SubtitleEntry]):
        for i, entry in enumerate(entries):
            entry.index = str(i+1)
