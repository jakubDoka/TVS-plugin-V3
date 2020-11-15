package twp.bundle;

import arc.util.Log;

public class BundleTest {
    public static void main(String[] args) {
        Bundle bundle = new Bundle();
        Log.info(bundle.getDefault("playerNotFound"));
    }
}
