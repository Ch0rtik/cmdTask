package uniq;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

public class UniqLauncher {
    @Option(name = "-i", usage = "Ignore the register")
    private boolean regIgnored;

    @Option(name = "-s", metaVar = "num", usage = "Skip the first N symbols of each line")
    private int skip = 0;

    @Option(name = "-u", usage = "Print only unique lines")
    private boolean uniqueOnly;

    @Option(name = "-c", usage = "Count merged strings", forbids = {"-u"})
    private boolean countMerged;

    @Option(name = "-o", metaVar = "ofile", usage = "Output file name")
    private File outFile;

    @Argument(metaVar = "arguments", usage = "Input file name")
    private File inFile;

    public static void main(String[] args) throws IOException {
        UniqLauncher launcher = new UniqLauncher();
        launcher.readCmdArguments(args);
        launcher.launchMerger();
    }

    private void readCmdArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }
        if (skip < 0) {
            throw new IllegalArgumentException("Skip can't be negative");
        }
    }

    private void launchMerger() throws IOException {
        Merger merger = new Merger(regIgnored, skip, countMerged, uniqueOnly);

        try {
            merger.merge(inFile, outFile);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
