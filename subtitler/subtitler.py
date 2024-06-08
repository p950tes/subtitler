#!/usr/bin/python3

from subtitle_parser import SubtitleParser
from subtitle_scrubber import SubtitleScrubber
from print_utils import verbose, fatal

import argparse
import os
import sys
import signal
import shutil

signal.signal(signal.SIGINT, lambda sig, frame : sys.exit(1))

def is_valid_file(parser, arg) -> str:
    if os.path.isfile(arg):
        return arg
    parser.error("The file %s does not exist!" % arg)

def parse_args() -> argparse.Namespace:
    argparser = argparse.ArgumentParser(prog='SubtitleUtil', description='Multi-purpose subtitle editing tool')

    argparser.add_argument('files', metavar='FILE', type=lambda x: is_valid_file(argparser, x), nargs='+',  help='Input file')
    argparser.add_argument('-v', '--verbose', action='store_true', help='Verbose mode')
    argparser.add_argument('--dry-run', '--nono', action='store_true', help='Make no changes')
    argparser.add_argument('--no-cleanup', dest='cleanup', action='store_false', help='Disables cleanup of backup file')

    args = argparser.parse_args()
    return args

def process_file(input_file_path: str) -> None:

    print("\nProcessing '" + input_file_path + "'")
    parser = SubtitleParser()
    parsed_file = parser.parse(input_file_path)

    scrubber = SubtitleScrubber()
    scrubber.scrub(parsed_file)

    for entry in parsed_file.entries:
        print(str(entry) + "\n")
    

def main() -> None:
    ARGS = parse_args()
    verbose('Arguments:\n  ' + '\n  '.join(f'{k}={v}' for k, v in vars(ARGS).items() if v != None) + "\n")

    if len(ARGS.files) > 1:
        print("Input files:")
        print('  ' + '\n  '.join(ARGS.files))

    for file in ARGS.files:
        process_file(file)
        if len(ARGS.files) > 1:
            print("---")

if __name__ == '__main__':
    main()


#def scrub_subtitle(file_path: str) -> None:
#    backup_file = file_path + ".bak"
#    shutil.copy2(file_path, backup_file)
#
#    if ARGS.cleanup:
#        os.remove(backup_file)
