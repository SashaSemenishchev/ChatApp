package me.mrfunny.kssdb.util.exceptions;

import java.io.IOException;

public class DatabaseCorruptedException extends IOException {
    public DatabaseCorruptedException() {
        super("The database is corrupted");
    }

    public DatabaseCorruptedException(String subReason) {
        super("The database is corrupted: " + subReason);
    }
}
