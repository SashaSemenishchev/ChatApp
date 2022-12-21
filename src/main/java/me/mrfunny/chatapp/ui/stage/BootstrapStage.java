package me.mrfunny.chatapp.ui.stage;

import me.mrfunny.chatapp.ui.stage.results.NewStage;
import me.mrfunny.chatapp.ui.stage.results.ShowFrame;
import me.mrfunny.chatapp.ui.stage.results.StageResult;

public class BootstrapStage {
    public static ShowFrame proceedTillGui(Stage first) {
        Stage current = first;
        while(current.proceed() instanceof NewStage stage) {
            current = stage.stage();
            if(current == null) return null;
        }
        StageResult result = current.proceed();
        if(result instanceof ShowFrame r) {
            return r;
        }
        return null;
    }
}
