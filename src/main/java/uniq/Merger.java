package uniq;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Merger {
    private final boolean uniqueOnly;
    private final LineHandler lh;
    private IOHandler io;

    public Merger(boolean regIgnored, int skip, boolean countMerged, boolean uniqueOnly) {
        this.uniqueOnly = uniqueOnly;
        lh = new LineHandler(countMerged, regIgnored, skip);
    }

    public void merge(List<String> arguments, File outFile) throws IOException{
        io = new IOHandler(arguments, outFile);
        mergeLines();
    }

    private void mergeLines() {
        String line;
        String prev = io.getLine();
        int cnt = 1;

        while ((line = io.getLine()) != null) {
            if (lh.linesEqual(prev, line)) {
                cnt ++;
                continue;
            }
            if (!uniqueOnly || cnt == 1) io.printLine(lh.withCounter(prev, cnt));
            prev = line;
            cnt = 1;
        }
        if (!uniqueOnly || cnt == 1) io.printLine(lh.withCounter(prev, cnt));

        io.closeIO();
    }

    private class LineHandler {
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

        public List<String> getLinesList(List<String> arguments) {
            return splitByLines(String.join(" ", arguments));
        }

        private List<String> splitByLines(String lines) {
            return new ArrayList<>(Arrays.asList(lines.split("\\r?\\n")));
        }
    }

    private class IOHandler {
        private final Supplier<String> getLine;
        private final Consumer<String> printLine;
        private final Runnable closeIO;

        public IOHandler(List<String> arguments, File outFile) throws IOException {
            Runnable closeReader;
            Runnable closeWriter;

            if (arguments.size() == 1 && arguments.get(0).endsWith(".txt")) {
                BufferedReader reader = new BufferedReader(new FileReader(arguments.get(0)));
                getLine = () -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        return null;
                    }
                };
                closeReader = () -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

            } else {
                Iterator<String> linesIterator = lh.getLinesList(arguments).listIterator();
                getLine = () -> {
                    try {
                        return linesIterator.next();
                    } catch (NoSuchElementException e) {
                        return null;
                    }
                };
                closeReader = () -> {};
            }

            if (outFile != null) {
                if (!outFile.exists()) {
                    throw new IOException(String.format("File %s does no exist", outFile.getName()));
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                printLine = (String line) -> {
                    try{
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                closeWriter = () -> {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            } else {
                printLine = System.out::println;
                closeWriter = () -> {};
            }

            closeIO = () -> {
                closeReader.run();
                closeWriter.run();
            };
        }

        public String getLine() {
            return getLine.get();
        }

        public void printLine(String line) {
            printLine.accept(line);
        }

        private void closeIO() {
            closeIO.run();
        }
    }
}