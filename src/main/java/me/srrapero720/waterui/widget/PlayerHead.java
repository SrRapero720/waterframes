package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/** Face of a player, with their name on the tooltip. Falls back to the default skin offline. */
public class PlayerHead extends Element {
    private static final int DEFAULT_SIZE = 8;

    private UUID uuid;

    public PlayerHead() {}

    public PlayerHead(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerHead uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    private PlayerInfo info() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || uuid == null || uuid.equals(Util.NIL_UUID)) return null;
        return connection.getPlayerInfo(uuid);
    }

    @Override
    protected int prefContentWidth(int available) {
        return DEFAULT_SIZE;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return DEFAULT_SIZE;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        PlayerInfo info = this.info();
        // PLAIN STEVE WHEN NOBODY IS BEHIND IT, NOT THE UUID DEPENDENT DEFAULT THAT MAY GIVE ALEX
        ResourceLocation skin = info != null ? info.getSkin().texture() : DefaultPlayerSkin.getDefaultTexture();
        // THE FACE RENDERER TAKES ONE SIDE, SO THE SMALLER ONE KEEPS IT INSIDE THE BOX
        int size = Math.min(contentWidth(), contentHeight());
        PlayerFaceRenderer.draw(graphics, skin, contentX() + (contentWidth() - size) / 2,
                contentY() + (contentHeight() - size) / 2, size);
    }

    @Override
    public List<Component> tooltip() {
        // GENERIC tooltip= SUPPLIER WINS WHEN SET, FALLING BACK TO THE PLAYER NAME BUILT-IN
        List<Component> generic = super.tooltip();
        if (generic != null) return generic;
        PlayerInfo info = this.info();
        String name = info != null ? info.getProfile().getName() : null;
        Component username = Component.literal(name != null ? name : translate("waterframes.gui.source.unknown"))
                .withStyle(ChatFormatting.AQUA);
        return List.of(translatable("waterframes.gui.source.added_by", username));
    }
}
