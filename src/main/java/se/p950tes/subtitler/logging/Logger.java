package se.p950tes.subtitler.logging;

import java.io.PrintStream;
import java.text.MessageFormat;

public class Logger {

	private final PrintStream out;
	private final PrintStream err;
	private boolean verbose = false;
	
	public Logger() {
		this.out = System.out;
		this.err = System.err;
	}
	public Logger(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void print(String msg, Object... params) {
		out.println(MessageFormat.format(msg, params));
	}
	public void error(String msg, Object... params) {
		err.println(MessageFormat.format(msg, params));
	}
	public void verbose(String msg, Object... params) {
		if (verbose) {
			print(msg, params);
		}
	}
}
