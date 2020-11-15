package twp.tools;

public class Testing {
    public static void Log(String message) {
        new RuntimeException(message).printStackTrace();
    }
}
