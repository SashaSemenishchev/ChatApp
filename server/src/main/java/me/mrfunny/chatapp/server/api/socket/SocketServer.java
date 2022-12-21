package me.mrfunny.chatapp.server.api.socket;

import com.corundumstudio.socketio.*;
import me.mrfunny.chatapp.server.Main;
import me.mrfunny.chatapp.server.api.util.AccountData;

public class SocketServer {
    private final SocketIOServer io;
    public SocketServer(String host, int port) {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setTransports(Transport.POLLING);
        SocketIOServer server = new SocketIOServer(config);
        server.addMultiTypeEventListener(
                "register",
                this::handleRegister,
                String.class,
                String.class,
                String.class
        );
        server.addMultiTypeEventListener("login", this::handleLogin, String.class, String.class);
        server.addEventListener("requestUnread", int.class, this::handleRequestUnread);
        server.addMultiTypeEventListener("sendMessage", this::handleMessage, String.class, String.class, String.class);
//        server.addEventInterceptor((client, eventName, args, ackRequest) -> System.out.println(eventName));
        this.io = server;
        server.start();
    }

    private void handleLogin(SocketIOClient client, MultiTypeArgs data, AckRequest ackRequest) throws Exception {
        String email = data.get(0).toString();
        String password = data.get(1).toString();
        AccountData account = Main.server.getDb().validatePassword(email, password);
        if(account == null) {
            ackRequest.sendAckData(false, "Email doesn't exists");
            return;
        }
        if(account.username() == null) {
            ackRequest.sendAckData(false, "Invalid password");
            return;
        }
        ackRequest.sendAckData(true, account.username());
    }

    private void handleRequestUnread(SocketIOClient client, int id, AckRequest ackSender) throws Exception {
        ackSender.sendAckData(Main.server.getDb().getUnreadMessages(id));
    }

    private void handleMessage(SocketIOClient sender, MultiTypeArgs data, AckRequest ackRequest) throws Exception {
        String email = data.get(0).toString();
        String password = data.get(1).toString();
        String message = data.get(2).toString();
        AccountData account = Main.server.getDb().validatePassword(email, password);
        if(account == null || account.username() == null) {
            ackRequest.sendAckData(false, "Invalid login data");
            return;
        }
        int id;
        try {
            id = Main.server.getDb().saveMessage(account, message);
        } catch (Exception exception) {
            exception.printStackTrace();
            ackRequest.sendAckData(false, "Failed to send message due to internal server error");
            return;
        }

        String username = account.username();
        for(SocketIOClient client : io.getAllClients()) {
            client.sendEvent("message", id, username, message);
        }
        ackRequest.sendAckData(true, "OK");
    }

    private void handleRegister(SocketIOClient client, MultiTypeArgs data, AckRequest ackSender) throws Exception {
        String email = data.get(0).toString();
        String username = data.get(1).toString();
        String password = data.get(2).toString();
        if(!Main.server.getDb().canCreateEmail(email)) {
            ackSender.sendAckData(false, "Email already exists");
            return;
        }

        if(!Main.server.getDb().canCreateUsername(username)) {
            ackSender.sendAckData(false, "Username already exist");
            return;
        }
        Main.server.getDb().saveUser(email, username, password);
        ackSender.sendAckData(true, "OK");
    }

    public SocketIOServer getIo() {
        return io;
    }
}
