package me.mrfunny.chatapp.api;

import io.socket.client.IO;
import io.socket.client.Manager;
import me.mrfunny.chatapp.Main;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.api.data.Message;
import me.mrfunny.chatapp.api.socket.SocketClient;
import me.mrfunny.chatapp.api.storage.LocalStorage;

import java.beans.EventHandler;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class MessengerClient {
    private final SocketClient socket;
    private final LocalStorage storage = new LocalStorage();
    private Consumer<Message> onMessage;
    public MessengerClient(String host) {
        try {
            this.socket = new SocketClient(IO.socket(host));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMessageReceiver(Consumer<Message> onMessage) {
        this.onMessage = onMessage;
    }
    public LocalStorage getStorage() {
        return storage;
    }

    public SocketClient getSocket() {
        return socket;
    }

    public void connect() {
        this.socket.getIO().connect();

        this.socket.getIO().on("message", args -> {
            onMessage.accept(new Message((int) args[0], args[1] + ": " + args[2]));
        });
    }

    public void saveAccountDetails(AccountData data) throws IOException {
        ByteArrayOutputStream bus = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bus)) {
            out.writeObject(data);
        }
        storage.store("account", bus.toByteArray());
    }

    public AccountData loadAccountDetails() {
        try {
            byte[] accountMeta = storage.read("account");
            if(accountMeta == null) return null;
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(accountMeta));
            Object object = in.readObject();
            if(object instanceof AccountData read) {
                return read;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
