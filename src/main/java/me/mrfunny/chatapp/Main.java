package me.mrfunny.chatapp;

import io.socket.client.IO;
import io.socket.client.Manager;
import me.mrfunny.chatapp.api.MessengerClient;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.ui.Frame;
import me.mrfunny.chatapp.ui.FrameManager;
import me.mrfunny.chatapp.ui.frames.ChatFrame;
import me.mrfunny.chatapp.ui.stage.InitialisationStage;
import me.mrfunny.chatapp.ui.stage.BootstrapStage;
import me.mrfunny.chatapp.ui.stage.Stage;
import me.mrfunny.chatapp.ui.stage.results.ShowFrame;
import me.mrfunny.kssdb.KeyStoredStringDatabase;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static MessengerClient client;
    public static Stage currentStage;
    public static KeyStoredStringDatabase cache;
    public static FrameManager frameManager;
    private static AccountData loggedAs;
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static File cacheFolder;
    public static void main(String[] args) throws Exception {
        String host = JOptionPane.showInputDialog(null, "Enter host to connect", "http://localhost:9904");
        if(host == null) return;
        client = new MessengerClient(host);
        client.connect();
        JFrame loading = new JFrame();
        loading.add(new JLabel("Connecting"));
        loading.setSize(200, 200);
        loading.setVisible(true);
        client.getSocket().getIO().on("connect", event -> System.out.println("Connected to the app"));

        cacheFolder = new File(client.getStorage().getStorageFolder(), "cached-messages");
        cache = KeyStoredStringDatabase.newClient(cacheFolder);

        currentStage = new InitialisationStage();
        loading.setVisible(false);
        ShowFrame stageToShow = BootstrapStage.proceedTillGui(currentStage);
        if(stageToShow == null) {
            System.err.println("No stage to show");
            return;
        }
        frameManager = new FrameManager(stageToShow.component());
        client.setMessageReceiver(message -> {
            try {
                cache.set(String.valueOf(message.id()), message.content());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(frameManager.getCurrentFrame() instanceof ChatFrame chat) {
                chat.addText(message.content());
            }
        });
    }

    public static void regenerateCache() throws IOException {
        Files.walk(cacheFolder.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        cache = KeyStoredStringDatabase.newClient(cacheFolder);
    }

    public static void login(AccountData data) {
        loggedAs = data;
        try {
            client.saveAccountDetails(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loginCached(AccountData data) {
        loggedAs = data;
    }

    public static AccountData getLoggedAs() {
        return loggedAs;
    }
}