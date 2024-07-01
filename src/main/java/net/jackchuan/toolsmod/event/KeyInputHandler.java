package net.jackchuan.toolsmod.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.jackchuan.toolsmod.screen.ChainBreakScreen;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_DRAGONPRO = "key.category.toolmod.keys";

    public static final String KEY_OPENCONFIG = "key.toolmod.openconfigscreen";

    public static KeyBinding configScreen;
    public static void registerScreenKeyInputs(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(configScreen.wasPressed()){
                client.setScreen(new ChainBreakScreen(client.currentScreen));
            }
        });
    }

    public static void registerKey() {
        configScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_OPENCONFIG,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KEY_CATEGORY_DRAGONPRO
        ));
    }
}
