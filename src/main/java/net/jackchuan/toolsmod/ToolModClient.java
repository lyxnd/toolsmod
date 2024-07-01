package net.jackchuan.toolsmod;

import net.fabricmc.api.ClientModInitializer;
import net.jackchuan.toolsmod.event.KeyInputHandler;

public class ToolModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyInputHandler.registerScreenKeyInputs();
    }
}
