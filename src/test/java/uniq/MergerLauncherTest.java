package uniq;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergerLauncherTest {
    void assertFileOutput(String[] args, File actual, File expected) throws IOException {
        MergerLauncher.main(args);
        assertTrue(FileUtils.contentEquals(actual, expected));
    }

    void assertConsoleOutput(String[] args, String expected) {
        String consoleOutput = "";
        try {
            consoleOutput = tapSystemOut(() -> MergerLauncher.main(args));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        assertEquals(expected, consoleOutput.trim());
    }

    @Test
    void main() throws IOException {
        final String RESOURCE_PATH = "src/test/resources/";
        File tempOut = new File(RESOURCE_PATH +"tempOut.txt");
        tempOut.createNewFile();

        assertFileOutput(new String[]{"-o", tempOut.getPath(), RESOURCE_PATH + "In.txt"},
                tempOut, new File(RESOURCE_PATH +"simpleOut.txt"));

        assertFileOutput(new String[]{"-o", tempOut.getPath(), "-c", RESOURCE_PATH + "In.txt"},
                tempOut, new File(RESOURCE_PATH +"countOut.txt"));

        assertFileOutput(new String[]{"-o", tempOut.getPath(), "-i",
                                "Hello, world!\n" +
                                "Hello, world!!!\n" +
                                "hello, world!\n" +
                                "hello, world!\n" +
                                "hello, World!\n" +
                                "Hell , World!"},
                tempOut, new File(RESOURCE_PATH +"ignoreOut.txt"));

        assertFileOutput(new String[]{"-o", tempOut.getPath(), "-s", "5", RESOURCE_PATH + "In.txt"},
                tempOut, new File(RESOURCE_PATH +"skipOut.txt"));

        tempOut.delete();

        assertConsoleOutput(new String[]{"-s", "5", "-i", RESOURCE_PATH + "In.txt"},
                "Hello, world!\r\n" +
                "Hello, world!!!\r\n" +
                "hello, world!");
    }
}