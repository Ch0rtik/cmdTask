package uniq;

import java.io.IOException;

public class UniqLauncher {

    public static void main(String[] args) {
        new UniqLauncher().readCmdArguments(args);
    }

    private void readCmdArguments(String[] args){
        CommandLineArguments values = new CommandLineArguments(args);

        Merger merger = new Merger(values.regIgnored, values.skip, values.countMerged, values.uniqueOnly);

        try {
            merger.merge(values.arguments, values.outFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
