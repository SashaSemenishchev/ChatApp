package me.mrfunny.chatapp.ui.frames;

import me.mrfunny.chatapp.Main;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.ui.Frame;
import me.mrfunny.chatapp.ui.component.PlaceholderPasswordField;
import me.mrfunny.chatapp.ui.component.PlaceholderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static me.mrfunny.chatapp.api.util.StringUtil.isBadString;

public class WelcomeFrame extends Frame {
    private final PlaceholderTextField email = new PlaceholderTextField();
    private final PlaceholderPasswordField password = new PlaceholderPasswordField();
    private final JButton loginButton;
    private final JPanel root;
    public WelcomeFrame() {
        super("ChatApp - Welcome");
        this.setSize(500, 600);
        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());
        final int firstGrid = 10;

        GridBagConstraints gbc = new GridBagConstraints();
        JLabel mainLabel = new JLabel("Welcome to ChatApp");
        mainLabel.setHorizontalAlignment(GridBagConstraints.CENTER);

        gbc.gridx = firstGrid;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        root.add(mainLabel, gbc);
        gbc.gridx = firstGrid;
        gbc.gridy = 1;
        email.setPlaceholder("Email");
        root.add(email, gbc);
        gbc.gridx = firstGrid;
        gbc.gridy = 2;
        password.setPlaceholder("Password");
        root.add(password, gbc);
        gbc.gridx = firstGrid;
        gbc.gridy = 3;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        this.loginButton = loginButton;
        root.add(loginButton, gbc);

        gbc.gridy = 4;
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 0.1;

        root.add(separator, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 5;
        root.add(new JLabel("Don't have an account?"), gbc);
        gbc.gridy = 6;
        JButton registerButton = new JButton("Register");
        root.add(registerButton, gbc);
        registerButton.addActionListener(event -> {
            Main.frameManager.changeFrame(new RegisterFrame());
        });
        this.root = root;
    }

    public PlaceholderTextField getEmail() {
        return email;
    }

    public PlaceholderPasswordField getPassword() {
        return password;
    }

    private void handleLogin(ActionEvent event) {

        String email = this.email.getText();
        String password = String.valueOf(this.password.getPassword());
        if(isBadString(email)) {
            error("Email can't be empty");
            return;
        } else if(isBadString(password)) {
            error("Password can't be empty");
            return;
        }
        this.loginButton.setEnabled(false);
        AccountData account = new AccountData(email, null, password);
        Main.client.getSocket().login(account).thenAccept((data) -> {
            this.loginButton.setEnabled(true);
            if(!data.successful()) {
                JOptionPane.showMessageDialog(Main.frameManager.getCurrentRawFrame(), data.message(), "Login error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            account.username(data.message());
            Main.login(account);
            Main.frameManager.changeFrame(new ChatFrame(account.username()));
        });
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(Main.frameManager.getCurrentRawFrame(), message, "Error while logging in", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public JPanel getRootPanel() {
        return this.root;
    }
}
