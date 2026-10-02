package OverallBans.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PunishTypeMenu {

    public static final int SIZE = 54;
    public static final int COLS = 9;
    public static final String TITLE = GuiItems.color("&8OverallBans &7- &fSelect Type");
    public static final int SLOT_BACK = 45;

    public static final int SLOT_BAN=11, SLOT_TBAN=12, SLOT_IPBAN=14, SLOT_IPTBAN=15;
    public static final int SLOT_SBAN=20, SLOT_STBAN=21, SLOT_IPSBAN=23, SLOT_IPSTBAN=24;
    public static final int SLOT_MUTE=29, SLOT_TMUTE=30, SLOT_IPMUTE=32, SLOT_IPTMUTE=33;
    public static final int SLOT_SMUTE=38, SLOT_STMUTE=39, SLOT_KICK=41;

    private final GuiManager guiManager;

    public PunishTypeMenu(GuiManager guiManager) { this.guiManager = guiManager; }

    public void open(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null || session.getTargetName() == null) {
            new MainMenu(guiManager).open(admin);
            return;
        }
        session.setTransferring(true);
        try {
            GuiHolder holder = new GuiHolder(GuiHolder.MENU_PUNISH_TYPE, session.getAdminUuid());
            Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
            holder.setInventory(inv);

            ItemStack[] contents = new ItemStack[SIZE];
            GuiItems.fillBorder(contents, SIZE, COLS);
            GuiItems.fillSpacing(contents, SIZE, COLS, 1, 4);

            contents[SLOT_BACK] = GuiItems.backButton();

            contents[SLOT_BAN]    = GuiItems.make(Material.IRON_SWORD, 1, "&c&lBan", "&7Permanent ban", "", "&8punishment.ban");
            contents[SLOT_TBAN]   = GuiItems.make(Material.IRON_SWORD, 1, "&6&lTempBan", "&7Temporary ban", "&7Requires: time", "", "&8punishment.tban");
            contents[SLOT_IPBAN]  = GuiItems.make(Material.IRON_SWORD, 1, "&c&lIP-Ban", "&7Permanent IP ban", "", "&8punishment.ipban");
            contents[SLOT_IPTBAN] = GuiItems.make(Material.IRON_SWORD, 1, "&6&lIP-TempBan", "&7Temporary IP ban", "&7Requires: time", "", "&8punishment.iptban");
            contents[SLOT_SBAN]    = GuiItems.make(Material.IRON_SWORD, 1, "&7&lSilent Ban", "&7Permanent ban (silent)", "", "&8punishment.sban");
            contents[SLOT_STBAN]   = GuiItems.make(Material.IRON_SWORD, 1, "&8&lSilent TempBan", "&7Temporary ban (silent)", "&7Requires: time", "", "&8punishment.stban");
            contents[SLOT_IPSBAN]  = GuiItems.make(Material.IRON_SWORD, 1, "&7&lSilent IP-Ban", "&7Permanent IP ban (silent)", "", "&8punishment.ipsban");
            contents[SLOT_IPSTBAN] = GuiItems.make(Material.IRON_SWORD, 1, "&8&lSilent IP-TempBan", "&7Temporary IP ban (silent)", "&7Requires: time", "", "&8punishment.ipstban");
            contents[SLOT_MUTE]    = GuiItems.make(Material.PAPER, 1, "&c&lMute", "&7Permanent mute", "", "&8punishment.mute");
            contents[SLOT_TMUTE]   = GuiItems.make(Material.PAPER, 1, "&6&lTempMute", "&7Temporary mute", "&7Requires: time", "", "&8punishment.tmute");
            contents[SLOT_IPMUTE]  = GuiItems.make(Material.PAPER, 1, "&c&lIP-Mute", "&7Permanent IP mute", "", "&8punishment.ipmute");
            contents[SLOT_IPTMUTE] = GuiItems.make(Material.PAPER, 1, "&6&lIP-TempMute", "&7Temporary IP mute", "&7Requires: time", "", "&8punishment.iptmute");
            contents[SLOT_SMUTE]  = GuiItems.make(Material.PAPER, 1, "&7&lSilent Mute", "&7Permanent mute (silent)", "", "&8punishment.smute");
            contents[SLOT_STMUTE] = GuiItems.make(Material.PAPER, 1, "&8&lSilent TempMute", "&7Temporary mute (silent)", "&7Requires: time", "", "&8punishment.stmute");
            contents[SLOT_KICK]    = GuiItems.make(Material.LEATHER_BOOTS, 1, "&e&lKick", "&7Kick the player now", "", "&8punishment.kick");

            inv.setContents(contents);
            admin.openInventory(inv);
        } finally {
            session.setTransferring(false);
        }
    }

    public static String typeKeyForSlot(int slot) {
        switch (slot) {
            case SLOT_BAN: return "ban"; case SLOT_TBAN: return "tban";
            case SLOT_IPBAN: return "ipban"; case SLOT_IPTBAN: return "iptban";
            case SLOT_SBAN: return "sban"; case SLOT_STBAN: return "stban";
            case SLOT_IPSBAN: return "ipsban"; case SLOT_IPSTBAN: return "ipstban";
            case SLOT_MUTE: return "mute"; case SLOT_TMUTE: return "tmute";
            case SLOT_IPMUTE: return "ipmute"; case SLOT_IPTMUTE: return "iptmute";
            case SLOT_SMUTE: return "smute"; case SLOT_STMUTE: return "stmute";
            case SLOT_KICK: return "kick";
            default: return null;
        }
    }

    public static boolean isTempType(String key) {
        if (key == null) return false;
        return key.equals("tban") || key.equals("iptban") || key.equals("stban") || key.equals("ipstban")
                || key.equals("tmute") || key.equals("iptmute") || key.equals("stmute");
    }
}
