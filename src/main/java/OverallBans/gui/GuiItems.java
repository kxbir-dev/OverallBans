package OverallBans.gui;

import OverallBans.PunishmentPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class GuiItems {

    private static PunishmentPlugin plugin;

    private GuiItems() {}

    public static void init(PunishmentPlugin plugin) {
        GuiItems.plugin = plugin;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> colorLore(String... lines) {
        List<String> out = new ArrayList<>();
        if (lines != null) for (String l : lines) out.add(color(l));
        return out;
    }

    public static boolean isFillEnabled() {
        if (plugin == null) return true;
        return plugin.getConfigManager().getBoolean("gui.fill-glass", true);
    }

    public static boolean isBorderEnabled() {
        if (plugin == null) return true;
        return plugin.getConfigManager().getBoolean("gui.border-glass", true);
    }

    public static ItemStack border() {
        return make(Material.STAINED_GLASS_PANE, (short) 7, 1, " ", (String[]) null);
    }

    public static ItemStack spacing() {
        return make(Material.STAINED_GLASS_PANE, (short) 15, 1, " ", (String[]) null);
    }

    public static ItemStack make(Material mat, short data, int amount, String name, String... lore) {
        ItemStack is = new ItemStack(mat, amount, data);
        ItemMeta m = is.getItemMeta();
        if (m != null) {
            if (name != null) m.setDisplayName(color(name));
            if (lore != null && lore.length > 0) m.setLore(colorLore(lore));
            is.setItemMeta(m);
        }
        return is;
    }

    public static ItemStack make(Material mat, int amount, String name, String... lore) {
        return make(mat, (short) 0, amount, name, lore);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack playerHead(String playerName, String... lore) {
        ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        ItemMeta m = is.getItemMeta();
        if (m instanceof SkullMeta) {
            SkullMeta sm = (SkullMeta) m;
            sm.setOwner(playerName);
            sm.setDisplayName(color("&a" + playerName));
            if (lore != null && lore.length > 0) sm.setLore(colorLore(lore));
            is.setItemMeta(sm);
        }
        return is;
    }

    public static void fillBorder(ItemStack[] contents, int size, int cols) {
        if (contents == null) return;
        if (!isBorderEnabled()) return;
        int rows = size / cols;
        ItemStack b = border();
        for (int c = 0; c < cols; c++) { contents[c] = b; contents[(rows - 1) * cols + c] = b; }
        for (int r = 1; r < rows - 1; r++) { contents[r * cols] = b; contents[r * cols + cols - 1] = b; }
    }

    public static void fillSpacing(ItemStack[] contents, int size, int cols, int startRow, int endRow) {
        if (contents == null) return;
        if (!isFillEnabled()) return;
        for (int r = startRow; r <= endRow; r++) {
            for (int c = 1; c < cols - 1; c++) {
                int slot = r * cols + c;
                if (slot < size && contents[slot] == null) contents[slot] = spacing();
            }
        }
    }

    public static ItemStack quickPunishButton() {
        return make(Material.IRON_SWORD, 1, "&c&lQuickPunish",
                "&7Click to punish an online player", "&7using a graphical interface.");
    }

    public static ItemStack offlinePunishButton() {
        return make(Material.BARRIER, 1, "&7&lOfflinePunish",
                "&7Click to punish an offline player", "&7by typing their name.");
    }

    public static ItemStack backButton() {
        return make(Material.ARROW, 1, "&f&lBack", "&7Return to previous menu.");
    }

    public static ItemStack cancelButton() {
        return make(Material.BARRIER, 1, "&c&lCancel", "&7Cancel the current punishment flow.");
    }

    public static ItemStack confirmButton() {
        return make(Material.WOOL, (short) 5, 1, "&a&lConfirm", "&7Click to apply the punishment.");
    }

    public static ItemStack anvilInputLabel(String fieldName, String description) {
        return make(Material.PAPER, 1, "&f" + fieldName,
                "&7" + description, "",
                "&eType your text in the rename field above.",
                "&eThen click the result item to confirm.",
                "&7Leave empty and click result to skip.");
    }
}
