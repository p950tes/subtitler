package se.p950tes.subtitler.model;

import java.util.List;
import java.util.Objects;

public class SubtitleEntry {

	private Integer index;
	private String timestamp;
	private List<String> lines;
	private boolean modified = false;

	public SubtitleEntry(Integer index, String timestamp, List<String> lines) {
		this.index = index;
		this.timestamp = timestamp;
		this.lines = lines;
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public List<String> getLines() {
		return lines;
	}
	public void setLines(List<String> lines) {
		this.lines = lines;
	}
	public boolean isModified() {
		return modified;
	}
	public void setModified(boolean modified) {
		this.modified = modified;
	}

	@Override
	public String toString() {
		String ret = "Index: " + index + "\n";
		ret += "Timestamp: " + timestamp;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			ret += "\n[" + i + "]: " + line;
		}
		return ret;
	}

	public String toFormattedEntry() {
		StringBuilder builder = new StringBuilder();
		builder.append(index).append("\n");
		builder.append(timestamp).append("\n");
		builder.append(String.join("\n", lines));
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(lines, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SubtitleEntry other = (SubtitleEntry) obj;
		return Objects.equals(lines, other.lines) && Objects.equals(timestamp, other.timestamp);
	}
}
