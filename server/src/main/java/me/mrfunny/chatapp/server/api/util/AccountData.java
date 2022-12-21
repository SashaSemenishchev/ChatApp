package me.mrfunny.chatapp.server.api.util;

import java.util.Objects;

public final class AccountData {
    private final String email;
    private String username;
    private final String password;

    public AccountData(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String email() {
        return email;
    }

    public String username() {
        return username;
    }
    public void username(String username) {
        this.username = username;
    }

    public String password() {
        return password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        AccountData that = (AccountData) obj;
        return Objects.equals(this.email, that.email) &&
                Objects.equals(this.username, that.username) &&
                Objects.equals(this.password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, username, password);
    }
}
