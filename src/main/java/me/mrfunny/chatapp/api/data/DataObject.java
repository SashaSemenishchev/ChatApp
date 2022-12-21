package me.mrfunny.chatapp.api.data;

import java.io.*;
import java.lang.reflect.Field;

public class DataObject implements Serializable {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(Field field : this.getClass().getDeclaredFields()) {
            try {
                result.append(field.getName()).append("=").append(field.get(this).toString()).append(";");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return result.toString();
    }
}
