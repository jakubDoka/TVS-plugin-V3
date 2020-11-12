package main.tools;

import main.database.Stat;

import static org.junit.jupiter.api.Assertions.*;

class EnumsTest {
    public static void main(String[] args) {
        Testing.Assert(Enums.contains(Stat.class, Stat.age.name()));
    }
}