
class SubtitleEntry:
    index: int
    timestamp: str
    lines: list[str]

    def __init__(self, index: int, timestamp: str, lines: list[str]) -> None:
        self.index = index
        self.timestamp = timestamp
        self.lines = lines
    
    def is_empty(self) -> bool:
        return len(self.lines) == 0

    def __str__(self) -> str:
        ret = "Index: " + str(self.index) + "\n"
        ret += "Timestamp: " + self.timestamp
        for i, line in enumerate(self.lines):
            ret += "\n[" + str(i) + "]: " + line
        return ret

class SubtitleFile:
    file_path: str
    entries: list[SubtitleEntry]
    
    def __init__(self, file_path: str, entries: list[SubtitleEntry]) -> None:
        self.file_path = file_path
        self.entries = entries
