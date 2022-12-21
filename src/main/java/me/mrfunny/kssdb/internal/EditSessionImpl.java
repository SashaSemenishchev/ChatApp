package me.mrfunny.kssdb.internal;

import me.mrfunny.kssdb.EditSession;
import me.mrfunny.kssdb.KeyStoredStringDatabase;
import me.mrfunny.kssdb.util.exceptions.StringTooLongException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditSessionImpl implements EditSession {
    private final KeyStoredStringDatabase database;
    private final Map<String, String> toWrite = new HashMap<>();
    private boolean applied = false;

    public EditSessionImpl(KeyStoredStringDatabase database) {
        this.database = database;
    }

    @Override
    public EditSession set(String key, String value) throws UnsupportedOperationException {
        checkApplied();
        toWrite.put(key, value);
        return this;
    }

    @Override
    public void bulkSet(Map<String, String> theMap) {
        checkApplied();
        toWrite.putAll(theMap);
    }

    @Override
    public void remove(String key) throws UnsupportedOperationException {
        checkApplied();
        toWrite.remove(key);
    }

    @Override
    public void apply() throws UnsupportedOperationException, IOException {
        checkApplied();
        for(String key : toWrite.keySet()) {
            if(key.length() > KeyStoredStringDatabaseImpl.maxKeyLength) {
                throw new UnsupportedOperationException(
                        "Map access through reflection detected - key length can't be more than " + KeyStoredStringDatabaseImpl.maxKeyLength,
                        new StringTooLongException(KeyStoredStringDatabaseImpl.maxKeyLength, key.length())
                );
            }
        }
        this.applied = true;
        database.set(toWrite);
    }

    private void checkApplied() throws UnsupportedOperationException {
        if(applied) throw new UnsupportedOperationException("The edit session is already applied");
    }
}
