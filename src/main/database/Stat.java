package main.database;

public enum Stat {
    playTime(0, false),
    age(0, false),
    level(0, false),

    buildingsBuilt(100),
    buildingsBroken(50),
    enemiesKilled(1),
    deaths(100),
    gamesPlayed(1000),
    gamesWon(100000),
    factoryVotes(5000),
    messageCount(0),
    loadoutVotes(3000),
    buildCoreVotes(10000),
    itemsTransported(100),
    mkgfVotes(10000);


    public int value;
    public boolean inStats = true;
    Stat(int value) {
        this.value = value;
    }

    Stat(int value, boolean inStats) {
        this.value = value;
        this.inStats = inStats;
    }
}
