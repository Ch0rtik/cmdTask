package uniq;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandLineArguments {
    @Option(name = "-i", usage = "Ignore the register")
    public boolean regIgnored;

    @Option(name = "-s", metaVar = "num", usage = "Skip the first N symbols of each line")
    public int skip = 0;

    @Option(name = "-u", usage = "Print only unique lines")
    public boolean uniqueOnly;

    @Option(name = "-c", usage = "Count merged strings")
    public boolean countMerged;

    @Option(name = "-o", metaVar = "ofile", usage = "Output file name")
    public File outFile;

    @Argument(metaVar = "arguments", usage = "Either a name of a file OR a text")
    public List<String> arguments = new ArrayList<>();

    public CommandLineArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }

    }
}