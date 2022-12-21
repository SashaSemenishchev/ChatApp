package me.mrfunny.kssdb.internal;

import me.mrfunny.kssdb.EditSession;
import me.mrfunny.kssdb.KeyStoredStringDatabase;
import me.mrfunny.kssdb.util.ByteConverters;
import me.mrfunny.kssdb.util.exceptions.DatabaseCorruptedException;
import me.mrfunny.kssdb.util.exceptions.FailedToCreateDatabaseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KeyStoredStringDatabaseImpl implements KeyStoredStringDatabase {
    private final static byte headerMagicValue = 0x37; // hex sum of alphabet index of letters kssdb
    private final static byte supportedVersion = 0x1; // hex sum of alphabet index of letters kssdb
    public final static int maxFileSize = 32000000;
    public final static int maxKeyLength = 255;
    private ArrayList<DatabaseKey> keys;
    private File keyIndexesFile;
    private File metaFile;
    private File dataDirectory;
    private ArrayList<IndexedStorageFile> indexedFiles;
    private boolean read = false;

    @Override
    public void connectTo(File folder, boolean gcAfter) throws IOException {
        if(read) {
            throw new IOException("Database is already locked");
        }
        if(!folder.exists()) {
            isEmpty = true;
            if(!folder.mkdirs()) {
                throw new IOException("Failed to create database folder on initialization");
            }
        }
        this.metaFile = new File(folder + File.separator + "meta");
        this.keyIndexesFile = new File(folder + File.separator + "keys");
        this.dataDirectory = new File(folder + File.separator + "data");
        readMeta();
        readKeyIndexes();
        indexDataFiles();
        this.read = true;
        if(gcAfter) {
            System.gc();
        }
    }

    private boolean isEmpty = false;

    private long lastPosition;

    private void readMeta() throws IOException {
        int entriesCount;
        try(FileInputStream stream = new FileInputStream(metaFile)) {
            byte[] header = stream.readNBytes(2);
            if(header[0] != headerMagicValue) {
                throw new StreamCorruptedException("Corrupted Header of the kssdb file");
            }

            if(header[1] != supportedVersion) {
                throw new UnsupportedEncodingException("This version of KSSDB supports encoding of version: " + supportedVersion);
            }

            entriesCount = ByteConverters.bytesToInt(stream.readNBytes(4));
        } catch (FileNotFoundException exception) {
            isEmpty = true;
            entriesCount = 0;
            createInitialData();
        }
        keys = new ArrayList<>(entriesCount);
    }

    public void readKeyIndexes() throws IOException {
        if(isEmpty) return;
        try(FileInputStream stream = new FileInputStream(keyIndexesFile)) {
            while(stream.available() > 0) {
                try {
                    byte[] lengthBytes = stream.readNBytes(4);
                    int length = ByteConverters.bytesToInt(lengthBytes); // the first byte is integer that defines the length of the key
                    long position = ByteConverters.bytesToLong(stream.readNBytes(8)); // 8 bytes for long value, that determines the position of the text in the db
                    String key = new String(stream.readNBytes(length));
                    keys.add(new DatabaseKey(key, position));
                } catch (IOException e) { // any byte missing - means database is corrupted
                    throw new DatabaseCorruptedException();
                }
            }
        } catch (FileNotFoundException exception) {
            throw new DatabaseCorruptedException();
        }
    }
    private void createInitialData() throws IOException {
        writeMeta(0);
        Files.write(keyIndexesFile.toPath(), new byte[0]); // empty file
    }

    private void writeMeta(int entriesCount) throws IOException {
        byte[] header = createMetadata(entriesCount);
        Files.write(metaFile.toPath(), header);
    }

    private byte[] createMetadata(int entriesCount) {
        byte[] bytes = new byte[6];
        bytes[0] = headerMagicValue;
        bytes[1] = supportedVersion;
        System.arraycopy(ByteConverters.intToBytes(entriesCount), 0, bytes, 2, 4);
        return bytes;
    }

    private void indexDataFiles() throws IOException {
        if(isEmpty) {
            if(!dataDirectory.mkdirs()) {
                throw new FailedToCreateDatabaseException("cannot access a data folder");
            }
        }
        long metPositions = -1; // the first iteration would make be 0, the second would be upper.
        File[] files = dataDirectory.listFiles();
        if(files == null) return;
        indexedFiles = new ArrayList<>(files.length);
        for(File dataFile : files) {
            indexedFiles.add(new IndexedStorageFile(dataFile, ++metPositions, metPositions += dataFile.length()));
        }

        this.lastPosition = metPositions;
    }

    @Override
    public String get(String key) throws IOException {
        // read all index files
        for(DatabaseKey entry : keys) {
            if(entry.getKey().equals(key)) {
                return get(entry);
            }
        }
        return null;
    }

    @Override
    public List<String> getAll(String key) throws IOException {
        ArrayList<String> result = new ArrayList<>(0);
        for(DatabaseKey entry : keys) {
            if(entry.getKey().equals(key)) {
                result.add(get(entry));
            }
        }
        return result;
    }

    @Override
    public String get(DatabaseKey key) throws IOException {
        return read(key.getPosition());
    }

    private String read(long position) throws IOException {
        IndexedStorageFile storageFile = indexedFiles
                .stream()
                .filter(file -> file.fallsDownIntoIt(position))
                .findFirst()
                .orElse(null);
        if(storageFile == null) {
            System.err.println("No found storage files, returning null: size of them: " + indexedFiles.size());
            return null;
        }
        int subPosition = (int) (position - storageFile.getPositionStart());
        String result;
        try(FileInputStream stream = new FileInputStream(storageFile.getFile())) {
            byte[] fileBytes = stream.readAllBytes();
            byte[] lengthBytes = new byte[4]; // 4 bytes for int
            System.arraycopy(fileBytes, subPosition, lengthBytes, 0, 4);
            int length = ByteConverters.bytesToInt(lengthBytes);
            byte[] string = new byte[length];
            System.arraycopy(fileBytes, subPosition + 4, string, 0, length);
            result = new String(string);
        }
        return result;
    }

    @Override
    public void set(String key, String value) throws IOException {
        FileOutputStream indexes = new FileOutputStream(keyIndexesFile, true);
        write(indexes, key, value);
        indexes.close();
        writeMeta(keys.size());
    }

    @Override
    public void set(Map<String, String> bulkSet) throws IOException {
        FileOutputStream indexes = new FileOutputStream(keyIndexesFile, true);
        for(Map.Entry<String, String> entry : bulkSet.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            write(indexes, key, value);
        }
        indexes.close();
        writeMeta(keys.size());
    }

    @Override
    public List<DatabaseKey> keys() {
        return keys;
    }

    private void write(FileOutputStream indexes, String key, String value) throws IOException{
        if(indexedFiles.isEmpty()){
            indexedFiles.add(createNewFile());
        }
        IndexedStorageFile lastFile = getLastStorageFile();
        if((lastFile.getFile().length() + calculateBytesSizeToWrite(value)) > maxFileSize) {
            lastFile = createNewFile();
            indexedFiles.add(lastFile);
        }
        long position = this.lastPosition + 1;
        FileOutputStream dataFile = new FileOutputStream(lastFile.getFile(), true);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] valueWriteData = new byte[4 + valueBytes.length]; // 4 bytes for length in int

        System.arraycopy(ByteConverters.intToBytes(valueBytes.length), 0, valueWriteData, 0, 4);
        System.arraycopy(valueBytes, 0, valueWriteData, 4, valueBytes.length);
        dataFile.write(valueWriteData);
        this.lastPosition += valueWriteData.length;
        lastFile.setPositionEnd(lastFile.getPositionEnd() + valueWriteData.length);
        dataFile.close();
        // write the index

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        int keyLength = keyBytes.length;
        byte[] indexWriteData = new byte[12 + keyLength]; // 12 is for 4 bytes for the length of the key and 8 bytes is for the position in the data section
        byte[] stringLength = ByteConverters.intToBytes(keyLength);
        System.arraycopy(stringLength, 0, indexWriteData, 0, 4);
        System.arraycopy(ByteConverters.longToBytes(position), 0, indexWriteData, 4, 8);
        System.arraycopy(keyBytes, 0, indexWriteData, 12, keyLength);
        indexes.write(indexWriteData);
        keys.add(new DatabaseKey(key, position));
    }

    private int calculateBytesSizeToWrite(String string) {
        return string.getBytes(StandardCharsets.UTF_8).length + 4; // 4 bytes for int
    }

    private IndexedStorageFile createNewFile() throws IOException {
        File newFile = new File(dataDirectory + File.separator + UUID.randomUUID());
        long positionStart = 0;
        long positionEnd = 0;
        if(!indexedFiles.isEmpty()) {
            IndexedStorageFile last = getLastStorageFile();
            positionStart = last.getPositionEnd() + 1;
            positionEnd = positionStart;
        }
        if(!newFile.createNewFile()) {
            throw new IOException("Failed to create database entry and new file to write");
        }
        return new IndexedStorageFile(newFile, positionStart, positionEnd);
    }

    private IndexedStorageFile getLastStorageFile() {
        return indexedFiles.get(indexedFiles.size() - 1);
    }

    @Override
    public EditSession newEditSession() {
        return new EditSessionImpl(this);
    }

    @Override
    public DatabaseKey getKeyByName(String name) {
        for(DatabaseKey key : keys) {
            if(key.getKey().equals(name)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public List<DatabaseKey> getAllKeysByName(String name) {
        ArrayList<DatabaseKey> result = new ArrayList<>();
        for(DatabaseKey key : keys) {
            if(key.getKey().equals(name)) {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(String key) {
        for(DatabaseKey entry : keys) {
            if(entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
}
