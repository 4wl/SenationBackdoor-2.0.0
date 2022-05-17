package me.earth.phobos.features.modules.render;

import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.RenderUtil;

import java.awt.*;

public
class BreakingESP
        extends Module {
    public final Setting < Float > lineWidth;
    public final Setting < Integer > boxAlpha;
    public Setting < Integer > red;
    public Setting < Integer > green;
    public Setting < Integer > blue;
    public Setting < Boolean > box = this.register ( new Setting <> ( "Box" , true ) );
    public Setting < Boolean > outline = this.register ( new Setting <> ( "Outline" , true ) );

    public
    BreakingESP ( ) {
        super ( "BreakingESP" , "Renders a box on blocks being broken" , Module.Category.RENDER , true , false , false );
        this.red = this.register ( new Setting <> ( "Red" , 125 , 0 , 255 ) );
        this.green = this.register ( new Setting <> ( "Green" , 0 , 0 , 255 ) );
        this.blue = this.register ( new Setting <> ( "Blue" , 255 , 0 , 255 ) );
        this.lineWidth = this.register ( new Setting < Object > ( "LineWidth" , 1.0f , 0.1f , 5.0f , object -> this.outline.getValue ( ) ) );
        this.boxAlpha = this.register ( new Setting < Object > ( "BoxAlpha" , 85 , 0 , 255 , object -> this.box.getValue ( ) ) );
    }

    @Override
    public
    void onRender3D ( Render3DEvent render3DEvent ) {
        if ( BreakingESP.mc.playerController.currentBlock != null ) {
            Color color = new Color ( this.red.getValue ( ) , this.green.getValue ( ) , this.blue.getValue ( ) , this.boxAlpha.getValue ( ) );
            RenderUtil.boxESP ( BreakingESP.mc.playerController.currentBlock , color , this.lineWidth.getValue ( ) , this.outline.getValue ( ) , this.box.getValue ( ) , this.boxAlpha.getValue ( ) , false );
        }
    }
}