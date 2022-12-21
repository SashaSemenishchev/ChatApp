package me.mrfunny.chatapp.server.api;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Properties;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.MultiTypeEventListener;
import me.mrfunny.chatapp.server.api.db.MySqlStorage;
import me.mrfunny.chatapp.server.api.socket.SocketServer;

public class ChatAppServer {
    private final SocketServer server;
    private final MySqlStorage db;
    public ChatAppServer(String serverHost, int serverPort, String host, int port, String database, String username, String password) throws SQLException {
        this.db = new MySqlStorage(host, port, database, username, password);
        this.server = new SocketServer(serverHost, serverPort);
    }

    public MySqlStorage getDb() {
        return db;
    }

    public SocketServer getSocket() {
        return server;
    }
}
