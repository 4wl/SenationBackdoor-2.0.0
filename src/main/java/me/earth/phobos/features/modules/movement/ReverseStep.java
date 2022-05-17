package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;

import java.util.Objects;

public
class ReverseStep
        extends Module {
    private final Setting < Mode > mode = this.register ( new Setting <> ( "Mode" , Mode.PPHOBOS ) );
    int delay;

    public
    ReverseStep ( ) {
        super ( "ReverseStep" , "Makes you better." , Category.MOVEMENT , true , false , false );
    }

    @Override
    public
    void onUpdate ( ) {
        if ( fullNullCheck ( ) ) return;
        if ( this.mode.getValue ( ) == Mode.REWRITE ) {
            if ( ReverseStep.mc.player.motionY > (double) - 0.06f ) {
                this.delay = 20;
            }
            if ( ReverseStep.mc.player.fallDistance > 0.0f && ReverseStep.mc.player.fallDistance < 1.0f && this.delay == 0 && ! Phobos.moduleManager.isModuleEnabled ( "Strafe" ) && ! mc.player.isInWater ( ) ) {
                ReverseStep.mc.player.motionY = - 3.9200038147008747;
            }
        }
        if ( fullNullCheck ( ) ) return;
        if ( this.mode.getValue ( ) == Mode.PPHOBOS ) {
            if ( mc.player != null && mc.world != null && mc.player.onGround && ! mc.player.isSneaking ( ) && ! mc.player.isInWater ( ) && ! mc.player.isDead && ! mc.player.isInLava ( ) && ! mc.player.isOnLadder ( ) && ! mc.player.noClip && ! mc.gameSettings.keyBindSneak.isKeyDown ( ) && ! mc.gameSettings.keyBindJump.isKeyDown ( ) && ! Phobos.moduleManager.isModuleEnabled ( "Strafe" ) ) {
                if ( Objects.requireNonNull ( ReverseStep.mc.player ).onGround ) {
                    ReverseStep.mc.player.motionY -= 1.0;
                }
            }
        }
    }

    private
    enum Mode {
        PPHOBOS,
        REWRITE

    }
}