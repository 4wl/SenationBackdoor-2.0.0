package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.client.Management;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public
class ChatModifier
        extends Module {
    private static ChatModifier INSTANCE = new ChatModifier ( );
    private final Timer timer = new Timer ( );
    public Setting < Suffix > suffix = this.register ( new Setting <> ( "Suffix" , Suffix.NONE , "Your Suffix." ) );
    public Setting < String > customSuffix = this.register ( new Setting <> ( "" , " | SenationBackdoor owns all." , v -> this.suffix.getValue ( ) == Suffix.CUSTOM ) );
    public Setting < Boolean > clean = this.register ( new Setting <> ( "CleanChat" , Boolean.FALSE , "Cleans your chat" ) );
    public Setting < Boolean > infinite = this.register ( new Setting <> ( "Infinite" , Boolean.FALSE , "Makes your chat infinite." ) );
    public Setting < Boolean > autoQMain = this.register ( new Setting <> ( "AutoQMain" , Boolean.FALSE , "Spams AutoQMain" ) );
    public Setting < Boolean > qNotification = this.register ( new Setting < Object > ( "QNotification" , Boolean.FALSE , v -> this.autoQMain.getValue ( ) ) );
    public Setting < Integer > qDelay = this.register ( new Setting < Object > ( "QDelay" , 9 , 1 , 90 , v -> this.autoQMain.getValue ( ) ) );
    public Setting < TextUtil.Color > timeStamps = this.register ( new Setting <> ( "Time" , TextUtil.Color.NONE ) );
    public Setting < Boolean > rainbowTimeStamps = this.register ( new Setting < Object > ( "RainbowTimeStamps" , Boolean.FALSE , v -> this.timeStamps.getValue ( ) != TextUtil.Color.NONE ) );
    public Setting < TextUtil.Color > bracket = this.register ( new Setting < Object > ( "Bracket" , TextUtil.Color.WHITE , v -> this.timeStamps.getValue ( ) != TextUtil.Color.NONE ) );
    public Setting < Boolean > space = this.register ( new Setting < Object > ( "Space" , Boolean.TRUE , v -> this.timeStamps.getValue ( ) != TextUtil.Color.NONE ) );
    public Setting < Boolean > all = this.register ( new Setting < Object > ( "All" , Boolean.FALSE , v -> this.timeStamps.getValue ( ) != TextUtil.Color.NONE ) );
    public Setting < Boolean > shrug = this.register ( new Setting <> ( "Shrug" , false ) );
    public Setting < Boolean > disability = this.register ( new Setting <> ( "Disability" , false ) );

    public
    ChatModifier ( ) {
        super ( "Chat" , "Modifies your chat" , Module.Category.MISC , true , false , false );
        this.setInstance ( );
    }

    public static
    ChatModifier getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new ChatModifier ( );
        }
        return INSTANCE;
    }

    private
    void setInstance ( ) {
        INSTANCE = this;
    }

    @Override
    public
    void onUpdate ( ) {
        if ( this.shrug.getValue ( ) ) {
            ChatModifier.mc.player.sendChatMessage ( TextUtil.shrug );
            this.shrug.setValue ( false );
        }
        if ( this.disability.getValue ( ) ) {
            ChatModifier.mc.player.sendChatMessage ( TextUtil.disability );
            this.disability.setValue ( false );
        }
        if ( this.autoQMain.getValue ( ) ) {
            if ( ! this.shouldSendMessage ( ChatModifier.mc.player ) ) {
                return;
            }
            if ( this.qNotification.getValue ( ) ) {
                Command.sendMessage ( "<AutoQueueMain> Sending message: /queue main" );
            }
            ChatModifier.mc.player.sendChatMessage ( "/queue main" );
            this.timer.reset ( );
        }
    }

    @SubscribeEvent
    public
    void onPacketSend ( PacketEvent.Send event ) {
        if ( event.getStage ( ) == 0 && event.getPacket ( ) instanceof CPacketChatMessage ) {
            CPacketChatMessage packet = event.getPacket ( );
            String s = packet.getMessage ( );
            if ( s.startsWith ( "/" ) ) {
                return;
            }
            switch (this.suffix.getValue ( )) {
                case SENATI: {
                    s = s + " \u23d0 \ua731\u1d07\u0274\u1d00\u1d1b\u026a\u1d0f\u0274\u0299\u1d00\u1d04\u1d0b\u1d05\u1d0f\u1d0f\u0280";
                    break;
                }
           
                case NIGHTM: {
                    s = s + " \u23d0 \u0274\u026a\u0262\u029c\u1d1b\u1d0d\u1d00\u0280\u1d07\u002e\u1d04\u1d04";
                    break;
                }
                case INSANE: {
                    s = s + " | \u028C\u0433\u1D07\u0455+ \u00AB \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u1D0F\u0274 \u1D1B\u1D0F\u1D18 \u00BB \u00BB \u02E2\u207F\u1D52\u02B7\u23D0 \u041D\u03B5\u13AE\u043D\u15E9\u03B5\u0455\u01AD\u03C5\u0455 \u00BB \u0299\u1D00\u1D04\u1D0B\u1D05\u1D0F\u1D0F\u0280\u1D07\u1D05 | \u1D0D\u1D07\u1D0F\u1D21 \u00BB \u1D1C\u0274\u026A\u1D04\u1D0F\u0280\u0274\u0262\u1D0F\u1D05.\u0262\u0262  \uA731\u1D07\u1D18\u1D18\u1D1C\u1D0B\u1D1C | \u029C\u1D1C\u1D22\u1D1C\u0274\u026A\u0262\u0280\u1D07\u1D07\u0274.\u0262\u0262tm \u00BB \u0299\u1D00\u1D04\u1D0B\u1D04\u029F\u026A\u1D07\u0274\u1D1Btm \u00BB \u0274\u1D0F\u1D1C \u029F\u1D07\u1D00\u1D0B \u23D0 \u0493\u1D0F\u0280\u0262\u1D07\u0280\u1D00\u1D1B \u2661 | \u04E8B\u039BM\u039B \u1103\u1102I\u03A3\u041F\u01AC - \u1D07\u029F\u1D07\u1D0D\u1D07\u0274\u1D1B\u1D00\u0280\uA731.\u1D04\u1D0F\u1D0D \u300B\u1D0F\uA731\u026A\u0280\u026A\uA731 | W\u00D4\u00D4K\u00CF\u00CA \u00C7L\u00EE\u00EB\u00D1Ttm {\u0280\u1D00\u026A\u1D0F\u0274\u1D0B\u1D07\u1D0B} \u30C3 \uFF32\uFF10\uFF10\uFF34\uFF5C \u1D20\u1D0F\u026A\u1D05 \u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D0B\u1D00\u1D0D\u026A \uFF5C \u02B3\u1D1C\u02E2\u02B0\u1D07\u02B3\u02B0\u1D00\u1D9C\u1D4F \uFF5C \u24CC\u24CC\u24BA \uFF5C \uFF49\uFF4D\uFF50\uFF41\uFF43\uFF54\uFF5C \uA730\u1D1C\u1D1B\u1D1C\u0280\u1D07tm \u24D6\u24DE\u24DB\u24D3 \uFF5C \u5342\u4E02\u3129\u51E0\u5342\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C\uA118\uA182\uA493\uA04D\uA35F\uA45B\uA2EB\uA1D3 \uFF5C \u02B3\u1D00\uFF54\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D00\u1D18\u1D0F\u029F\u029F\u028F\u1D0F\u0274 \uFF5C \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u2758 \u1D3E\u1D3C\u1D3E\u1D2E\u1D3C\u1D2E \u1D9C\u1D38\u1D35\u1D31\u1D3A\u1D40 \u23D0 \u0262\u1D00\u028F \u23D0  c l i e n t |  B a L l C l i E n T\u00BB \u028C\u0433\u1D07\u0455+ \u00AB \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u1D0F\u0274 \u1D1B\u1D0F\u1D18 \u00BB \u00BB \u02E2\u207F\u1D52\u02B7\u23D0 \u041D\u03B5\u13AE\u043D\u15E9\u03B5\u0455\u01AD\u03C5\u0455 \u00BB \u0299\u1D00\u1D04\u1D0B\u1D05\u1D0F\u1D0F\u0280\u1D07\u1D05 | \u1D0D\u1D07\u1D0F\u1D21 \u00BB \u1D1C\u0274\u026A\u1D04\u1D0F\u0280\u0274\u0262\u1D0F\u1D05.\u0262\u0262  \uA731\u1D07\u1D18\u1D18\u1D1C\u1D0B\u1D1C | \u029C\u1D1C\u1D22\u1D1C\u0274\u026A\u0262\u0280\u1D07\u1D07\u0274.\u0262\u0262tm \u00BB \u0299\u1D00\u1D04\u1D0B\u1D04\u029F\u026A\u1D07\u0274\u1D1Btm \u00BB \u0274\u1D0F\u1D1C \u029F\u1D07\u1D00\u1D0B  \u23D0 \u0493\u1D0F\u0280\u0262\u1D07\u0280\u1D00\u1D1B \u2661 | \u04E8B\u039BM\u039B \u1103\u1102I\u03A3\u041F\u01AC - \u1D07\u029F\u1D07\u1D0D\u1D07\u0274\u1D1B\u1D00\u0280\uA731.\u1D04\u1D0F\u1D0D \u300B\u1D0F\uA731\u026A\u0280\u026A\uA731 | W\u00D4\u00D4K\u00CF\u00CA \u00C7L\u00EE\u00EB\u00D1Ttm {\u0280\u1D00\u026A\u1D0F\u0274\u1D0B\u1D07\u1D0B} \u30C3 \uFF32\uFF10\uFF10\uFF34\uFF5C \u1D20\u1D0F\u026A\u1D05 \u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D0B\u1D00\u1D0D\u026A \uFF5C \u02B3\u1D1C\u02E2\u02B0\u1D07\u02B3\u02B0\u1D00\u1D9C\u1D4F \uFF5C \u24CC\u24CC\u24BA \uFF5C \uFF49\uFF4D\uFF50\uFF41\uFF43\uFF54\uFF5C \uA730\u1D1C\u1D1B\u1D1C\u0280\u1D07tm \u24D6\u24DE\u24DB\u24D3 \uFF5C \u5342\u4E02\u3129\u51E0\u5342\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C\uA118\uA182\uA493\uA04D\uA35F\uA45B\uA2EB\uA1D3 \uFF5C \u02B3\u1D00\uFF54\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D00\u1D18\u1D0F\u029F\u029F\u028F\u1D0F\u0274 \uFF5C \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u2758 \u1D3E\u1D3C\u1D3E\u1D2E\u1D3C\u1D2E \u1D9C\u1D38\u1D35\u1D31\u1D3A\u1D40 \u23D0 \u0262\u1D00\u028F \u23D0  c l i e n t |  B a L l C l i E n T\u00BB \u028C\u0433\u1D07\u0455+ \u00AB \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u1D0F\u0274 \u1D1B\u1D0F\u1D18 \u00BB \u00BB \u02E2\u207F\u1D52\u02B7\u23D0 \u041D\u03B5\u13AE\u043D\u15E9\u03B5\u0455\u01AD\u03C5\u0455 \u00BB \u0299\u1D00\u1D04\u1D0B\u1D05\u1D0F\u1D0F\u0280\u1D07\u1D05 | \u1D0D\u1D07\u1D0F\u1D21 \u00BB \u1D1C\u0274\u026A\u1D04\u1D0F\u0280\u0274\u0262\u1D0F\u1D05.\u0262\u0262  \uA731\u1D07\u1D18\u1D18\u1D1C\u1D0B\u1D1C | \u029C\u1D1C\u1D22\u1D1C\u0274\u026A\u0262\u0280\u1D07\u1D07\u0274.\u0262\u0262tm \u00BB \u0299\u1D00\u1D04\u1D0B\u1D04\u029F\u026A\u1D07\u0274\u1D1Btm \u00BB \u0274\u1D0F\u1D1C \u029F\u1D07\u1D00\u1D0B  \u23D0 \u0493\u1D0F\u0280\u0262\u1D07\u0280\u1D00\u1D1B \u2661 | \u04E8B\u039BM\u039B \u1103\u1102I\u03A3\u041F\u01AC - \u1D07\u029F\u1D07\u1D0D\u1D07\u0274\u1D1B\u1D00\u0280\uA731.\u1D04\u1D0F\u1D0D \u300B\u1D0F\uA731\u026A\u0280\u026A\uA731 | W\u00D4\u00D4K\u00CF\u00CA \u00C7L\u00EE\u00EB\u00D1Ttm {\u0280\u1D00\u026A\u1D0F\u0274\u1D0B\u1D07\u1D0B} \u30C3 \uFF32\uFF10\uFF10\uFF34\uFF5C \u1D20\u1D0F\u026A\u1D05 \u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D0B\u1D00\u1D0D\u026A \uFF5C \u02B3\u1D1C\u02E2\u02B0\u1D07\u02B3\u02B0\u1D00\u1D9C\u1D4F \uFF5C \u24CC\u24CC\u24BA \uFF5C \uFF49\uFF4D\uFF50\uFF41\uFF43\uFF54\uFF5C \uA730\u1D1C\u1D1B\u1D1C\u0280\u1D07tm \u24D6\u24DE\u24DB\u24D3 \uFF5C \u5342\u4E02\u3129\u51E0\u5342\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C\uA118\uA182\uA493\uA04D\uA35F\uA45B\uA2EB\uA1D3 \uFF5C \u02B3\u1D00\uFF54\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D00\u1D18\u1D0F\u029F\u029F\u028F\u1D0F\u0274 \uFF5C \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u2758 \u1D3E\u1D3C\u1D3E\u1D2E\u1D3C\u1D2E \u1D9C\u1D38\u1D35\u1D31\u1D3A\u1D40 \u23D0 \u0262\u1D00\u028F \u23D0  c l i e n t |  B a L l C l i E n T\u00BB \u028C\u0433\u1D07\u0455+ \u00AB \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u1D0F\u0274 \u1D1B\u1D0F\u1D18 \u00BB \u00BB \u02E2\u207F\u1D52\u02B7\u23D0 \u041D\u03B5\u13AE\u043D\u15E9\u03B5\u0455\u01AD\u03C5\u0455 \u00BB \u0299\u1D00\u1D04\u1D0B\u1D05\u1D0F\u1D0F\u0280\u1D07\u1D05 | \u1D0D\u1D07\u1D0F\u1D21 \u00BB \u1D1C\u0274\u026A\u1D04\u1D0F\u0280\u0274\u0262\u1D0F\u1D05.\u0262\u0262  \uA731\u1D07\u1D18\u1D18\u1D1C\u1D0B\u1D1C | \u029C\u1D1C\u1D22\u1D1C\u0274\u026A\u0262\u0280\u1D07\u1D07\u0274.\u0262\u0262tm \u00BB \u0299\u1D00\u1D04\u1D0B\u1D04\u029F\u026A\u1D07\u0274\u1D1Btm \u00BB \u0274\u1D0F\u1D1C \u029F\u1D07\u1D00\u1D0B  \u23D0 \u0493\u1D0F\u0280\u0262\u1D07\u0280\u1D00\u1D1B \u2661 | \u04E8B\u039BM\u039B \u1103\u1102I\u03A3\u041F\u01AC - \u1D07\u029F\u1D07\u1D0D\u1D07\u0274\u1D1B\u1D00\u0280\uA731.\u1D04\u1D0F\u1D0D \u300B\u1D0F\uA731\u026A\u0280\u026A\uA731 | W\u00D4\u00D4K\u00CF\u00CA \u00C7L\u00EE\u00EB\u00D1Ttm {\u0280\u1D00\u026A\u1D0F\u0274\u1D0B\u1D07\u1D0B} \u30C3 \uFF32\uFF10\uFF10\uFF34\uFF5C \u1D20\u1D0F\u026A\u1D05 \u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D0B\u1D00\u1D0D\u026A \uFF5C \u02B3\u1D1C\u02E2\u02B0\u1D07\u02B3\u02B0\u1D00\u1D9C\u1D4F \uFF5C \u24CC\u24CC\u24BA \uFF5C \uFF49\uFF4D\uFF50\uFF41\uFF43\uFF54\uFF5C \uA730\u1D1C\u1D1B\u1D1C\u0280\u1D07tm \u24D6\u24DE\u24DB\u24D3 \uFF5C \u5342\u4E02\u3129\u51E0\u5342\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C\uA118\uA182\uA493\uA04D\uA35F\uA45B\uA2EB\uA1D3 \uFF5C \u02B3\u1D00\uFF54\u1D04\u029F\u026A\u1D07\u0274\u1D1B \uFF5C \u1D00\u1D18\u1D0F\u029F\u029F\u028F\u1D0F\u0274 \uFF5C \u1D0B\u1D00\u1D0D\u026A \u0299\u029F\u1D1C\u1D07 \u2758 \u1D3E\u1D3C\u1D3E\u1D2E\u1D3C\u1D2E \u1D9C\u1D38\u1D35\u1D31\u1D3A\u1D40 \u23D0 \u0262\u1D00\u028F \u23D0  c l i e n t |  B a L l C l i E n T | Phobos1.5.4.eu | popbobhack | .grabcoords | faxhax";
                    break;
                }
                case CUSTOM: {
                    s = s + this.customSuffix.getValue ( );
                    break;
                }
            }
            if ( s.length ( ) >= 256 ) {
                s = s.substring ( 0 , 256 );
            }
            packet.message = s;
        }
    }

    @SubscribeEvent
    public
    void onChatPacketReceive ( PacketEvent.Receive event ) {
        if ( event.getStage ( ) == 0 ) {
            event.getPacket ( );
        }// empty if block
    }

    @SubscribeEvent
    public
    void onPacketReceive ( PacketEvent.Receive event ) {
        if ( event.getStage ( ) == 0 && this.timeStamps.getValue ( ) != TextUtil.Color.NONE && event.getPacket ( ) instanceof SPacketChat ) {
            if ( ! ( (SPacketChat) event.getPacket ( ) ).isSystem ( ) ) {
                return;
            }
            String originalMessage = ( (SPacketChat) event.getPacket ( ) ).chatComponent.getFormattedText ( );
            String message = this.getTimeString ( originalMessage ) + originalMessage;
            ( (SPacketChat) event.getPacket ( ) ).chatComponent = new TextComponentString ( message );
        }
    }

    public
    String getTimeString ( String message ) {
        String date = new SimpleDateFormat ( "k:mm" ).format ( new Date ( ) );
        if ( this.rainbowTimeStamps.getValue ( ) ) {
            String timeString = "<" + date + ">" + ( this.space.getValue ( ) ? " " : "" );
            StringBuilder builder = new StringBuilder ( timeString );
            builder.insert ( 0 , "\u00a7+" );
            if ( ! message.contains ( Management.getInstance ( ).getRainbowCommandMessage ( ) ) ) {
                builder.append ( "\u00a7r" );
            }
            return builder.toString ( );
        }
        return ( this.bracket.getValue ( ) == TextUtil.Color.NONE ? "" : TextUtil.coloredString ( "<" , this.bracket.getValue ( ) ) ) + TextUtil.coloredString ( date , this.timeStamps.getValue ( ) ) + ( this.bracket.getValue ( ) == TextUtil.Color.NONE ? "" : TextUtil.coloredString ( ">" , this.bracket.getValue ( ) ) ) + ( this.space.getValue ( ) ? " " : "" ) + "\u00a7r";
    }

    private
    boolean shouldSendMessage ( EntityPlayer player ) {
        if ( player.dimension != 1 ) {
            return false;
        }
        if ( ! this.timer.passedS ( this.qDelay.getValue ( ) ) ) {
            return false;
        }
        return player.getPosition ( ).equals ( new Vec3i ( 0 , 240 , 0 ) );
    }

    public
    enum Suffix {
        NONE,
        NIGHTM,
        SENATI,
        CUSTOM,
        INSANE

    }
}

