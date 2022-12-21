package me.mrfunny.chatapp.ui;

import javax.swing.*;

public abstract class Frame {
    private final String name;
    private int height;
    private int width;

    public Frame(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSize(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public abstract JPanel getRootPanel();
}
