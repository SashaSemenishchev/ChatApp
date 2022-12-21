package me.mrfunny.chatapp.server;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import me.mrfunny.chatapp.server.api.ChatAppServer;

import java.sql.SQLException;

public class Main {
    public static ChatAppServer server;
    public static void main(String[] args) throws Exception {
        server = new ChatAppServer(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), args[4], args[5], args[6]);
        Thread printingHook = new Thread(() -> {
            System.out.println("Shutting down");
            server.getSocket().getIo().stop();
        });
        printingHook.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(printingHook);
    }
}