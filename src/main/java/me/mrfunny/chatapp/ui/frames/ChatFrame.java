package me.mrfunny.chatapp.ui.frames;

import me.mrfunny.chatapp.Main;
import me.mrfunny.chatapp.api.data.Message;
import me.mrfunny.chatapp.ui.Frame;
import me.mrfunny.kssdb.EditSession;
import me.mrfunny.kssdb.internal.DatabaseKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChatFrame extends Frame {
    private final JTextArea chatWindow;
    private final JTextArea textInput;
    private final JPanel root;
    private final String username;
    private final JButton sendButton;

    public ChatFrame(String username) {
        super("Chat App - In Room");
        this.username = username;
        this.setSize(1000, 600);
        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 10;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 20;
        JTextArea chat = new JTextArea(23, 70);
        chat.setEnabled(false);
        JScrollPane scroll = new JScrollPane(chat, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        root.add(scroll, gbc);

        gbc.gridheight = 1;

        gbc.gridx = 0;
        gbc.gridy = 20;
        JTextArea textInput = new JTextArea(3, 70);
        JScrollPane textScroll = new JScrollPane(textInput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(textScroll, gbc);

        gbc.gridx = 10;
        gbc.gridy = 20;
        JButton sendButton = new JButton("Send");
        this.sendButton = sendButton;
        root.add(sendButton, gbc);
        InputMap inputMap = textInput.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textInput.getActionMap();

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        inputMap.put(enterKey, enterKey.toString());
        actionMap.put(enterKey.toString(), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleSend(event);
            }
        });

        textInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                handleNewLine(event);
            }
        });

        sendButton.addActionListener(this::handleSend);

        this.chatWindow = chat;
        this.textInput = textInput;
        this.root = root;
        DatabaseKey lastCached = null;
        try {
            for(DatabaseKey key : Main.cache.keys()) {
                try {
                    addText(Main.cache.get(key));
                    lastCached = key;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            lastCached = null;
            try {
                Main.regenerateCache();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }


        EditSession unreadMessages = Main.cache.newEditSession();
        Main.client.getSocket()
                .requestUnread(lastCached == null ? 0 : Integer.parseInt(lastCached.getKey()))
                .thenAccept(messages -> {
                    for(Message message : messages) {
                        unreadMessages.set(String.valueOf(message.id()), message.content());
                        addText(message.content());
                    }
                    try {
                        unreadMessages.apply();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.chatWindow.setCaretPosition(this.chatWindow.getText().length() - 1);
                });
    }

    private boolean sendInProgress = false;

    private void handleSend(ActionEvent event) {
        JTextArea area = this.textInput;
        String message = area.getText();
        if(message == null) return;
        message = message.trim();
        if(message.equals("")) return;
        sendStatus(true);
        ScheduledFuture<?> future = Main.scheduler.schedule(() -> {
            if(sendInProgress) {
                sendStatus(false);
                JOptionPane.showMessageDialog(Main.frameManager.getCurrentRawFrame(), "Message send timeout", "Error while sending message", JOptionPane.ERROR_MESSAGE);
            }
        }, 10, TimeUnit.SECONDS);
        Main.client.getSocket().sendMessage(Main.getLoggedAs(), message).thenAccept(result -> {
            future.cancel(true);
            if(!result.successful()) {
                JOptionPane.showMessageDialog(Main.frameManager.getCurrentRawFrame(), result.message(), "Error while sending message", JOptionPane.ERROR_MESSAGE);
                return;
            }
            area.setText(null);
            area.setCaretPosition(0);
            sendStatus(false);
        });
    }

    public void sendStatus(boolean sending) {
        this.textInput.setEnabled(!sending);
        this.sendButton.setEnabled(!sending);
        this.sendInProgress = sending;
        if(!sending) {
            this.textInput.requestFocus();
        }
    }

    private void handleNewLine(KeyEvent event) {
        if(event.getKeyCode() != KeyEvent.VK_ENTER) return;
        Object source = event.getSource();
        boolean isSend = !event.isControlDown();
        if(source instanceof JTextArea area) {
            if(!area.isEnabled()) return;
            if(!isSend) {
                area.setText(area.getText() + "\n");
                area.setCaretPosition(area.getText().length());
            }
        }
    }

    public JTextArea getChatWindow() {
        return chatWindow;
    }

    public void addText(String toAdd) {
        this.chatWindow.setText(this.chatWindow.getText() + "\n" + toAdd);
        this.chatWindow.setCaretPosition(this.chatWindow.getText().length() - 1);
    }

    @Override
    public JPanel getRootPanel() {
        return root;
    }
}
