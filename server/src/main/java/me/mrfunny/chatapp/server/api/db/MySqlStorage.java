package me.mrfunny.chatapp.server.api.db;

import me.mrfunny.chatapp.server.api.util.AccountData;
import me.mrfunny.chatapp.server.api.util.CipherUtil;

import java.sql.*;
import java.util.HashMap;

public class MySqlStorage {
    private final Connection connection;
    public MySqlStorage(String host, int port, String database, String username, String password) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.connection = DriverManager.getConnection(
        "jdbc:mysql://" +
                host + ":" + port + "/" +
                database +
                "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&useTimezone=true&serverTimezone=GMT",
            username,
            password
        );
        Statement statement = this.connection.createStatement();
        statement.execute(
                "CREATE TABLE IF NOT EXISTS users (id int not null auto_increment, email VARCHAR(256), username VARCHAR(20), password VARCHAR(512), PRIMARY KEY(id));"
        );
        statement.execute(
                "CREATE TABLE IF NOT EXISTS messages (id int not null auto_increment, sender VARCHAR(20), content TEXT, PRIMARY KEY(id));"
        );
        statement.close();
    }

    public void saveUser(String email, String username, String password) throws Exception {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO `users` (email, username, password) VALUES (?, ?, ?)"
        );
        statement.setObject(1, email);
        statement.setObject(2, username);
        statement.setObject(3, CipherUtil.sha256(password));
        statement.executeUpdate();
    }

    public boolean canCreateEmail(String email) throws Exception {
        PreparedStatement statement = connection.prepareStatement("SELECT email FROM users WHERE email=?");
        statement.setObject(1, email);
        ResultSet query = statement.executeQuery();
        if(query.next()) {
            statement.close();
            return false;
        }
        statement.close();
        return true;
    }

    public boolean canCreateUsername(String username) throws Exception {
        PreparedStatement statement = connection.prepareStatement("SELECT username FROM users WHERE username=?");
        statement.setObject(1, username);
        ResultSet query = statement.executeQuery();
        if (query.next()) {
            statement.close();
            return false;
        }
        statement.close();
        return true;
    }

    public AccountData validatePassword(String email, String password) throws Exception {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email=?");
        statement.setObject(1, email);
        ResultSet result = statement.executeQuery();
        if(result.next()) {
            String passwordHashed = CipherUtil.sha256(password);
            AccountData toReturn;
            if(passwordHashed.equals(result.getString("password"))) {
                toReturn = new AccountData(
                        result.getString("email"),
                        result.getString("username"),
                        passwordHashed
                );
            } else {
                toReturn = new AccountData(null, null, null);
            }
            statement.close();
            return toReturn;
        }
        statement.close();
        return null;
    }

    public int saveMessage(AccountData data, String message) throws Exception {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO messages (sender, content) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setObject(1, data.username());
        statement.setObject(2, message);
        statement.executeUpdate();
        ResultSet generated = statement.getGeneratedKeys();
        int result = 0;
        if(generated.next()) {
            result = generated.getInt(1);
        }
        statement.close();
        return result;
    }

    public HashMap<Integer, String> getUnreadMessages(int lastId) throws Exception {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id > ?");
        statement.setObject(1, lastId);
        ResultSet resultSet = statement.executeQuery();
        HashMap<Integer, String> result = new HashMap<>();
        while(resultSet.next()) {
            result.put(resultSet.getInt("id"), resultSet.getString("sender") + ": " + resultSet.getString("content"));
        }
        statement.close();
        return result;
    }
}
