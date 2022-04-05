package uniq;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Merger {
    private final boolean uniqueOnly;
    private final LineHandler lineHandler;

    public Merger(boolean regIgnored, int skip, boolean countMerged, boolean uniqueOnly) {
        this.uniqueOnly = uniqueOnly;
        lineHandler = new LineHandler(countMerged, regIgnored, skip);
    }

    private static class LineHandler {

        private final BiFunction<String, String, Boolean> linesEqual;
        private final BiFunction<String, Integer, String> withCounter;
        private final Function<String, String> withSkip;

        public LineHandler(boolean countMerged, boolean regIgnored, int skip) {
            linesEqual = regIgnored? String::equalsIgnoreCase: String::equals;
            withCounter = countMerged?
                    (String prev, Integer cnt) -> (cnt + (cnt==1?" merge  | ": " merges | ")) + prev:
                    (String prev, Integer cnt) -> prev;
            withSkip = skip==0?
                    (String line) -> line:
                    (String line) -> skip < line.length()? line.substring(skip): "";
        }

        public boolean linesEqual(String prev, String line) {
            return linesEqual.apply(withSkip(prev), withSkip(line));
        }

        public String withCounter(String prev, Integer cnt) {
            return withCounter.apply(prev, cnt);
        }

        private String withSkip(String line) {
            return withSkip.apply(line);
        }
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
        return new ArrayList<>(Arrays.asList(lines.split("\\\\r?\\\\n")));
    }

    private List<String> merge(List<String> linesList) {
        String prev = "";
        List<String> result = new ArrayList<>();
        int cnt = 1;

        for (String line: linesList) {
            if (lineHandler.linesEqual(prev, line)) {
                cnt ++;
                continue;
            }
            if (!uniqueOnly || cnt == 1) result.add(lineHandler.withCounter(prev, cnt));
            prev = line;
            cnt = 1;
        }

        if (!uniqueOnly || cnt == 1) result.add(lineHandler.withCounter(prev, cnt));

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
