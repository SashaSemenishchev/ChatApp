package me.mrfunny.chatapp.ui.frames;

import me.mrfunny.chatapp.Main;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.ui.Frame;
import me.mrfunny.chatapp.ui.component.PlaceholderPasswordField;
import me.mrfunny.chatapp.ui.component.PlaceholderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;

import static me.mrfunny.chatapp.api.util.StringUtil.isBadString;

public class RegisterFrame extends Frame {
    private final PlaceholderTextField username = new PlaceholderTextField();
    private final PlaceholderTextField email = new PlaceholderTextField();
    private final PlaceholderPasswordField password = new PlaceholderPasswordField();
    private final PlaceholderPasswordField repeatPassword = new PlaceholderPasswordField();
    private final JPanel root;

    private final JButton registerButton;
    public RegisterFrame() {
        super("ChatApp - Register");
        this.setSize(500, 600);
        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());
        final int firstGrid = 10;

        GridBagConstraints gbc = new GridBagConstraints();
        JLabel mainLabel = new JLabel("Register to ChatApp");
        mainLabel.setHorizontalAlignment(GridBagConstraints.CENTER);

        gbc.gridx = firstGrid;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        root.add(mainLabel, gbc);

        gbc.gridy++;
        username.setPlaceholder("Username");
        root.add(username, gbc);

        gbc.gridy++;
        email.setPlaceholder("Email");
        root.add(email, gbc);

        gbc.gridy++;
        password.setPlaceholder("Password");
        root.add(password, gbc);

        gbc.gridy++;
        repeatPassword.setPlaceholder("Repeat password");
        root.add(repeatPassword, gbc);

        gbc.gridy++;
        JButton registerButton = new JButton("Register");
        this.registerButton = registerButton;
        root.add(registerButton, gbc);

        registerButton.addActionListener(this::handleRegister);

        gbc.gridy++;
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 0.1;
        root.add(separator, gbc);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;
        JLabel account = new JLabel("Have an account");
        account.setHorizontalAlignment(JLabel.CENTER);
        root.add(account, gbc);

        gbc.gridy++;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> Main.frameManager.changeFrame(new WelcomeFrame()));
        root.add(loginButton, gbc);

        this.root = root;
    }

    private void handleRegister(ActionEvent event) {
        char[] passwordCharacters = repeatPassword.getPassword();
        if(!Arrays.equals(passwordCharacters, password.getPassword())) {
            error("Passwords don't match");
            return;
        }

        String password = String.valueOf(passwordCharacters);
        String email = this.email.getText();
        String username = this.username.getText();

        if(isBadString(password)) {
            error("Password can't be empty");
            return;
        } else if(isBadString(email)) {
            error("Email can't be empty");
            return;
        } else if(isBadString(username)) {
            error("Username can't be empty");
            return;
        }
        this.registerButton.setEnabled(false);

        AccountData toRegister = new AccountData(email, username, password);
        Main.client.getSocket().register(toRegister).thenAccept(result -> {
            this.registerButton.setEnabled(true);
            if(!result.successful()) {
                error(result.message());
                return;
            }
            Main.login(toRegister);
            Main.frameManager.changeFrame(new ChatFrame(toRegister.username()));
        });
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(Main.frameManager.getCurrentRawFrame(), message, "Error while registering", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public JPanel getRootPanel() {
        return this.root;
    }
}
