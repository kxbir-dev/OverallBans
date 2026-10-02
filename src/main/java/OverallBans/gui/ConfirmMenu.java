package OverallBans.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmMenu {

    public static final int SIZE = 54;
    public static final int COLS = 9;
    public static final String TITLE = GuiItems.color("&8OverallBans &7- &fConfirm");
    public static final int SLOT_BACK = 45;
    public static final int SLOT_CANCEL = 49;
    public static final int SLOT_CONFIRM = 53;
    public static final int SLOT_TARGET = 22;

    private final GuiManager guiManager;

    public ConfirmMenu(GuiManager guiManager) { this.guiManager = guiManager; }

    public void open(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null || session.getPunishmentTypeKey() == null) {
            new MainMenu(guiManager).open(admin);
            return;
        }
        session.setTransferring(true);
        try {
            GuiHolder holder = new GuiHolder(GuiHolder.MENU_CONFIRM, session.getAdminUuid());
            Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
            holder.setInventory(inv);

            ItemStack[] contents = new ItemStack[SIZE];
            GuiItems.fillBorder(contents, SIZE, COLS);
            GuiItems.fillSpacing(contents, SIZE, COLS, 1, 4);

            contents[SLOT_BACK] = GuiItems.backButton();

            List<String> lore = new ArrayList<>();
            lore.add(GuiItems.color("&7Target: &f" + session.getTargetName()));
            if (session.isOfflineTarget()) {
                lore.add(GuiItems.color("&7Status: &c&lOFFLINE"));
            } else {
                lore.add(GuiItems.color("&7Status: &a&lONLINE"));
            }
            lore.add(GuiItems.color("&7Type: &f" + session.getPunishmentTypeKey()));
            lore.add("");
            if (session.isNeedsTime()) {
                if (session.isTimeExplicit() && session.getTimeInput() != null && !session.getTimeInput().trim().isEmpty())
                    lore.add(GuiItems.color("&7Time: &f" + session.getTimeInput()));
                else
                    lore.add(GuiItems.color("&7Time: &8(not provided)"));
            }
            if (session.isWhomutedExplicit() && session.getWhomutedInput() != null && !session.getWhomutedInput().trim().isEmpty())
                lore.add(GuiItems.color("&7WhoPunished: &f" + session.getWhomutedInput()));
            else
                lore.add(GuiItems.color("&7WhoPunished: &8(default)"));
            if (session.isReasonExplicit() && session.getReasonInput() != null && !session.getReasonInput().trim().isEmpty())
                lore.add(GuiItems.color("&7Reason: &f" + session.getReasonInput()));
            else
                lore.add(GuiItems.color("&7Reason: &8(none)"));
            lore.add("");
            lore.add(GuiItems.color("&aClick confirm to apply."));

            ItemStack targetHead = GuiItems.playerHead(session.getTargetName());
            if (targetHead.hasItemMeta() && targetHead.getItemMeta() != null) {
                ItemMeta meta = targetHead.getItemMeta();
                meta.setLore(lore);
                targetHead.setItemMeta(meta);
            }
            contents[SLOT_TARGET] = targetHead;
            contents[SLOT_CANCEL] = GuiItems.cancelButton();
            contents[SLOT_CONFIRM] = GuiItems.confirmButton();

            inv.setContents(contents);
            admin.openInventory(inv);
        } finally {
            session.setTransferring(false);
        }
    }
}
