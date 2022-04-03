import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergerLauncher {
    @Option(name = "-i", usage = "Ignore the register")
    private boolean regIgnored;

    @Option(name = "-s", usage = "Skip the first N symbols of each line")
    private int skip = 0;

    @Option(name = "-u", usage = "Print only unique lines")
    private boolean uniqueOnly;

    @Option(name = "-c", usage = "Count merged strings")
    private boolean countMerged;

    @Option(name = "-o", metaVar = "ofile", usage = "Output file name")
    private File outFile;

    @Argument(usage = "Either a name of a file OR a text")
    private List<String> arguments = new ArrayList<>();


    public static void main(String[] args) {
        new MergerLauncher().read(args);

    }

    private void read(String[] args){
        final CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        Merger merger = new Merger(regIgnored, skip, countMerged, uniqueOnly);

        try {
            merger.merge(arguments, outFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
