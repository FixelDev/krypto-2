package pl.lodz.p.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Dao {
    public static byte[] readFromFile(Path path) {
        if (path == null) {
            return null;
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeToFile(Path path, byte[] data) {
        if (path == null) {
            return;
        }

        try {
            Files.write(path, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
