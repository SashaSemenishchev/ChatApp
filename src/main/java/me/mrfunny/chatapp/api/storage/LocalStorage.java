package me.mrfunny.chatapp.api.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalStorage {
    private final File storageFolder;
    public LocalStorage() {
        File storage = new File("storage");
        if(!storage.exists()) {
            storage.mkdirs();
        }
        this.storageFolder = storage;
    }

    public void store(String key, byte[] value) throws IOException {
        Files.write(Path.of(storageFolder.getAbsolutePath(), key), value);
    }

    public byte[] read(String key) throws IOException {
        File toRead = new File(storageFolder, key);
        if(!toRead.exists()) return null;

        return Files.readAllBytes(toRead.toPath());
    }

    public File getStorageFolder() {
        return storageFolder;
    }
}
