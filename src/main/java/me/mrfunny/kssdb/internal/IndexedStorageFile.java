package me.mrfunny.kssdb.internal;

import java.io.File;

public class IndexedStorageFile {
    private final File theFile;
    private final long positionStart;
    private long positionEnd;

    public IndexedStorageFile(File theFile, long positionStart, long positionEnd) {
        this.theFile = theFile;
        this.positionStart = positionStart;
        this.positionEnd = positionEnd;
    }

    public File getFile() {
        return theFile;
    }

    public long getPositionStart() {
        return positionStart;
    }

    public long getPositionEnd() {
        return positionEnd;
    }

    public boolean fallsDownIntoIt(long position) {
        return (position >= positionStart) && (position < positionEnd);
    }

    @Override
    public String toString() {
        return "IndexedFile{file: " + theFile.getName() + "; start: " + positionEnd + "; end: " + positionEnd + "}";
    }

    public void setPositionEnd(long positionEnd) {
        this.positionEnd = positionEnd;
    }
}
