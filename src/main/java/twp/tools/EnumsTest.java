package twp.tools;

import twp.database.Stat;

class EnumsTest {
    public static void main(String[] args) {
        Testing.Assert(Enums.contains(Stat.class, Stat.age.name()));
    }
}