package twp.database;

import arc.util.Log;

import static twp.Main.db;

class DataHandlerTest {
    public static void main(String[] args) {
        Log.info(db.handler.newId());
    }
}