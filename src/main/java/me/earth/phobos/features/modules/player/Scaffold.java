package me.earth.phobos.features.modules.player;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.movement.Sprint;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.Timer;
import me.earth.phobos.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public
class Scaffold extends Module {
    private final Setting < ? > mode;
    private final Setting swing;
    private final Setting bSwitch;
    private final Setting center;
    private final Setting keepY;
    private final Setting sprint;
    private final Setting replenishBlocks;
    private final Setting down;
    private final Setting expand;
    private final List < ? extends Block > invalid;
    private final Timer timerMotion;
    private final Timer itemTimer;
    private final Timer timer;
    public Setting rotation;
    private int lastY;
    private BlockPos pos;
    private boolean teleported;

    public
    Scaffold ( ) {
        super ( "Scaffold" , "Places Blocks underneath you." , Category.PLAYER , true , false , false );
        this.mode = this.register ( new Setting <> ( "Mode" , Mode.Fast ) );
        this.rotation = this.register ( new Setting ( "Rotate" , false , ( v ) -> this.mode.getValue ( ) == Mode.Fast ) );
        this.swing = this.register ( new Setting ( "Swing Arm" , false , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.bSwitch = this.register ( new Setting ( "Switch" , false , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.center = this.register ( new Setting ( "Center" , false , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.keepY = this.register ( new Setting ( "KeepYLevel" , false , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.sprint = this.register ( new Setting ( "UseSprint" , true , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.replenishBlocks = this.register ( new Setting ( "ReplenishBlocks" , true , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.down = this.register ( new Setting ( "Down" , false , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.expand = this.register ( new Setting ( "Expand" , 1.0F , 0.0F , 6.0F , ( v ) -> this.mode.getValue ( ) == Mode.Legit ) );
        this.invalid = Arrays.asList ( Blocks.ENCHANTING_TABLE , Blocks.FURNACE , Blocks.CARPET , Blocks.CRAFTING_TABLE , Blocks.TRAPPED_CHEST , Blocks.CHEST , Blocks.DISPENSER , Blocks.AIR , Blocks.WATER , Blocks.LAVA , Blocks.FLOWING_WATER , Blocks.FLOWING_LAVA , Blocks.SNOW_LAYER , Blocks.TORCH , Blocks.ANVIL , Blocks.JUKEBOX , Blocks.STONE_BUTTON , Blocks.WOODEN_BUTTON , Blocks.LEVER , Blocks.NOTEBLOCK , Blocks.STONE_PRESSURE_PLATE , Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE , Blocks.WOODEN_PRESSURE_PLATE , Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE , Blocks.RED_MUSHROOM , Blocks.BROWN_MUSHROOM , Blocks.YELLOW_FLOWER , Blocks.RED_FLOWER , Blocks.ANVIL , Blocks.CACTUS , Blocks.LADDER , Blocks.ENDER_CHEST );
        this.timerMotion = new Timer ( );
        this.itemTimer = new Timer ( );
        this.timer = new Timer ( );
    }

    public static
    void swap ( int slot , int hotbarNum ) {
        mc.playerController.windowClick ( mc.player.inventoryContainer.windowId , slot , 0 , ClickType.PICKUP , mc.player );
        mc.playerController.windowClick ( mc.player.inventoryContainer.windowId , hotbarNum , 0 , ClickType.PICKUP , mc.player );
        mc.playerController.windowClick ( mc.player.inventoryContainer.windowId , slot , 0 , ClickType.PICKUP , mc.player );
        mc.playerController.updateController ( );
    }

    public static
    int getItemSlot ( Container container , Item item ) {
        int slot = 0;

        for (int i = 9; i < 45; ++ i) {
            if ( container.getSlot ( i ).getHasStack ( ) ) {
                ItemStack is = container.getSlot ( i ).getStack ( );
                if ( is.getItem ( ) == item ) {
                    slot = i;
                }
            }
        }

        return slot;
    }

    public static
    boolean isMoving ( EntityLivingBase entity ) {
        return entity.moveForward != 0.0F || entity.moveStrafing != 0.0F;
    }

    public
    void onEnable ( ) {
        this.timer.reset ( );
    }

    @SubscribeEvent
    public
    void onUpdateWalkingPlayerPost ( UpdateWalkingPlayerEvent event ) {
        if ( this.mode.getValue ( ) == Scaffold.Mode.Fast ) {
            if ( this.isOff ( ) || fullNullCheck ( ) || event.getStage ( ) == 0 ) {
                return;
            }

            if ( ! mc.gameSettings.keyBindJump.isKeyDown ( ) ) {
                this.timer.reset ( );
            }

            BlockPos playerBlock;
            if ( BlockUtil.isScaffoldPos ( ( playerBlock = EntityUtil.getPlayerPosWithEntity ( ) ).add ( 0 , - 1 , 0 ) ) ) {
                if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 2 , 0 ) ) ) {
                    this.place ( playerBlock.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( - 1 , - 1 , 0 ) ) ) {
                    this.place ( playerBlock.add ( 0 , - 1 , 0 ) , EnumFacing.EAST );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 1 , - 1 , 0 ) ) ) {
                    this.place ( playerBlock.add ( 0 , - 1 , 0 ) , EnumFacing.WEST );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 1 , - 1 ) ) ) {
                    this.place ( playerBlock.add ( 0 , - 1 , 0 ) , EnumFacing.SOUTH );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 1 , 1 ) ) ) {
                    this.place ( playerBlock.add ( 0 , - 1 , 0 ) , EnumFacing.NORTH );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 1 , - 1 , 1 ) ) ) {
                    if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 1 , 1 ) ) ) {
                        this.place ( playerBlock.add ( 0 , - 1 , 1 ) , EnumFacing.NORTH );
                    }

                    this.place ( playerBlock.add ( 1 , - 1 , 1 ) , EnumFacing.EAST );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( - 1 , - 1 , 1 ) ) ) {
                    if ( BlockUtil.isValidBlock ( playerBlock.add ( - 1 , - 1 , 0 ) ) ) {
                        this.place ( playerBlock.add ( 0 , - 1 , 1 ) , EnumFacing.WEST );
                    }

                    this.place ( playerBlock.add ( - 1 , - 1 , 1 ) , EnumFacing.SOUTH );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 1 , - 1 , 1 ) ) ) {
                    if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 1 , 1 ) ) ) {
                        this.place ( playerBlock.add ( 0 , - 1 , 1 ) , EnumFacing.SOUTH );
                    }

                    this.place ( playerBlock.add ( 1 , - 1 , 1 ) , EnumFacing.WEST );
                } else if ( BlockUtil.isValidBlock ( playerBlock.add ( 1 , - 1 , 1 ) ) ) {
                    if ( BlockUtil.isValidBlock ( playerBlock.add ( 0 , - 1 , 1 ) ) ) {
                        this.place ( playerBlock.add ( 0 , - 1 , 1 ) , EnumFacing.EAST );
                    }

                    this.place ( playerBlock.add ( 1 , - 1 , 1 ) , EnumFacing.NORTH );
                }
            }
        }

    }

    public
    void onUpdate ( ) {
        if ( this.mode.getValue ( ) == Scaffold.Mode.Legit ) {
            if ( Phobos.moduleManager.isModuleEnabled ( "Sprint" ) && ( (Boolean) this.down.getValue ( ) && mc.gameSettings.keyBindSneak.isKeyDown ( ) || ! (Boolean) this.sprint.getValue ( ) ) ) {
                mc.player.setSprinting ( false );
                Sprint.getInstance ( ).disable ( );
            }

            if ( (Boolean) this.replenishBlocks.getValue ( ) && ! ( mc.player.getHeldItem ( EnumHand.MAIN_HAND ).getItem ( ) instanceof ItemBlock ) && this.getBlockCountHotbar ( ) <= 0 && this.itemTimer.passedMs ( 100L ) ) {
                for (int i = 9; i < 45; ++ i) {
                    if ( mc.player.inventoryContainer.getSlot ( i ).getHasStack ( ) ) {
                        ItemStack is = mc.player.inventoryContainer.getSlot ( i ).getStack ( );
                        if ( is.getItem ( ) instanceof ItemBlock && ! this.invalid.contains ( Block.getBlockFromItem ( is.getItem ( ) ) ) && i < 36 ) {
                            swap ( getItemSlot ( mc.player.inventoryContainer , is.getItem ( ) ) , 44 );
                        }
                    }
                }
            }

            if ( (Boolean) this.keepY.getValue ( ) ) {
                if ( ! isMoving ( mc.player ) && mc.gameSettings.keyBindJump.isKeyDown ( ) || mc.player.collidedVertically || mc.player.onGround ) {
                    this.lastY = MathHelper.floor ( mc.player.posY );
                }
            } else {
                this.lastY = MathHelper.floor ( mc.player.posY );
            }

            BlockData blockData = null;
            double x = mc.player.posX;
            double z = mc.player.posZ;
            double y = (Boolean) this.keepY.getValue ( ) ? (double) this.lastY : mc.player.posY;
            double forward = mc.player.movementInput.moveForward;
            double strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if ( ! mc.player.collidedHorizontally ) {
                double[] coords = this.getExpandCoords ( x , z , forward , strafe , yaw );
                x = coords[0];
                z = coords[1];
            }

            if ( this.canPlace ( mc.world.getBlockState ( new BlockPos ( mc.player.posX , mc.player.posY - (double) ( mc.gameSettings.keyBindSneak.isKeyDown ( ) && (Boolean) this.down.getValue ( ) ? 2 : 1 ) , mc.player.posZ ) ).getBlock ( ) ) ) {
                x = mc.player.posX;
                z = mc.player.posZ;
            }

            BlockPos blockBelow = new BlockPos ( x , y - 1.0D , z );
            if ( mc.gameSettings.keyBindSneak.isKeyDown ( ) && (Boolean) this.down.getValue ( ) ) {
                blockBelow = new BlockPos ( x , y - 2.0D , z );
            }

            this.pos = blockBelow;
            if ( mc.world.getBlockState ( blockBelow ).getBlock ( ) == Blocks.AIR ) {
                blockData = this.getBlockData2 ( blockBelow );
            }

            if ( blockData != null ) {
                if ( this.getBlockCountHotbar ( ) <= 0 || ! (Boolean) this.bSwitch.getValue ( ) && ! ( mc.player.getHeldItemMainhand ( ).getItem ( ) instanceof ItemBlock ) ) {
                    return;
                }

                int heldItem = mc.player.inventory.currentItem;
                if ( (Boolean) this.bSwitch.getValue ( ) ) {
                    for (int j = 0; j < 9; ++ j) {
                        mc.player.inventory.getStackInSlot ( j );
                        if ( mc.player.inventory.getStackInSlot ( j ).getCount ( ) != 0 && mc.player.inventory.getStackInSlot ( j ).getItem ( ) instanceof ItemBlock && ! this.invalid.contains ( ( (ItemBlock) mc.player.inventory.getStackInSlot ( j ).getItem ( ) ).getBlock ( ) ) ) {
                            mc.player.inventory.currentItem = j;
                            break;
                        }
                    }
                }

                if ( this.mode.getValue ( ) == Scaffold.Mode.Legit ) {
                    if ( mc.gameSettings.keyBindJump.isKeyDown ( ) && mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F && ! mc.player.isPotionActive ( MobEffects.JUMP_BOOST ) ) {
                        if ( ! this.teleported && (Boolean) this.center.getValue ( ) ) {
                            this.teleported = true;
                            BlockPos pos = new BlockPos ( mc.player.posX , mc.player.posY , mc.player.posZ );
                            mc.player.setPosition ( (double) pos.getX ( ) + 0.5D , pos.getY ( ) , (double) pos.getZ ( ) + 0.5D );
                        }

                        if ( (Boolean) this.center.getValue ( ) && ! this.teleported ) {
                            return;
                        }

                        mc.player.motionY = 0.41999998688697815D;
                        mc.player.motionZ = 0.0D;
                        mc.player.motionX = 0.0D;
                        if ( this.timerMotion.sleep ( 1500L ) ) {
                            mc.player.motionY = - 0.28D;
                        }
                    } else {
                        this.timerMotion.reset ( );
                        if ( this.teleported && (Boolean) this.center.getValue ( ) ) {
                            this.teleported = false;
                        }
                    }
                }

                if ( mc.playerController.processRightClickBlock ( mc.player , mc.world , blockData.position , blockData.face , new Vec3d ( (double) blockData.position.getX ( ) + Math.random ( ) , (double) blockData.position.getY ( ) + Math.random ( ) , (double) blockData.position.getZ ( ) + Math.random ( ) ) , EnumHand.MAIN_HAND ) != EnumActionResult.FAIL ) {
                    if ( (Boolean) this.swing.getValue ( ) ) {
                        mc.player.swingArm ( EnumHand.MAIN_HAND );
                    } else {
                        mc.player.connection.sendPacket ( new CPacketAnimation ( EnumHand.MAIN_HAND ) );
                    }
                }

                mc.player.inventory.currentItem = heldItem;
            }
        }

    }

    public
    double[] getExpandCoords ( double x , double z , double forward , double strafe , float YAW ) {
        BlockPos underPos = new BlockPos ( x , mc.player.posY - (double) ( mc.gameSettings.keyBindSneak.isKeyDown ( ) && (Boolean) this.down.getValue ( ) ? 2 : 1 ) , z );
        Block underBlock = mc.world.getBlockState ( underPos ).getBlock ( );
        double xCalc = - 999.0D;
        double zCalc = - 999.0D;
        double dist = 0.0D;

        for (double expandDist = (Float) this.expand.getValue ( ) * 2.0F; ! this.canPlace ( underBlock ); underBlock = mc.world.getBlockState ( underPos ).getBlock ( )) {
            ++ dist;
            if ( dist > expandDist ) {
                dist = expandDist;
            }

            xCalc = x + ( forward * 0.45D * Math.cos ( Math.toRadians ( YAW + 90.0F ) ) + strafe * 0.45D * Math.sin ( Math.toRadians ( YAW + 90.0F ) ) ) * dist;
            zCalc = z + ( forward * 0.45D * Math.sin ( Math.toRadians ( YAW + 90.0F ) ) - strafe * 0.45D * Math.cos ( Math.toRadians ( YAW + 90.0F ) ) ) * dist;
            if ( dist == expandDist ) {
                break;
            }

            underPos = new BlockPos ( xCalc , mc.player.posY - (double) ( mc.gameSettings.keyBindSneak.isKeyDown ( ) && (Boolean) this.down.getValue ( ) ? 2 : 1 ) , zCalc );
        }

        return new double[]{xCalc , zCalc};
    }

    public
    boolean canPlace ( Block block ) {
        return ( block instanceof BlockAir || block instanceof BlockLiquid ) && mc.world != null && mc.player != null && this.pos != null && mc.world.getEntitiesWithinAABBExcludingEntity ( null , new AxisAlignedBB ( this.pos ) ).isEmpty ( );
    }

    private
    int getBlockCountHotbar ( ) {
        int blockCount = 0;

        for (int i = 36; i < 45; ++ i) {
            if ( mc.player.inventoryContainer.getSlot ( i ).getHasStack ( ) ) {
                ItemStack is = mc.player.inventoryContainer.getSlot ( i ).getStack ( );
                Item item = is.getItem ( );
                if ( is.getItem ( ) instanceof ItemBlock && ! this.invalid.contains ( ( (ItemBlock) item ).getBlock ( ) ) ) {
                    blockCount += is.getCount ( );
                }
            }
        }

        return blockCount;
    }

    private
    Scaffold.BlockData getBlockData2 ( BlockPos pos ) {
        if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
            return new Scaffold.BlockData ( pos.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
        } else {
            BlockPos pos2 = pos.add ( - 1 , 0 , 0 );
            if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                return new Scaffold.BlockData ( pos2.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
            } else {
                BlockPos pos3 = pos.add ( 1 , 0 , 0 );
                if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                    return new Scaffold.BlockData ( pos3.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                } else {
                    BlockPos pos4 = pos.add ( 0 , 0 , 1 );
                    if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                        return new Scaffold.BlockData ( pos4.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                    } else {
                        BlockPos pos5 = pos.add ( 0 , 0 , - 1 );
                        if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                            return new Scaffold.BlockData ( pos5.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                        } else {
                            pos.add ( - 2 , 0 , 0 );
                            if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos2.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                return new Scaffold.BlockData ( pos2.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                            } else {
                                pos.add ( 2 , 0 , 0 );
                                if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos3.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                    return new Scaffold.BlockData ( pos3.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                } else {
                                    pos.add ( 0 , 0 , 2 );
                                    if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos4.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                        return new Scaffold.BlockData ( pos4.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                    } else {
                                        pos.add ( 0 , 0 , - 2 );
                                        if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos5.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                            return new Scaffold.BlockData ( pos5.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                        } else {
                                            BlockPos pos10 = pos.add ( 0 , - 1 , 0 );
                                            if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos10.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                                return new Scaffold.BlockData ( pos10.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                            } else {
                                                BlockPos pos11 = pos10.add ( 1 , 0 , 0 );
                                                if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                                } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos11.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                                    return new Scaffold.BlockData ( pos11.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                                } else {
                                                    BlockPos pos12 = pos10.add ( - 1 , 0 , 0 );
                                                    if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                                    } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos12.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                                        return new Scaffold.BlockData ( pos12.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                                    } else {
                                                        BlockPos pos13 = pos10.add ( 0 , 0 , 1 );
                                                        if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                                        } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos13.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ) {
                                                            return new Scaffold.BlockData ( pos13.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH );
                                                        } else {
                                                            BlockPos pos14 = pos10.add ( 0 , 0 , - 1 );
                                                            if ( ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( 0 , - 1 , 0 ) ).getBlock ( ) ) ) {
                                                                return new Scaffold.BlockData ( pos14.add ( 0 , - 1 , 0 ) , EnumFacing.UP );
                                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( 0 , 1 , 0 ) ).getBlock ( ) ) ) {
                                                                return new Scaffold.BlockData ( pos14.add ( 0 , 1 , 0 ) , EnumFacing.DOWN );
                                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( - 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                                return new Scaffold.BlockData ( pos14.add ( - 1 , 0 , 0 ) , EnumFacing.EAST );
                                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( 1 , 0 , 0 ) ).getBlock ( ) ) ) {
                                                                return new Scaffold.BlockData ( pos14.add ( 1 , 0 , 0 ) , EnumFacing.WEST );
                                                            } else if ( ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( 0 , 0 , 1 ) ).getBlock ( ) ) ) {
                                                                return new Scaffold.BlockData ( pos14.add ( 0 , 0 , 1 ) , EnumFacing.NORTH );
                                                            } else {
                                                                return ! this.invalid.contains ( mc.world.getBlockState ( pos14.add ( 0 , 0 , - 1 ) ).getBlock ( ) ) ? new Scaffold.BlockData ( pos14.add ( 0 , 0 , - 1 ) , EnumFacing.SOUTH ) : null;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public
    void place ( BlockPos posI , EnumFacing face ) {
        BlockPos pos = posI;
        if ( face == EnumFacing.UP ) {
            pos = posI.add ( 0 , - 1 , 0 );
        } else if ( face == EnumFacing.NORTH ) {
            pos = posI.add ( 0 , 0 , 1 );
        } else if ( face == EnumFacing.SOUTH ) {
            pos = posI.add ( 0 , 0 , - 1 );
        } else if ( face == EnumFacing.EAST ) {
            pos = posI.add ( - 1 , 0 , 0 );
        } else if ( face == EnumFacing.WEST ) {
            pos = posI.add ( 1 , 0 , 0 );
        }

        int oldSlot = mc.player.inventory.currentItem;
        int newSlot = - 1;

        for (int i = 0; i < 9; ++ i) {
            ItemStack stack = mc.player.inventory.getStackInSlot ( i );
            if ( ! InventoryUtil.isNull ( stack ) && stack.getItem ( ) instanceof ItemBlock && Block.getBlockFromItem ( stack.getItem ( ) ).getDefaultState ( ).isFullBlock ( ) ) {
                newSlot = i;
                break;
            }
        }

        if ( newSlot != - 1 ) {
            boolean crouched = false;
            if ( ! mc.player.isSneaking ( ) && BlockUtil.blackList.contains ( mc.world.getBlockState ( pos ).getBlock ( ) ) ) {
                mc.player.connection.sendPacket ( new CPacketEntityAction ( mc.player , Action.START_SNEAKING ) );
                crouched = true;
            }

            if ( ! ( mc.player.getHeldItemMainhand ( ).getItem ( ) instanceof ItemBlock ) ) {
                mc.player.connection.sendPacket ( new CPacketHeldItemChange ( newSlot ) );
                mc.player.inventory.currentItem = newSlot;
                mc.playerController.updateController ( );
            }

            if ( mc.gameSettings.keyBindJump.isKeyDown ( ) ) {
                EntityPlayerSP var10000 = mc.player;
                var10000.motionX *= 0.3D;
                var10000 = mc.player;
                var10000.motionZ *= 0.3D;
                mc.player.jump ( );
                if ( this.timer.passedMs ( 1500L ) ) {
                    mc.player.motionY = - 0.28D;
                    this.timer.reset ( );
                }
            }

            if ( (Boolean) this.rotation.getValue ( ) ) {
                float[] angle = MathUtil.calcAngle ( mc.player.getPositionEyes ( mc.getRenderPartialTicks ( ) ) , new Vec3d ( (float) pos.getX ( ) + 0.5F , (float) pos.getY ( ) - 0.5F , (float) pos.getZ ( ) + 0.5F ) );
                mc.player.connection.sendPacket ( new Rotation ( angle[0] , (float) MathHelper.normalizeAngle ( (int) angle[1] , 360 ) , mc.player.onGround ) );
            }

            mc.playerController.processRightClickBlock ( mc.player , mc.world , pos , face , new Vec3d ( 0.5D , 0.5D , 0.5D ) , EnumHand.MAIN_HAND );
            mc.player.swingArm ( EnumHand.MAIN_HAND );
            mc.player.connection.sendPacket ( new CPacketHeldItemChange ( oldSlot ) );
            mc.player.inventory.currentItem = oldSlot;
            mc.playerController.updateController ( );
            if ( crouched ) {
                mc.player.connection.sendPacket ( new CPacketEntityAction ( mc.player , Action.STOP_SNEAKING ) );
            }

        }
    }

    public
    enum Mode {
        Legit,
        Fast
    }

    private static
    class BlockData {
        public BlockPos position;
        public EnumFacing face;

        public
        BlockData ( BlockPos position , EnumFacing face ) {
            this.position = position;
            this.face = face;
        }
    }
}