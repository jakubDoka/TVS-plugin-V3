package twp.database.enums;

// all the stats that user profile stores
public enum Stat {
    playTime(0, true),
    age(0, true),
    level(0),

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
    missedVotes(-1000),
    missedVotesCombo(0),
    mkgfVotes(10000);


    public int value;
    public boolean time;
    Stat(int value) {
        this.value = value;
    }

    Stat(int value, boolean time) {
        this.value = value;
        this.time = time;
    }
}
