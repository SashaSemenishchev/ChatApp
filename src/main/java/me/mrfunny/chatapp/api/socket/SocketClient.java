package me.mrfunny.chatapp.api.socket;

import io.socket.client.Ack;
import io.socket.client.Socket;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.api.data.Message;
import me.mrfunny.chatapp.api.data.RequestResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SocketClient {
    private final Socket socket;
    public SocketClient(Socket socket) {
        this.socket = socket;
    }

    public CompletableFuture<RequestResult> register(AccountData toRegister) {
        CompletableFuture<RequestResult> future = new CompletableFuture<>();
        socket.emit("register", toRegister.email(), toRegister.username(), toRegister.password(), (Ack) args -> future.complete(new RequestResult((boolean) args[0], args[1].toString())));
        return future;
    }

    public CompletableFuture<RequestResult> login(AccountData data) {
        CompletableFuture<RequestResult> future = new CompletableFuture<>();
        socket.emit("login", data.email(), data.password(), (Ack) args -> future.complete(new RequestResult((boolean) args[0], args[1].toString())));
        return future;
    }

    public CompletableFuture<RequestResult> sendMessage(AccountData loggedAs, String message) {
        CompletableFuture<RequestResult> future = new CompletableFuture<>();
        socket.emit("sendMessage", loggedAs.email(), loggedAs.password(), message, (Ack) args -> future.complete(new RequestResult((boolean) args[0], args[1].toString())));
        return future;
    }

    public CompletableFuture<List<Message>> requestUnread(int lastCached) {
        CompletableFuture<List<Message>> future = new CompletableFuture<>();
        socket.emit("requestUnread", lastCached, (Ack) args -> {
            List<Message> result = new ArrayList<>();
            JSONObject object = (JSONObject) args[0];
            Iterator<?> keys = object.keys();
            while(keys.hasNext()) {
                String key = keys.next().toString();
                try {
                    result.add(new Message(Integer.parseInt(key), object.getString(key)));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            future.complete(result);
        });
        return future;
    }

    public Socket getIO() {
        return socket;
    }
}
