package se.p950tes.subtitler.file;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileManager {

	public boolean fileExists(Path file) {
		return Files.exists(file);
	}
	
	public boolean isReadableFile(Path file) {
		return Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file);
	}
	
	public boolean isWritableFile(Path file) {
		return Files.exists(file) && Files.isRegularFile(file) && Files.isWritable(file);
	}
	
	public List<String> readLinesFromFile(Path file) {
		try {
			return Files.readAllLines(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read file: " + file, e);
		}
	}
	
	public PrintStream openOutputStream(Path file) throws IOException {
		OutputStream fileOutputStream = Files.newOutputStream(file);
		return new PrintStream(fileOutputStream);
	}
	
	public void copy(Path source, Path destination) {
		try {
			Files.copy(source, destination);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to backup original file", e);
		}
	}
	
	public Path resolveBackupFile(Path inputFile, String backupFileSuffix) {
		String originalFileName = inputFile.getFileName().toString();
		String backupFileName = originalFileName + backupFileSuffix;
		Path directory = inputFile.toAbsolutePath().getParent();
		
		File backupFile = new File(directory.toFile(), backupFileName);
		return backupFile.toPath();
	}
	
    public boolean isStdinAvailable() {
        try {
            if (System.console() != null) {
                return false; // interactive terminal --> no piped input
            }
            // If something is already buffered in stdin
            return System.in.available() > 0;
            
        } catch (IOException e) {
            return false;
        }
    }
}
