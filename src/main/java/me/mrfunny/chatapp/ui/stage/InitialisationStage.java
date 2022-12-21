package me.mrfunny.chatapp.ui.stage;

import me.mrfunny.chatapp.Main;
import me.mrfunny.chatapp.api.data.AccountData;
import me.mrfunny.chatapp.api.data.RequestResult;
import me.mrfunny.chatapp.ui.frames.ChatFrame;
import me.mrfunny.chatapp.ui.frames.WelcomeFrame;
import me.mrfunny.chatapp.ui.stage.results.ShowFrame;
import me.mrfunny.chatapp.ui.stage.results.StageResult;

public class InitialisationStage implements Stage {
    @Override
    public StageResult proceed() {
        AccountData data = Main.client.loadAccountDetails();

        if(data == null) {
            return new ShowFrame(new WelcomeFrame());
        }
        RequestResult result = Main.client.getSocket().login(data).join();
        if(!result.successful()) {
            return new ShowFrame(new WelcomeFrame());
        }
        Main.loginCached(data);
        return new ShowFrame(new ChatFrame(data.username()));
    }
}
