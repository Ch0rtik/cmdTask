package uniq;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public void merge(File inFile, File outFile) throws IOException{
        mergeLines(new IOHandler(inFile, outFile));
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
        private final Function<String, String> skipFirstSymbols;

        public LineHandler(boolean countMerged, boolean regIgnored, int skip) {
            linesEqual = regIgnored? String::equalsIgnoreCase: String::equals;
            addCounter = countMerged?
                    (String prev, Integer cnt) -> {
                        if (prev != null){
                            String merges = cnt==1?"merge ": "merges";
                            return String.format("%d %s | %s", cnt, merges, prev);
                        }
                        return "";
                    }:
                    (String prev, Integer cnt) -> prev;
            skipFirstSymbols = skip==0?
                    (String line) -> line:
                    (String line) -> skip < line.length()? line.substring(skip): "";
        }

        public boolean linesEqual(String prev, String line) {
            return linesEqual.apply(skipFirstSymbols(prev), skipFirstSymbols(line));
        }

        public String addCounter(String prev, Integer cnt) {
            return addCounter.apply(prev, cnt);
        }

        private String skipFirstSymbols(String line) {
            return skipFirstSymbols.apply(line);
        }

    }

    private static class IOHandler {
        private final Supplier<String> getLine;
        private final Consumer<String> printLine;
        private final Runnable closeIO;

        public IOHandler(File inFile, File outFile) throws IOException {
            Runnable closeReader;
            Runnable closeWriter;

            //Setting up reader
            if (inFile != null && isTextFile(inFile)) {
                if (inFile.length() == 0) throw new IllegalArgumentException("File is empty");
                BufferedReader reader = new BufferedReader(new FileReader(inFile));
                getLine = () -> {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                        return null;
                    }
                };
                closeReader = () -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                };

            } else {
                Scanner scanner = new Scanner(System.in);
                getLine = () -> {
                    if (scanner.hasNextLine()) {
                        return scanner.nextLine();
                    }
                    return null;
                };
                closeReader = scanner::close;
            }

            //Setting up writer
            if (outFile != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                printLine = (String line) -> {
                    if (line == null) return;
                    try{
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                };

                closeWriter = () -> {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                };
            } else {
                if (inFile == null || !isTextFile(inFile)) {
                    List<String> result = new ArrayList<>();
                    printLine = result::add;
                    closeWriter = () -> {
                        result.forEach(System.out::println);
                    };
                } else {
                    printLine = System.out::println;
                    closeWriter = () -> {
                    };
                }
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

        private boolean isTextFile(File inFile) throws IOException {
            if(!inFile.exists()) {
                throw new IOException(String.format("%s doesn't exist", inFile.getName()));
            }
            Path path = FileSystems.getDefault().getPath(inFile.getPath());
            String type = Files.probeContentType(path);
            if (!type.equals("text/plain")) throw new IllegalArgumentException("File is not text");
            if (inFile.length() == 0) throw new IllegalArgumentException("File is empty");
            return true;
        }
    }
}