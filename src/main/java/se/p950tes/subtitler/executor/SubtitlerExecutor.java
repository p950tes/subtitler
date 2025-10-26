package se.p950tes.subtitler.executor;

import java.nio.file.Path;

import se.p950tes.subtitler.file.FileManager;
import se.p950tes.subtitler.logging.Logger;
import se.p950tes.subtitler.options.SubtitlerArguments;
import se.p950tes.subtitler.service.SubtitleMergerService;
import se.p950tes.subtitler.service.SubtitleScrubberService;

public class SubtitlerExecutor {

	private final FileManager fileManager;
	private final Logger logger;

	private SubtitlerArguments args;
	
	public SubtitlerExecutor(FileManager fileManager, Logger logger) {
		this.fileManager = fileManager;
		this.logger = logger;
	}

	public void execute(SubtitlerArguments arguments) {
		this.args = arguments; 
		this.logger.setVerbose(arguments.verbose());
		
		if (args.transformationOptions().timeShiftMode()) {
			throw new UnsupportedOperationException("Time shift not yet implemented");
		}
		if (args.transformationOptions().scrubMode()) {
			executeScrubOperation();
		}
		if (args.transformationOptions().mergeMode()) {
			executeMergeOperation();
		}
	}

	private void executeScrubOperation() {
		SubtitleScrubberService scrubber = new SubtitleScrubberService(fileManager, logger, args.outputOptions().inPlaceEdit(), args.outputOptions().backupFileSuffix());
		for (Path file : args.inputOptions().inputFiles()) {
			scrubber.processFile(file);
		}
	}
	private void executeMergeOperation() {
		SubtitleMergerService service = new SubtitleMergerService(fileManager, logger);
		service.merge(args.inputOptions().inputFiles(), args.outputOptions().outputFile());
	}
}
