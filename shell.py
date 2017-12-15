import argparse
import submission
import sys
import wordsegUtil

# REPL and main entry point
def repl(command=None):

    if command is None:
        cmdAndLine = line.split(None, 1)
        cmd, line = cmdAndLine[0], ' '.join(cmdAndLine[1:])
    else:
        cmd = command
        line = line

    line = wordsegUtil.cleanLine(line)
    parts = wordsegUtil.words(line)
    print '  ' + ' '.join(
        submission.segmentWords(part, unigramCost) for part in parts)



