import sys

def print_error(*args, **kwargs) -> None:
    print("ERROR: ", file=sys.stderr, end='')
    print(*args, file=sys.stderr, **kwargs)

def fatal(*args, **kwargs) -> None:
    print_error(*args, **kwargs)
    exit(1)

def verbose(*args, **kwargs) -> None:
    #if ARGS.verbose:
    if False:
        print(*args, file=sys.stderr, **kwargs)
