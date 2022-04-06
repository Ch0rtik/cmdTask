package uniq;

import java.io.IOException;

public class MergerLauncher {

    public static void main(String[] args) {
        new MergerLauncher().readCmdArguments(args);
    }

    private void readCmdArguments(String[] args){
        CommandLineArgument values = new CommandLineArgument(args);

        Merger merger = new Merger(values.regIgnored, values.skip, values.countMerged, values.uniqueOnly);

        try {
            merger.merge(values.arguments, values.outFile);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
