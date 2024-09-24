package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import org.bukkit.Color;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.TextDisplay;

public class CraftTextDisplay extends CraftDisplay implements TextDisplay {

    public CraftTextDisplay(CraftServer server, net.minecraft.world.entity.Display.TextDisplay entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.Display.TextDisplay getHandle() {
        return (net.minecraft.world.entity.Display.TextDisplay) super.getHandle();
    }

    @Override
    public String toString() {
        return "CraftTextDisplay";
    }

    @Override
    public String getText() {
        return CraftChatMessage.fromComponent(getHandle().getText());
    }

    @Override
    public void setText(String text) {
        getHandle().setText(CraftChatMessage.fromString(text, true)[0]);
    }

    @Override
    public int getLineWidth() {
        return getHandle().getLineWidth();
    }

    @Override
    public void setLineWidth(int width) {
        getHandle().getEntityData().set(net.minecraft.world.entity.Display.TextDisplay.DATA_LINE_WIDTH_ID, width);
    }

    @Override
    public Color getBackgroundColor() {
        int color = getHandle().getBackgroundColor();

        return (color == -1) ? null : Color.fromARGB(color);
    }

    @Override
    public void setBackgroundColor(Color color) {
        if (color == null) {
            getHandle().getEntityData().set(net.minecraft.world.entity.Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, -1);
        } else {
            getHandle().getEntityData().set(net.minecraft.world.entity.Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, color.asARGB());
        }
    }

    @Override
    public byte getTextOpacity() {
        return getHandle().getTextOpacity();
    }

    @Override
    public void setTextOpacity(byte opacity) {
        getHandle().setTextOpacity(opacity);
    }

    @Override
    public boolean isShadowed() {
        return getFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_SHADOW);
    }

    @Override
    public void setShadowed(boolean shadow) {
        setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_SHADOW, shadow);
    }

    @Override
    public boolean isSeeThrough() {
        return getFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_SEE_THROUGH);
    }

    @Override
    public void setSeeThrough(boolean seeThrough) {
        setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_SEE_THROUGH, seeThrough);
    }

    @Override
    public boolean isDefaultBackground() {
        return getFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND);
    }

    @Override
    public void setDefaultBackground(boolean defaultBackground) {
        setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND, defaultBackground);
    }

    @Override
    public TextAlignment getAlignment() {
        net.minecraft.world.entity.Display.TextDisplay.Align nms = net.minecraft.world.entity.Display.TextDisplay.getAlign(getHandle().getFlags());
        return TextAlignment.valueOf(nms.name());
    }

    @Override
    public void setAlignment(TextAlignment alignment) {
        Preconditions.checkArgument(alignment != null, "Alignment cannot be null");

        switch (alignment) {
            case LEFT:
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_LEFT, true);
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_RIGHT, false);
                break;
            case RIGHT:
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_LEFT, false);
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_RIGHT, true);
                break;
            case CENTER:
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_LEFT, false);
                setFlag(net.minecraft.world.entity.Display.TextDisplay.FLAG_ALIGN_RIGHT, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown alignment " + alignment);
        }
    }

    private boolean getFlag(int flag) {
        return (getHandle().getFlags() & flag) != 0;
    }

    private void setFlag(int flag, boolean set) {
        byte flagBits = getHandle().getFlags();

        if (set) {
            flagBits |= flag;
        } else {
            flagBits &= ~flag;
        }

        getHandle().setFlags(flagBits);
    }
}
