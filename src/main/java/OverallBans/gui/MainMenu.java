package OverallBans.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainMenu {

    public static final int SIZE = 54;
    public static final int COLS = 9;
    public static final String TITLE = GuiItems.color("&8OverallBans &7- &fMain");
    public static final int SLOT_QUICK_PUNISH = 4;
    public static final int SLOT_OFFLINE_PUNISH = 8;
    public static final int SLOT_CHECK_PUNISH = 0;

    private final GuiManager guiManager;

    public MainMenu(GuiManager guiManager) { this.guiManager = guiManager; }

    public void open(Player admin) {
        GuiSession session = guiManager.startSession(admin);
        session.setTransferring(true);
        try {
            GuiHolder holder = new GuiHolder(GuiHolder.MENU_MAIN, session.getAdminUuid());
            Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
            holder.setInventory(inv);

            ItemStack[] contents = new ItemStack[SIZE];
            GuiItems.fillBorder(contents, SIZE, COLS);
            GuiItems.fillSpacing(contents, SIZE, COLS, 1, 4);

            contents[SLOT_QUICK_PUNISH] = GuiItems.quickPunishButton();
            contents[SLOT_OFFLINE_PUNISH] = GuiItems.offlinePunishButton();
            contents[SLOT_CHECK_PUNISH] = GuiItems.make(Material.BOOK, 1, "&b&lCheckPunish",
                    "&7Check active punishments",
                    "&7for a specific player.");

            inv.setContents(contents);
            admin.openInventory(inv);
        } finally {
            session.setTransferring(false);
        }
    }
}
