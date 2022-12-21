package me.mrfunny.kssdb.util.exceptions;

import java.io.IOException;

public class StringTooLongException extends IOException {
    public StringTooLongException(int expected, int got) {
        super("String is too long. Expected length of " + expected + ", got " + got);
    }
}
