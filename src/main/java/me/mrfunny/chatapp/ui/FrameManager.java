package me.mrfunny.chatapp.ui;

import javax.swing.*;

public class FrameManager {
    private final JFrame currentRawFrame;
    private Frame currentFrame;
    public FrameManager(Frame first) {
        this.currentRawFrame = new JFrame();
        currentRawFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        updateFrameOptions(first);
        currentRawFrame.setVisible(true);
    }

    public void changeFrame(Frame changeTo) {
        updateFrameOptions(changeTo);
        this.currentRawFrame.revalidate();
    }

    private void updateFrameOptions(Frame toUpdate) {
        currentRawFrame.setSize(toUpdate.getWidth(), toUpdate.getHeight());
        currentRawFrame.setContentPane(toUpdate.getRootPanel());
        currentRawFrame.setName(toUpdate.getName());
        this.currentFrame = toUpdate;
    }

    public JFrame getCurrentRawFrame() {
        return currentRawFrame;
    }

    public Frame getCurrentFrame() {
        return currentFrame;
    }
}
