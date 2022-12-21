package me.mrfunny.kssdb;

import me.mrfunny.kssdb.internal.DatabaseKey;
import me.mrfunny.kssdb.internal.KeyStoredStringDatabaseImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface KeyStoredStringDatabase {
    /**
     * Connects the database objects and reads all needed data from it to the RAM
     * @param databaseFolder The folder to connect
     * @param gcAfter Should we run {@link System#gc()} after all data is read?
     * @throws IOException Thrown in case of any occurred reading error (includes corrupted exception)
     */
    void connectTo(File databaseFolder, boolean gcAfter) throws IOException;

    /**
     * Gets a value from the database by given key
     * @param key The key
     * @return A first value associated with this key
     * @throws IOException in the process it reads files, so it's needed
     */
    String get(String key) throws IOException;

    /**
     * Gets all values from the database by given key
     * @param key The key
     * @return A list of all values associated with this key
     * @throws IOException in the process it reads files, so it's needed
     */
    List<String> getAll(String key) throws IOException;

    /**
     * Gets a first values from the database by given key
     * @param key The key
     * @return A first value associated with this key
     * @throws IOException in the process it reads files, so it's needed
     */
    String get(DatabaseKey key) throws IOException;

    /**
     * Sets a value by the key
     * @param key The key to set
     * @param data The value you need to associate with the key
     * @throws IOException Any read/write errors
     */
    void set(String key, String data) throws IOException;

    /**
     * Reads all entries from the given map, and set's to the database each key/value pair
     * @param bulkSet The map database will gather data from
     * @throws IOException Any read/write errors
     * @see KeyStoredStringDatabase#newEditSession() - more intuitive way, works the same as with map
     */
    void set(Map<String, String> bulkSet) throws IOException;

    /**
     * @return List of ALL keys in the database
     */
    List<DatabaseKey> keys();

    /**
     * Edit session - is basically a list of key/value pair. Preferred over {@link KeyStoredStringDatabase#set(Map)}
     * @return A new edit session
     */
    EditSession newEditSession();

    /**
     * Gets an internal database key for faster reading. Used in case if the key would be referenced in the code multiple times.
     * @param name Internal database key
     * @return A first internal key with associated name
     */
    DatabaseKey getKeyByName(String name);

    /**
     * Gets an internal database key for faster reading. Used in case if the key would be referenced in the code multiple times.
     * @param name Internal database key
     * @return A first internal key with associated name
     */
    List<DatabaseKey> getAllKeysByName(String name);

    /**
     * Tells if the database has this key?
     * @param key The key to look up
     * @return exists - true, otherwise - false
     */
    boolean containsKey(String key);

    /**
     * @return A new database client, which not connected to the folder
     */
    static KeyStoredStringDatabase newClient() {
        return new KeyStoredStringDatabaseImpl();
    }

    /**
     * Returns a new client, preliminary connecting it to the folder
     * @param folder The folder to connect
     * @return A new database client, which is already connected to that folder
     * @throws IOException Thrown in case if database is corrupted or program has insufficient permissions to read the folder
     */
    static KeyStoredStringDatabase newClient(File folder) throws IOException {
        KeyStoredStringDatabaseImpl db = new KeyStoredStringDatabaseImpl();
        db.connectTo(folder, false);
        return db;
    }
}
