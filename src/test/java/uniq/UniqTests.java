package uniq;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;;

import java.io.File;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withTextFromSystemIn;
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
    }

    @Test
    void noInputFile() {
        assertThrows(IOException.class, () -> {
            UniqLauncher.main(new String[]{"-o", getPath("tempOut.txt"), getPath("In2.txt")});
        });

    }

    @Test
    void emptyInputFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            UniqLauncher.main(new String[]{"-o", getPath("tempOut.txt"), getPath("EmptyIn.txt")});});

    }

    @Test
    void consoleInput() throws IOException, Exception {
        withTextFromSystemIn("Hello, world!",
                "Hello, world!!!",
                "hello, world!",
                "hello, world!",
                "hello, World!",
                "Hell , World!").execute(() -> {assertFileOutput(new String[]{"-o", getPath("tempOut.txt")}, "simpleOut.txt");});
    }

    @Test
    void emptyConsoleInput() throws Exception {
        withTextFromSystemIn().execute(() -> {assertThrows(IllegalArgumentException.class, () ->{
            UniqLauncher.main(new String[]{});
        });});
    }

    @Test
    void consoleOutput() {
        assertConsoleOutput(new String[]{getPath("In.txt")},
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!\r\n" +
                        "hello, World!\r\n" +
                        "Hell , World!");
    }

    @Test
    void consoleInputOutput() throws Exception{
        withTextFromSystemIn("Hello, world!",
                "Hello, world!!!",
                "hello, world!",
                "hello, world!",
                "hello, World!",
                "Hell , World!").execute(() -> assertConsoleOutput(new String[]{},
                "Hello, world!\r\n" +
                        "Hello, world!!!\r\n" +
                        "hello, world!\r\n" +
                        "hello, World!\r\n" +
                        "Hell , World!"));
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
    void negativeSkip(){
        assertThrows(IllegalArgumentException.class, ()-> {
            UniqLauncher.main(new String[]{"-o", getPath("tempOut.txt"), "-s", "-5", getPath("In.txt")});});
    }

    @Test
    void uniqueOnly() throws IOException{
        assertFileOutput(new String[] {"-u", "-o", getPath("tempOut.txt"), getPath("In.txt")}, "uniqueOut.txt");
    }

    @Test
    void everyPossibleOption() throws IOException{
        assertFileOutput(new String[] {"-o", getPath("tempOut.txt"), "-s", "5", "-i", "-c", getPath("In.txt")}, "everyOut.txt");

    }
}