import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Merger {

    private final boolean countMerged;
    private final boolean regIgnored;
    private final int skip;
    private final boolean uniqueOnly;

    public Merger(boolean regIgnored, int skip, boolean countMerged, boolean uniqueOnly) {
        this.countMerged = countMerged;
        this.regIgnored = regIgnored;
        this.skip = skip;
        this.uniqueOnly = uniqueOnly;
    }

    private List<String> getLinesList(File inFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
            List<String> linesList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                linesList.add(line);
            }
            return linesList;
        }
    }

    private List<String> getLinesList(String lines) {
        return new ArrayList<>(Arrays.asList(lines.split("(\\\\n)")));
    }

    private List<String> merge(List<String> linesList) {
        String prevLine = "";
        List<String> result = new ArrayList<>();
        int count = 1;

        BiFunction<String, String, Boolean> linesEqual = regIgnored? String::equalsIgnoreCase: String::equals;
        BiFunction<String, Integer, String> getLine =
                (String prev, Integer cnt) ->
                        (!countMerged? "" : (cnt + (cnt==1?" merge  | ": " merges | "))) + prev;

        for (String line: linesList) {
            if (!linesEqual.apply(prevLine.substring(skip), line.substring(skip))) {
                if (!uniqueOnly || count == 1)
                    result.add(getLine.apply(prevLine, count));
                prevLine = line;
                count = 1;
                continue;
            }
            count ++;
        }

        if (!uniqueOnly || count == 1)
            result.add(getLine.apply(prevLine, count));

        result.remove(0);

        return result;
    }

    public void merge(List<String> arguments, File outFile) throws IOException{
        List<String> linesList;
        if (arguments.size() == 1 && arguments.get(0).endsWith(".txt")) {
            linesList = getLinesList(new File(arguments.get(0)));
        } else {
            linesList = getLinesList(String.join(" ", arguments));
        }
        print(merge(linesList), outFile);
    }

    private void print(List<String> result, File outFile) throws IOException {
        if (outFile == null) {
            cmdPrint(result);
        } else {
            filePrint(result, outFile);
        }

    }

    private void cmdPrint(List<String> result) {
        for (String line : result) {
            System.out.println(line);
        }
    }

    private void filePrint(List<String> result, File outFile) throws IOException{
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))){
            for (String line : result) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

}
