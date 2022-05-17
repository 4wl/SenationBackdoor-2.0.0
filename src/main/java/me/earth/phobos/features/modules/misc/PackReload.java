package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class PackReload
        extends Module {

    public
    PackReload ( ) {
        super ( "PackReload" , "efficiency" , Category.MISC , false , false , false );
    }

    @SubscribeEvent
    public
    void onTick ( ) {
        PackReload.mc.refreshResources ( );
        this.disable ( );
    }
}