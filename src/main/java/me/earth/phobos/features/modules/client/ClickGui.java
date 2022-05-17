package me.earth.phobos.features.modules.client;

import java.awt.Color;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.event.events.UpdateEvent;
import me.earth.phobos.features.gui.PhobosGui;           // coded at 7:29AM i have school in like 20 minutes
import me.earth.phobos.features.modules.Module;         // Im home rn so im tryna fix this bullshit also NIGHTMARE YOUR FUCKING STUPID
import me.earth.phobos.features.modules.ModuleManifest;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import tcb.bces.listener.Subscribe; 

@ModuleManifest(label="ClickGui", category=Module.Category.CLIENT, key=157)
public class ClickGui
extends Module {
    public final Setting<String> prefix = this.register(new Setting<String>("Prefix", "."));
    public final Setting<Boolean> customFov = this.register(new Setting<Boolean>("Custom Fov", false));
    public final Setting<Float> fov = this.register(new Setting<Float>("Fov", Float.valueOf(150.0f), Float.valueOf(-180.0f), Float.valueOf(180.0f)));
    public final Setting<Integer> red = this.register(new Setting<Integer>("Red", 255, 0, 255));
    public final Setting<Integer> green = this.register(new Setting<Integer>("Green", 255, 0, 255));
    public final Setting<Integer> blue = this.register(new Setting<Integer>("Blue", 255, 0, 255));
    public final Setting<Integer> hoverAlpha = this.register(new Setting<Integer>("Hover Alpha", 60, 0, 255));
    public final Setting<Integer> enabledAlpha = this.register(new Setting<Integer>("Enabled Alpha", 60, 0, 255));
    public final Setting<Integer> categoryRed = this.register(new Setting<Integer>("Category Red", 255, 0, 255));
    public final Setting<Integer> categoryGreen = this.register(new Setting<Integer>("Category Green", 255, 0, 255));
    public final Setting<Integer> categoryBlue = this.register(new Setting<Integer>("Category Blue", 255, 0, 255));
    public final Setting<Integer> categoryAlpha = this.register(new Setting<Integer>("Category Alpha", 40, 0, 255));
    public final Setting<Integer> alpha = this.register(new Setting<Integer>("Alpha", 40, 0, 255));
    private static ClickGui INSTANCE;

    public ClickGui() {
        INSTANCE = this;
    }

    public ClickGui(String name, String description, Category category, boolean hasListener, boolean hidden, boolean alwaysListening) {
        super(name, description, category, hasListener, hidden, alwaysListening);
    }

    public static ClickGui getInstance() {
        return INSTANCE;
    }

    public final int getColor(boolean hover) {
        return new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue(), hover ? this.hoverAlpha.getValue().intValue() : this.enabledAlpha.getValue().intValue()).getRGB();
    }

    @Subscribe
    public void onTick(UpdateEvent event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!(this.mc.currentScreen instanceof PhobosGui)) {
            this.setEnabled(false);
        }
        if (this.customFov.getValue().booleanValue()) {
            this.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, this.fov.getValue().floatValue());
        }
    }

    @Override
    public void onEnable() {
        if (this.mc.player != null) {
            this.mc.displayGuiScreen((GuiScreen)new PhobosGui());
        }
    }

    @Override
    public void onDisable() {
        if (this.mc.currentScreen instanceof PhobosGui) {
            this.mc.displayGuiScreen(null);
        }
    }
}
