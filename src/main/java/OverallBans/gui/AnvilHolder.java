package OverallBans.gui;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.UUID;

public class AnvilHolder implements InventoryHolder {

    private final UUID sessionId;
    private final String menuType;
    private ContainerAnvil container;
    private CraftInventoryAnvil bukkitInventory;

    public AnvilHolder(UUID sessionId, String menuType) {
        this.sessionId = sessionId;
        this.menuType = menuType;
    }

    public UUID getSessionId() { return sessionId; }
    public String getMenuType() { return menuType; }
    public ContainerAnvil getContainer() { return container; }
    public CraftInventoryAnvil getBukkitInventory() { return bukkitInventory; }

    public void openRealAnvil(Player player, String title, org.bukkit.inventory.ItemStack inputItem) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        PlayerInventory playerInv = entityPlayer.inventory;
        World world = ((CraftWorld) player.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(
                (int) player.getLocation().getX(),
                (int) player.getLocation().getY(),
                (int) player.getLocation().getZ());

        CustomAnvilContainer anvil = new CustomAnvilContainer(playerInv, world, pos, entityPlayer);

        this.container = anvil;
        this.bukkitInventory = (CraftInventoryAnvil) anvil.getBukkitView().getTopInventory();

        if (inputItem != null) {
            bukkitInventory.setItem(0, inputItem);
        }

        IChatBaseComponent chatTitle = CraftChatMessage.fromString(title)[0];

        int containerId = entityPlayer.nextContainerCounter();
        entityPlayer.activeContainer = anvil;
        anvil.windowId = containerId;

        entityPlayer.playerConnection.sendPacket(
                new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", chatTitle, 0));

        anvil.addSlotListener(entityPlayer);
    }

    public String getRenamedText() {
        if (container == null) return "";
        try {
            Field lf = findField(container.getClass(), "l");
            if (lf != null) {
                lf.setAccessible(true);
                return (String) lf.get(container);
            }
        } catch (Throwable t) {}
        return "";
    }

    private static Field findField(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try { return clazz.getDeclaredField(name); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        return null;
    }

    @Override
    public Inventory getInventory() { return bukkitInventory; }
}
