package OverallBans.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerListMenu {

    public static final int SIZE = 54;
    public static final int COLS = 9;
    public static final String TITLE = GuiItems.color("&8OverallBans &7- &fQuickPunish");
    public static final int SLOT_OFFLINE_PUNISH = 8;
    public static final int SLOT_BACK = 45;

    private static final int[] PLAYER_SLOTS = {
            10, 11, 12, 13, 14, 15,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final GuiManager guiManager;

    public PlayerListMenu(GuiManager guiManager) { this.guiManager = guiManager; }

    public void open(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) { new MainMenu(guiManager).open(admin); return; }
        session.setTransferring(true);
        try {
            GuiHolder holder = new GuiHolder(GuiHolder.MENU_PLAYER_LIST, session.getAdminUuid());
            Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
            holder.setInventory(inv);

            ItemStack[] contents = new ItemStack[SIZE];
            GuiItems.fillBorder(contents, SIZE, COLS);

            if (GuiItems.isFillEnabled()) {
                for (int slot : PLAYER_SLOTS) if (slot < SIZE) contents[slot] = GuiItems.spacing();
            }

            contents[SLOT_OFFLINE_PUNISH] = GuiItems.offlinePunishButton();
            contents[SLOT_BACK] = GuiItems.backButton();

            List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
            online.removeIf(p -> p.getUniqueId().equals(admin.getUniqueId()));
            for (int i = 0; i < PLAYER_SLOTS.length && i < online.size(); i++) {
                Player target = online.get(i);
                contents[PLAYER_SLOTS[i]] = GuiItems.playerHead(target.getName(),
                        "&7Click to punish &f" + target.getName());
            }
            inv.setContents(contents);
            admin.openInventory(inv);
        } finally {
            session.setTransferring(false);
        }
    }
}
