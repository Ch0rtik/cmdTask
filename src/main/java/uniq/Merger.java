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

    public Merger(boolean regIgnored, int skip, boolean countMerged, boolean uniqueOnly) {
        this.uniqueOnly = uniqueOnly;
        lh = new LineHandler(countMerged, regIgnored, skip);
    }

    public void merge(List<String> arguments, File outFile) throws IOException{
        mergeLines(new IOHandler(arguments, outFile));
    }

    private void mergeLines(IOHandler io) {
        String line;
        String prev = io.getLine();
        int cnt = 1;

        while ((line = io.getLine()) != null) {
            if (lh.linesEqual(prev, line)) {
                cnt ++;
                continue;
            }
            if (!uniqueOnly || cnt == 1) io.printLine(lh.addCounter(prev, cnt));
            prev = line;
            cnt = 1;
        }
        if (!uniqueOnly || cnt == 1) io.printLine(lh.addCounter(prev, cnt));

        io.closeIO();
    }

    private static class LineHandler {
        private final BiFunction<String, String, Boolean> linesEqual;
        private final BiFunction<String, Integer, String> addCounter;
        private final Function<String, String> withSkip;

        public LineHandler(boolean countMerged, boolean regIgnored, int skip) {
            linesEqual = regIgnored? String::equalsIgnoreCase: String::equals;
            addCounter = countMerged?
                    (String prev, Integer cnt) -> (cnt + (cnt==1?" merge  | ": " merges | ")) + prev:
                    (String prev, Integer cnt) -> prev;
            withSkip = skip==0?
                    (String line) -> line:
                    (String line) -> skip < line.length()? line.substring(skip): "";
        }

        public boolean linesEqual(String prev, String line) {
            return linesEqual.apply(withSkip(prev), withSkip(line));
        }

        public String addCounter(String prev, Integer cnt) {
            return addCounter.apply(prev, cnt);
        }

        private String withSkip(String line) {
            return withSkip.apply(line);
        }

    }

    private static class IOHandler {
        private final Supplier<String> getLine;
        private final Consumer<String> printLine;
        private final Runnable closeIO;

        public IOHandler(List<String> arguments, File outFile) throws IOException {
            Runnable closeReader;
            Runnable closeWriter;

            //Setting up reader
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
                        System.err.println(e.getMessage());
                    }
                };

            } else {
                Iterator<String> linesIterator = getLinesList(arguments).listIterator();
                getLine = () -> {
                    try {
                        return linesIterator.next();
                    } catch (NoSuchElementException e) {
                        return null;
                    }
                };
                closeReader = () -> {};
            }

            //Setting up writer
            if (outFile != null) {
                if (!outFile.exists()) {
                    throw new IOException(String.format("%s (Не удается найти указанный файл)", outFile.getPath()));
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                printLine = (String line) -> {
                    try{
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                };

                closeWriter = () -> {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
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

        public void closeIO() {
            closeIO.run();
        }

        private List<String> getLinesList(List<String> arguments) {
            return splitByLines(String.join(" ", arguments));
        }

        private List<String> splitByLines(String lines) {
            //return new ArrayList<>(Arrays.asList(lines.split(System.lineSeparator())));
            return new ArrayList<>(Arrays.asList(lines.split("\\r?\\n")));
        }
    }
}