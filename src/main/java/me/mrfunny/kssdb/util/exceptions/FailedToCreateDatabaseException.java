package me.mrfunny.kssdb.util.exceptions;

import java.io.IOException;

public class FailedToCreateDatabaseException extends IOException {
    public FailedToCreateDatabaseException(String subReason) {
        super("Failed to create database: " + subReason);
    }
}
