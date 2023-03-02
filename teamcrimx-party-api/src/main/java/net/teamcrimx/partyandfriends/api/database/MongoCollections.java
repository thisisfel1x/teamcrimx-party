package net.teamcrimx.partyandfriends.api.database;

public enum MongoCollections {

    // DATABASE
    DATABASE("ntework"),

    // COLLECTIONS
    FRIENDS("friends");

    String collectionName;

    MongoCollections(String collectionName) {
        this.collectionName = collectionName;
    }

    public String collecitonName() {
        return collectionName;
    }
}
