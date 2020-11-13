package twp.tools;

public class Testing {
    public static void Assert(boolean val) {
        if (!val) {
            throw new RuntimeException("assetr false");
        }
    }
}
