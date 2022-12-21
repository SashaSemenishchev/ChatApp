package me.mrfunny.kssdb.internal;

public class DatabaseKey {
    private final String key;
    private final long position;

    public DatabaseKey(String key, long position) {
        this.key = key;
        this.position = position;
    }

    public String getKey() {
        return key;
    }

    public long getPosition() {
        return position;
    }
}
