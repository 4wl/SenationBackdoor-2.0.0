package me.earth.phobos.features.modules.movement;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

public class TickShift extends Module {

    public TickShift() {
        super("TickShift", "shift tick", Category.MISC, true, false, false);
    }

    public final Setting<Float> multiplier = this.register(new Setting("Multiplier", 1.5f, 0.0f, 10.0f));
    public final Setting<Integer> disableTicks = this.register(new Setting("Disable Ticks", 10, 0, 100));
    public int ticks;

    @Override
    public void onUpdate() {
        ticks++;
        if (ticks < disableTicks.getValue()) {
            mc.timer.tickLength = 50.0f / multiplier.getValue();
        } else if (ticks > disableTicks.getValue()) {
            mc.timer.tickLength = 50.0f;
            ticks = 0;
            this.disable();
        }
    }

}
