package uniq;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniqTests {
    private final String RESOURCE_PATH = "src/test/resources/";
    private void assertFileOutput(String[] args, File expected) throws IOException {
        File tempOut = new File(RESOURCE_PATH +"tempOut.txt");
        tempOut.createNewFile();
        UniqLauncher.main(args);

        assertTrue(FileUtils.contentEquals(tempOut, expected));

        tempOut.delete();
    }

    private void assertConsoleOutput(String[] args, String expected) {
        String consoleOutput = "";
        try {
            consoleOutput = tapSystemOut(() -> UniqLauncher.main(args));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        assertEquals(expected, consoleOutput.trim());
    }

    private void assertConsoleErr(String[] args, String expected) {
        String consoleOutput = "";
        try {
            consoleOutput = tapSystemErr(() -> UniqLauncher.main(args));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        assertEquals(expected, consoleOutput.trim());
    }

    @Test
    void fileInputOutput() throws IOException {
        assertFileOutput(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", RESOURCE_PATH + "In.txt"},
                new File(RESOURCE_PATH +"simpleOut.txt"));

        assertConsoleErr(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", RESOURCE_PATH + "In2.txt"},
                "src\\test\\resources\\In2.txt (Не удается найти указанный файл)");

        assertConsoleErr(new String[]{"-o", RESOURCE_PATH + "Out.txt", RESOURCE_PATH + "In.txt"},
                "src\\test\\resources\\Out.txt (Не удается найти указанный файл)");
    }

    @Test
    void withCounter() throws IOException {
        assertFileOutput(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", "-c", RESOURCE_PATH + "In.txt"},
                new File(RESOURCE_PATH +"countOut.txt"));
    }

    @Test
    void ignoreCase() throws IOException {
        assertFileOutput(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", "-i", RESOURCE_PATH + "In.txt"},
                new File(RESOURCE_PATH +"ignoreOut.txt"));

    }

    @Test
    void consoleInput() throws IOException {
        assertFileOutput(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", "-i",
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!\r\n" +
                        "hello, world!\r\n" +
                        "hello, World!\r\n" +
                        "Hell , World!"}, new File(RESOURCE_PATH +"ignoreOut.txt"));
    }

    @Test
    void withSkip() throws IOException{
        assertFileOutput(new String[]{"-o", RESOURCE_PATH + "tempOut.txt", "-s", "5", RESOURCE_PATH + "In.txt"},
                new File(RESOURCE_PATH +"skipOut.txt"));
    }

    @Test
    void consoleOutput() {
        assertConsoleOutput(new String[]{"-s", "5", "-i", RESOURCE_PATH + "In.txt"},
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!");
    }
}