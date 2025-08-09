package se.p950tes.subtitler.options;

public record SubtitlerArguments(
		/**
		 * Transformation options
		 */
		TransformationOptions transformationOptions,
		
		/**
		 * Input options
		 */
		InputOptions inputOptions,
		
		/**
		 * Output options
		 */
		OutputOptions outputOptions,
		
		/**
		 * Verbose mode
		 */
		boolean verbose
) {
	
	public static SubtitlerArguments create() {
		return new SubtitlerArguments(null, null, null, false);
	}
	
	public SubtitlerArguments withTransformationOptions(TransformationOptions transformationOptions) {
		return new SubtitlerArguments(transformationOptions, inputOptions, outputOptions, verbose);
	}
	public SubtitlerArguments withInputOptions(InputOptions inputOptions) {
		return new SubtitlerArguments(transformationOptions, inputOptions, outputOptions, verbose);
	}
	public SubtitlerArguments withOutputOptions(OutputOptions outputOptions) {
		return new SubtitlerArguments(transformationOptions, inputOptions, outputOptions, verbose);
	}
	public SubtitlerArguments withVerbose(boolean verbose) {
		return new SubtitlerArguments(transformationOptions, inputOptions, outputOptions, verbose);
	}
}
