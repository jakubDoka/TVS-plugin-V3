package main.database;

import arc.util.Log;

import static main.Main.db;

class DataHandlerTest {
    public static void main(String[] args) {
        Log.info(db.handler.newId());
    }
}