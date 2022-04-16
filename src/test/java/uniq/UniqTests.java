package uniq;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.*;

class UniqTests {
    private void assertFileOutput(String[] args, String expectedFileName) throws IOException {
        File expected =  new File(getPath(expectedFileName));
        File tempOut = new File(getPath("tempOut.txt"));
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

    private String getPath(String fileName) {
        return "src/test/resources/" + fileName;
    }

    @Test
    void fileInputOutput() throws IOException {
        assertFileOutput(new String[]{"-o", getPath("tempOut.txt"), getPath("In.txt")},
                "simpleOut.txt");
        assertThrows(IOException.class, () -> {
            UniqLauncher.main(new String[]{"-o", getPath("tempOut.txt"), getPath("In2.txt")});
        });
        assertThrows(IOException.class, () -> {
            UniqLauncher.main(new String[]{"-o", getPath("Out.txt"), getPath("In.txt")});
        });
    }

    @Test
    void withCounter() throws IOException {
        assertFileOutput(new String[]{"-o", getPath("tempOut.txt"), "-c", getPath("In.txt")},
                "countOut.txt");
    }

    @Test
    void ignoreCase() throws IOException {
        assertFileOutput(new String[]{"-o", getPath("tempOut.txt"), "-i", getPath("In.txt")},
                "ignoreOut.txt");

    }


    @Test
    void withSkip() throws IOException{
        assertFileOutput(new String[]{"-o", getPath("tempOut.txt"), "-s", "5", getPath("In.txt")},
                "skipOut.txt");
    }

    @Test
    void consoleOutput() {
        assertConsoleOutput(new String[]{"-s", "5", "-i", getPath("In.txt")},
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!");
    }

    /**@Test
    void consoleInput() throws IOException {
        assertFileOutput(new String[]{"-o", getPath("tempOut.txt"), "-i",
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!\r\n" +
                        "hello, world!\r\n" +
                        "hello, World!\r\n" +
                        "Hell , World!"}, "ignoreOut.txt");
    }

    @Test
    void emptyInput() {
        assertThrows(IllegalArgumentException.class, () -> UniqLauncher.main(new String[]{"-o", getPath("tempOut.txt"), getPath("EmptyIn.txt")}));
    }
    **/

}