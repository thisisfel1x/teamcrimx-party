package net.teamcrimx.partyandfriends.api.database;

public enum MongoCollection {

    // DATABASE
    DATABASE("network"),

    // COLLECTIONS
    FRIENDS("friends");

    String collectionName;

    MongoCollection(String collectionName) {
        this.collectionName = collectionName;
    }

    public String collectionName() {
        return collectionName;
    }
}
