package me.mrfunny.kssdb;

import java.io.IOException;
import java.util.Map;

public interface EditSession {
    /**
     * Adds key/value pair to buffer
     * @param key Key to add
     * @param value Value to add
     * @return The same object, for builder-like interface
     * @throws UnsupportedOperationException - thrown in case if the edit session is already got applied to the database
     */
    EditSession set(String key, String value) throws UnsupportedOperationException;
    /**
     * Adds key/value pair to buffer
     * @param theMap Adds all entries from the map to the edit session
     * @throws UnsupportedOperationException - thrown in case if the edit session is already got applied to the database
     */
    void bulkSet(Map<String, String> theMap) throws UnsupportedOperationException;
    /**
     * Removes key from the edit session. Note! This does not remove a key from the database, this removes the key and its value ONLY from the edit session context
     * @param key The key to remove
     * @throws UnsupportedOperationException - thrown in case if the edit session is already got applied to the database
     */
    void remove(String key) throws UnsupportedOperationException;
    /**
     * Applies all changes from the edit session using only 1 read/write buffer.
     * @throws UnsupportedOperationException - thrown in case if the edit session is already got applied to the database
     */
    void apply() throws UnsupportedOperationException, IOException;
}
