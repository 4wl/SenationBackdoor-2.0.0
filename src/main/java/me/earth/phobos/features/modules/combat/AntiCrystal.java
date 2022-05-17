package me.earth.phobos.features.modules.combat;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public
class AntiCrystal
        extends Module {
    private final List < BlockPos > targets = new ArrayList <> ( );
    private final Timer timer = new Timer ( );
    private final Timer breakTimer = new Timer ( );
    private final Timer checkTimer = new Timer ( );
    public Setting < Float > range = this.register ( new Setting <> ( "Range" , 6.0f , 0.0f , 10.0f ) );
    public Setting < Float > wallsRange = this.register ( new Setting <> ( "WallsRange" , 3.5f , 0.0f , 10.0f ) );
    public Setting < Float > minDmg = this.register ( new Setting <> ( "MinDmg" , 6.0f , 0.0f , 40.0f ) );
    public Setting < Float > selfDmg = this.register ( new Setting <> ( "SelfDmg" , 2.0f , 0.0f , 10.0f ) );
    public Setting < Integer > placeDelay = this.register ( new Setting <> ( "PlaceDelay" , 0 , 0 , 500 ) );
    public Setting < Integer > breakDelay = this.register ( new Setting <> ( "BreakDelay" , 0 , 0 , 500 ) );
    public Setting < Integer > checkDelay = this.register ( new Setting <> ( "CheckDelay" , 0 , 0 , 500 ) );
    public Setting < Integer > wasteAmount = this.register ( new Setting <> ( "WasteAmount" , 1 , 1 , 5 ) );
    public Setting < Switch > switcher = this.register ( new Setting <> ( "Switch" , Switch.NONE ) );
    public Setting < Updates > mode = this.register ( new Setting <> ( "Updates" , Updates.TICK ) );
    public Setting < Boolean > rotate = this.register ( new Setting <> ( "Rotate" , true ) );
    public Setting < Boolean > packet = this.register ( new Setting <> ( "Packet" , true ) );
    public Setting < Boolean > instant = this.register ( new Setting <> ( "Predict" , false ) );
    public Setting < Boolean > resetBreakTimer = this.register ( new Setting <> ( "ResetBreak" , true ) );
    public Setting < Integer > rotations = this.register ( new Setting <> ( "Spoofs" , 1 , 1 , 20 ) );
    private float yaw;
    private float pitch;
    private boolean rotating;
    private int rotationPacketsSpoofed;
    private Entity breakTarget;

    public
    AntiCrystal ( ) {
        super ( "AntiCrystal" , "Hacker shit" , Module.Category.COMBAT , true , false , false );
    }

    @Override
    public
    void onToggle ( ) {
        this.rotating = false;
    }

    private
    Entity getDeadlyCrystal ( ) {
        Entity bestcrystal = null;
        float highestDamage = 0.0f;
        for (Entity crystal : AntiCrystal.mc.world.loadedEntityList) {
            float damage;
            if ( ! ( crystal instanceof EntityEnderCrystal ) || AntiCrystal.mc.player.getDistanceSq ( crystal ) > 169.0 || ( damage = DamageUtil.calculateDamage ( crystal , AntiCrystal.mc.player ) ) < this.minDmg.getValue ( ) )
                continue;
            if ( bestcrystal == null ) {
                bestcrystal = crystal;
                highestDamage = damage;
                continue;
            }
            if ( ! ( damage > highestDamage ) ) continue;
            bestcrystal = crystal;
            highestDamage = damage;
        }
        return bestcrystal;
    }

    private
    int getSafetyCrystals ( Entity deadlyCrystal ) {
        int count = 0;
        for (Entity entity : AntiCrystal.mc.world.loadedEntityList) {
            if ( entity instanceof EntityEnderCrystal || DamageUtil.calculateDamage ( entity , AntiCrystal.mc.player ) > 2.0f || deadlyCrystal.getDistanceSq ( entity ) > 144.0 )
                continue;
            ++ count;
        }
        return count;
    }

    private
    BlockPos getPlaceTarget ( Entity deadlyCrystal ) {
        BlockPos closestPos = null;
        float smallestDamage = 10.0f;
        for (BlockPos pos : BlockUtil.possiblePlacePositions ( this.range.getValue ( ) )) {
            float damage = DamageUtil.calculateDamage ( pos , AntiCrystal.mc.player );
            if ( damage > 2.0f || deadlyCrystal.getDistanceSq ( pos ) > 144.0 || AntiCrystal.mc.player.getDistanceSq ( pos ) >= MathUtil.square ( this.wallsRange.getValue ( ) ) && BlockUtil.rayTracePlaceCheck ( pos , true , 1.0f ) )
                continue;
            if ( closestPos == null ) {
                smallestDamage = damage;
                closestPos = pos;
                continue;
            }
            if ( ! ( damage < smallestDamage ) && ( damage != smallestDamage || ! ( AntiCrystal.mc.player.getDistanceSq ( pos ) < AntiCrystal.mc.player.getDistanceSq ( closestPos ) ) ) )
                continue;
            smallestDamage = damage;
            closestPos = pos;
        }
        return closestPos;
    }

    @SubscribeEvent
    public
    void onPacketSend ( PacketEvent.Send event ) {
        if ( event.getStage ( ) == 0 && this.rotate.getValue ( ) && this.rotating ) {
            if ( event.getPacket ( ) instanceof CPacketPlayer ) {
                CPacketPlayer packet = event.getPacket ( );
                packet.yaw = this.yaw;
                packet.pitch = this.pitch;
            }
            ++ this.rotationPacketsSpoofed;
            if ( this.rotationPacketsSpoofed >= this.rotations.getValue ( ) ) {
                this.rotating = false;
                this.rotationPacketsSpoofed = 0;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public
    void onPacketReceive ( PacketEvent.Receive event ) {
        if ( AntiCrystal.fullNullCheck ( ) )
            return;
        if ( event.getPacket ( ) instanceof SPacketSpawnObject && this.instant.getValue ( ) ) {
            SPacketSpawnObject packet2 = event.getPacket ( );
            BlockPos pos = new BlockPos ( packet2.getX ( ) , packet2.getY ( ) , packet2.getZ ( ) );
            if ( packet2.getType ( ) == 51 ) {
                if ( targets.contains ( pos.down ( ) ) && AutoCrystal.mc.player.getDistanceSq ( pos ) <= MathUtil.square ( this.range.getValue ( ) ) ) {
                    this.attackCrystalPredict ( packet2.getEntityID ( ) , pos );
                    this.targets.clear ( );
                } /* else if ( DamageUtil.calculateDamage ( pos.down ( ) , AntiCrystal.mc.player ) > this.minDmg.getValue ( ) ) { // bad!
                    BlockPos placeTarget = this.getPlaceTarget ( pos );
                    if ( placeTarget != null ) {
                        boolean offhand = AntiCrystal.mc.player.getHeldItemOffhand ( ).getItem ( ) == Items.END_CRYSTAL;
                        BlockUtil.placeCrystalOnBlock ( placeTarget , offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , true , true , this.switcher.getValue ( ) == Switch.SILENT );
                        this.targets.add ( placeTarget );
                    }
                } */
            }
        }
    }

    @Override
    public
    void onTick ( ) {
        if ( this.mode.getValue ( ) == Updates.TICK )
            this.doAntiCrystal ( );
    }

    @Override
    public
    void onUpdate ( ) {
        if ( this.mode.getValue ( ) == Updates.UPDATE )
            this.doAntiCrystal ( );
    }

    @SubscribeEvent
    public
    void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( this.mode.getValue ( ) == Updates.WALKING )
            this.doAntiCrystal ( );
    }

    private
    void doAntiCrystal ( ) {
        if ( ! AntiCrystal.fullNullCheck ( ) && this.checkTimer.passedMs ( this.checkDelay.getValue ( ) ) ) {
            Entity deadlyCrystal = this.getDeadlyCrystal ( );
            if ( deadlyCrystal != null ) {
                if ( this.getSafetyCrystals ( deadlyCrystal ) < this.wasteAmount.getValue ( ) ) {
                    BlockPos placeTarget = this.getPlaceTarget ( deadlyCrystal );
                    if ( placeTarget != null ) {
                        this.targets.add ( placeTarget );
                    }
                    this.placeCrystal ( deadlyCrystal );
                }
                this.breakTarget = this.getBreakTarget ( deadlyCrystal );
                this.breakCrystal ( );
            }
            this.checkTimer.reset ( );
        }
    }

    public
    Entity getBreakTarget ( Entity deadlyCrystal ) {
        Entity smallestCrystal = null;
        float smallestDamage = 10.0f;
        for (Entity entity : AntiCrystal.mc.world.loadedEntityList) {
            float damage;
            if ( ! ( entity instanceof EntityEnderCrystal ) || ( damage = DamageUtil.calculateDamage ( entity , AntiCrystal.mc.player ) ) > this.selfDmg.getValue ( ) || entity.getDistanceSq ( deadlyCrystal ) > 144.0 || AntiCrystal.mc.player.getDistanceSq ( entity ) > MathUtil.square ( this.wallsRange.getValue ( ) ) && EntityUtil.rayTraceHitCheck ( entity , true ) )
                continue;
            if ( smallestCrystal == null ) {
                smallestCrystal = entity;
                smallestDamage = damage;
                continue;
            }
            if ( ! ( damage < smallestDamage ) && ( smallestDamage != damage || ! ( AntiCrystal.mc.player.getDistanceSq ( entity ) < AntiCrystal.mc.player.getDistanceSq ( smallestCrystal ) ) ) )
                continue;
            smallestCrystal = entity;
            smallestDamage = damage;
        }
        return smallestCrystal;
    }

    private
    void placeCrystal ( Entity deadlyCrystal ) {
        boolean offhand;
        offhand = AntiCrystal.mc.player.getHeldItemOffhand ( ).getItem ( ) == Items.END_CRYSTAL;
        if ( this.timer.passedMs ( this.placeDelay.getValue ( ) ) && ( this.switcher.getValue ( ) == Switch.NORMAL || this.switcher.getValue ( ) == Switch.SILENT || AntiCrystal.mc.player.getHeldItemMainhand ( ).getItem ( ) == Items.END_CRYSTAL || offhand ) && ! this.targets.isEmpty ( ) && this.getSafetyCrystals ( deadlyCrystal ) <= this.wasteAmount.getValue ( ) ) {
            if ( this.switcher.getValue ( ) == Switch.NORMAL && AntiCrystal.mc.player.getHeldItemMainhand ( ).getItem ( ) != Items.END_CRYSTAL && ! offhand ) {
                this.doSwitch ( );
            }
            if ( ! this.targets.isEmpty ( ) ) {
                this.rotateToPos ( this.targets.get ( this.targets.size ( ) - 1 ) );
                BlockUtil.placeCrystalOnBlock ( this.targets.get ( this.targets.size ( ) - 1 ) , offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , true , true , this.switcher.getValue ( ) == Switch.SILENT );
            }
            this.timer.reset ( );
        }
    }

    private
    void doSwitch ( ) {
        int crystalSlot;
        crystalSlot = AntiCrystal.mc.player.getHeldItemMainhand ( ).getItem ( ) == Items.END_CRYSTAL ? AntiCrystal.mc.player.inventory.currentItem : - 1;
        if ( crystalSlot == - 1 ) {
            for (int l = 0; l < 9; ++ l) {
                if ( AntiCrystal.mc.player.inventory.getStackInSlot ( l ).getItem ( ) != Items.END_CRYSTAL ) continue;
                crystalSlot = l;
                break;
            }
        }
        if ( crystalSlot != - 1 ) {
            AntiCrystal.mc.player.inventory.currentItem = crystalSlot;
        }
    }

    private
    void breakCrystal ( ) {
        if ( this.breakTimer.passedMs ( this.breakDelay.getValue ( ) ) && this.breakTarget != null && DamageUtil.canBreakWeakness ( AntiCrystal.mc.player ) ) {
            this.rotateTo ( this.breakTarget );
            EntityUtil.attackEntity ( this.breakTarget , this.packet.getValue ( ) , true );
            this.breakTimer.reset ( );
            this.targets.clear ( );
        }
    }

    private
    void attackCrystalPredict ( int entityID , BlockPos pos ) {
        // TODO: 8/2/2021 rotate if people actually want it
        CPacketUseEntity attackPacket = new CPacketUseEntity ( );
        attackPacket.entityId = entityID;
        attackPacket.action = CPacketUseEntity.Action.ATTACK;
        AutoCrystal.mc.player.connection.sendPacket ( attackPacket );
        AutoCrystal.mc.player.connection.sendPacket ( new CPacketAnimation ( EnumHand.MAIN_HAND ) );
        if ( this.resetBreakTimer.getValue ( ) ) {
            this.breakTimer.reset ( );
        }
    }

    private
    void rotateTo ( Entity entity ) {
        if ( this.rotate.getValue ( ) ) {
            float[] angle = MathUtil.calcAngle ( AntiCrystal.mc.player.getPositionEyes ( mc.getRenderPartialTicks ( ) ) , entity.getPositionVector ( ) );
            this.yaw = angle[0];
            this.pitch = angle[1];
            this.rotating = true;
        }
    }

    private
    void rotateToPos ( BlockPos pos ) {
        if ( this.rotate.getValue ( ) ) {
            float[] angle = MathUtil.calcAngle ( AntiCrystal.mc.player.getPositionEyes ( mc.getRenderPartialTicks ( ) ) , new Vec3d ( (float) pos.getX ( ) + 0.5f , (float) pos.getY ( ) - 0.5f , (float) pos.getZ ( ) + 0.5f ) );
            this.yaw = angle[0];
            this.pitch = angle[1];
            this.rotating = true;
        }
    }

    public
    enum Switch {
        NONE,
        SILENT,
        NORMAL
    }

    public
    enum Updates {
        TICK,
        UPDATE,
        WALKING
    }
}

