package se.p950tes.subtitler.cli.argprocessing;

import java.util.Map;
import java.util.Stack;

import picocli.CommandLine.IParameterPreprocessor;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

public class DisableSpaceSeparatorPreprocessor implements IParameterPreprocessor {

	public DisableSpaceSeparatorPreprocessor() {
		super();
	}

	@Override
	public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
		// we need to decide whether the next arg is the file to edit or the name of the editor to use.
		if (" ".equals(info.get("separator"))) { // parameter was not attached to option

			// act as if the user specified --open=defaultEditor
			args.push("");
		}
		return false; // picocli's internal parsing is resumed for this option
	}
}
