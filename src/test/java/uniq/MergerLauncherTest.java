package uniq;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MergerLauncherTest {

    @Test
    void main() {
        assertThrows(IOException.class, () -> MergerLauncher.main(new String[]{ "-i","Inn.txt" }));

    }
}