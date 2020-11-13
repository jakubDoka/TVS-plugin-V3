package twp.database;

public enum Perm {
    normal(0),
    high(1),
    higher(2),
    highest(3),

    loadout(Stat.loadoutVotes),
    factory(Stat.factoryVotes),
    restart,
    change,
    gameOver,
    build,
    destruct,
    suicide,
    colorCombo,
    antiGrief(Stat.mkgfVotes),
    skip,
    coreBuild(Stat.buildCoreVotes);

    public int value = -1;
    public Stat relation = null;

    Perm(Stat relation) {
        this.relation = relation;
        this.value = 0;
    }

    Perm() {
    }

    Perm(int value) {
        this.value = value;
    }
}
