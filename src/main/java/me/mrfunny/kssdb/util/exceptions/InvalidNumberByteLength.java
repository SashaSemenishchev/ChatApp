package me.mrfunny.kssdb.util.exceptions;

import java.io.IOException;

public class InvalidNumberByteLength extends IOException {
    public InvalidNumberByteLength(String reason) {
        super(reason);
    }
}
