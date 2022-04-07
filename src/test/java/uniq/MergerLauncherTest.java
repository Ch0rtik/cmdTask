package uniq;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergerLauncherTest {

    @Test
    void main() throws IOException {
        String path = "src/test/resources/";
        String tempPath = "src/test/resources/tempOut.txt";
        File tempOut = new File(tempPath);

        tempOut.createNewFile();

        MergerLauncher.main(new String[]{"-o", tempPath, path + "In.txt"});
        assertTrue(FileUtils.contentEquals(tempOut, new File(path + "simpleOut.txt")));

        MergerLauncher.main(new String[]{"-o", tempPath, "-c", path + "In.txt"});
        assertTrue(FileUtils.contentEquals(tempOut, new File(path + "countOut.txt")));

        MergerLauncher.main(new String[]{"-o", tempPath, "-i",
                    "Hello, world!\n" +
                    "Hello, world!!!\n" +
                    "hello, world!\n" +
                    "hello, world!\n" +
                    "hello, World!\n" +
                    "Hell , World!"});
        assertTrue(FileUtils.contentEquals(tempOut, new File(path + "ignoreOut.txt")));

        MergerLauncher.main(new String[]{"-o", tempPath, "-s", "5", path + "In.txt"});
        assertTrue(FileUtils.contentEquals(tempOut, new File(path + "skipOut.txt")));


        String consoleOutput = "";
        try {
            consoleOutput = tapSystemOut(() -> MergerLauncher.main(new String[]{"-s", "5", "-i", path + "In.txt"}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals("Hello, world!\r\n" +
                    "Hello, world!!!\r\n" +
                   "hello, world!", consoleOutput.trim());

        tempOut.delete();
    }
}