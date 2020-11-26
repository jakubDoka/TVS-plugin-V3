package twp.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Files {

    private static String toString(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        Stream<String> stream = java.nio.file.Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);

        stream.forEach(s -> contentBuilder.append(s).append("\n"));

        return contentBuilder.toString();
    }
}
