package se.p950tes.subtitler.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
	
	public BufferedReader getFileReader(Path file) {
		try {
			return Files.newBufferedReader(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed create file reader for file: " + file, e);
		}
	}
	
	public PrintStream openOutputStream(Path file) throws IOException {
		OutputStream fileOutputStream = Files.newOutputStream(file);
		return new PrintStream(fileOutputStream);
	}
	public PrintStream getSystemOutStream() {
		return System.out;
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
