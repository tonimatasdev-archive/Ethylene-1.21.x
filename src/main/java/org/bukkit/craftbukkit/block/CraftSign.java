package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.block.sign.CraftSignSide;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.jetbrains.annotations.NotNull;

public class CraftSign<T extends net.minecraft.world.level.block.entity.SignBlockEntity> extends CraftBlockEntityState<T> implements Sign {

    private final CraftSignSide front;
    private final CraftSignSide back;

    public CraftSign(World world, T tileEntity) {
        super(world, tileEntity);
        this.front = new CraftSignSide(this.getSnapshot().getFrontText());
        this.back = new CraftSignSide(this.getSnapshot().getBackText());
    }

    protected CraftSign(CraftSign<T> state, Location location) {
        super(state, location);
        this.front = new CraftSignSide(this.getSnapshot().getFrontText());
        this.back = new CraftSignSide(this.getSnapshot().getBackText());
    }

    @Override
    public String[] getLines() {
        return front.getLines();
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {
        return front.getLine(index);
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        front.setLine(index, line);
    }

    @Override
    public boolean isEditable() {
        return !isWaxed();
    }

    @Override
    public void setEditable(boolean editable) {
        this.setWaxed(!editable);
    }

    @Override
    public boolean isWaxed() {
        return getSnapshot().isWaxed();
    }

    @Override
    public void setWaxed(boolean waxed) {
        getSnapshot().setWaxed(waxed);
    }

    @Override
    public boolean isGlowingText() {
        return front.isGlowingText();
    }

    @Override
    public void setGlowingText(boolean glowing) {
        front.setGlowingText(glowing);
    }

    @NotNull
    @Override
    public SignSide getSide(Side side) {
        Preconditions.checkArgument(side != null, "side == null");

        switch (side) {
            case FRONT:
                return front;
            case BACK:
                return back;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public SignSide getTargetSide(Player player) {
        ensureNoWorldGeneration();
        Preconditions.checkArgument(player != null, "player cannot be null");

        if (getSnapshot().isFacingFrontText(((CraftPlayer) player).getHandle())) {
            return front;
        }

        return back;
    }

    @Override
    public Player getAllowedEditor() {
        ensureNoWorldGeneration();

        // getPlayerWhoMayEdit is always null for the snapshot, so we use the wrapped TileEntity
        UUID id = getTileEntity().getPlayerWhoMayEdit();
        return (id == null) ? null : Bukkit.getPlayer(id);
    }

    @Override
    public DyeColor getColor() {
        return front.getColor();
    }

    @Override
    public void setColor(DyeColor color) {
        front.setColor(color);
    }

    @Override
    public void applyTo(T sign) {
        getSnapshot().setText(front.applyLegacyStringToSignSide(), true);
        getSnapshot().setText(back.applyLegacyStringToSignSide(), false);

        super.applyTo(sign);
    }

    @Override
    public CraftSign<T> copy() {
        return new CraftSign<T>(this, null);
    }

    @Override
    public CraftSign<T> copy(Location location) {
        return new CraftSign<T>(this, location);
    }

    public static void openSign(Sign sign, Player player, Side side) {
        Preconditions.checkArgument(sign != null, "sign == null");
        Preconditions.checkArgument(side != null, "side == null");
        Preconditions.checkArgument(sign.isPlaced(), "Sign must be placed");
        Preconditions.checkArgument(sign.getWorld() == player.getWorld(), "Sign must be in same world as Player");

        if (!CraftEventFactory.callPlayerSignOpenEvent(player, sign, side, PlayerSignOpenEvent.Cause.PLUGIN)) {
            return;
        }

        net.minecraft.world.level.block.entity.SignBlockEntity handle = ((CraftSign<?>) sign).getTileEntity();
        handle.setAllowedPlayerEditor(player.getUniqueId());

        ((CraftPlayer) player).getHandle().openTextEdit(handle, Side.FRONT == side);
    }

    public static net.minecraft.network.chat.Component[] sanitizeLines(String[] lines) {
        net.minecraft.network.chat.Component[] components = new net.minecraft.network.chat.Component[4];

        for (int i = 0; i < 4; i++) {
            if (i < lines.length && lines[i] != null) {
                components[i] = CraftChatMessage.fromString(lines[i])[0];
            } else {
                components[i] = net.minecraft.network.chat.Component.empty();
            }
        }

        return components;
    }

    public static String[] revertComponents(net.minecraft.network.chat.Component[] components) {
        String[] lines = new String[components.length];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = revertComponent(components[i]);
        }
        return lines;
    }

    private static String revertComponent(net.minecraft.network.chat.Component component) {
        return CraftChatMessage.fromComponent(component);
    }
}
