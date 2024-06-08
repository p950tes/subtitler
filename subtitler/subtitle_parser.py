
from typing import Final
from enum import Enum
from subtitle_entities import SubtitleFile, SubtitleEntry
from print_utils import verbose
import re

class SubtitleParser:

    INDEX_PATTERN:     Final[re.Pattern] = re.compile(r"^\d+$")
    TIMESTAMP_PATTERN: Final[re.Pattern] = re.compile(r"^[\d:,]+\s+\-\-\>\s+[\d:,]+$")

    class Type(Enum):
        INDEX, TIMESTAMP, CONTENT = range(3)

    entries: list[SubtitleEntry] = []

    cur_index = ""
    cur_timestamp = ""
    cur_content = []
    last_parsed: Type

    def parse(self, file_path: str) -> SubtitleFile:
        
        with open(file_path) as filehandle:
            lines = filehandle.readlines()
        
        num_lines = len(lines)
        verbose("Number of lines: " + str(num_lines))

        for i, line in enumerate(lines):
            line = line.strip()
            if not line:
                continue
            
            if i < num_lines-1 and self.looks_like_index(line) and self.looks_like_timestamp(lines[i+1]):
                self.reset()
                self.cur_index = self.INDEX_PATTERN.match(line).group()
                self.last_parsed = self.Type.INDEX
                continue

            if self.cur_index and self.looks_like_timestamp(line):
                self.cur_timestamp = self.TIMESTAMP_PATTERN.match(line).group()
                self.last_parsed = self.Type.TIMESTAMP
                continue

            if self.last_parsed in [self.Type.TIMESTAMP, self.Type.CONTENT]:
                self.cur_content.append(line)
                self.last_parsed = self.Type.CONTENT
                continue
        
        self.reset()
        
        return SubtitleFile(file_path, self.entries)


    def reset(self) -> None:
        if self.cur_index and self.cur_timestamp:
            entry = SubtitleEntry(self.cur_index, self.cur_timestamp, self.cur_content)
            verbose("\nParsed entry: \n" + str(entry))
            self.entries.append(entry)

        self.last_parsed = None
        self.cur_index = ""
        self.cur_timestamp = ""
        self.cur_content = []
    
    def looks_like_index(self, line: str) -> bool:
        line = line.strip()
        return self.__matches(self.INDEX_PATTERN, line)
    
    def looks_like_timestamp(self, line: str) -> bool:
        line = line.strip()
        return self.__matches(self.TIMESTAMP_PATTERN, line)

    @staticmethod
    def __matches(pattern: re.Pattern, line: str) -> bool:
        if pattern.match(line):
            return True
        else:
            return False
