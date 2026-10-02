package OverallBans.gui;

import OverallBans.PunishmentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AnvilInputMenu {

    public static final String TITLE_TIME = GuiItems.color("&8OverallBans &7- &fTime");
    public static final String TITLE_WHOMUTED = GuiItems.color("&8OverallBans &7- &fWhoPunished");
    public static final String TITLE_REASON = GuiItems.color("&8OverallBans &7- &fReason");
    public static final String TITLE_PLAYER_NAME = GuiItems.color("&8OverallBans &7- &fPlayerName");
    public static final String TITLE_CHECK_PLAYER = GuiItems.color("&8OverallBans &7- &fCheckPlayer");

    public static final int SLOT_OUTPUT = 2;

    private final GuiManager guiManager;

    public AnvilInputMenu(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    public void openCheckPlayerName(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) return;
        session.setCurrentStep(GuiHolder.MENU_ANVIL_CHECK_PLAYER);
        openAnvil(admin, GuiHolder.MENU_ANVIL_CHECK_PLAYER, TITLE_CHECK_PLAYER,
                GuiItems.anvilInputLabel("CheckPlayer", "Type the name of the player to check"));
    }

    public void openPlayerName(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) return;
        session.setCurrentStep(GuiHolder.MENU_ANVIL_PLAYER_NAME);
        openAnvil(admin, GuiHolder.MENU_ANVIL_PLAYER_NAME, TITLE_PLAYER_NAME,
                GuiItems.anvilInputLabel("PlayerName", "Type the name of the player to punish"));
    }

    public void openTime(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) return;
        session.setCurrentStep(GuiHolder.MENU_ANVIL_TIME);
        openAnvil(admin, GuiHolder.MENU_ANVIL_TIME, TITLE_TIME,
                GuiItems.anvilInputLabel("Time", "Format: e.g. 1y6h30m, 90d, 2h, 30m"));
    }

    public void openWhomuted(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) return;
        session.setCurrentStep(GuiHolder.MENU_ANVIL_WHOMUTED);
        openAnvil(admin, GuiHolder.MENU_ANVIL_WHOMUTED, TITLE_WHOMUTED,
                GuiItems.anvilInputLabel("WhoPunished", "The name shown as who issued the punishment"));
    }

    public void openReason(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null) return;
        session.setCurrentStep(GuiHolder.MENU_ANVIL_REASON);
        openAnvil(admin, GuiHolder.MENU_ANVIL_REASON, TITLE_REASON,
                GuiItems.anvilInputLabel("Reason", "The reason for this punishment"));
    }

    private void openAnvil(Player admin, String menuType, String title, ItemStack inputItem) {
        final GuiSession session = guiManager.getSession(admin);
        if (session == null) return;

        session.setTransferring(true);

        Bukkit.getScheduler().runTask(PunishmentPlugin.getInstance(), () -> {
            try {
                AnvilHolder holder = new AnvilHolder(admin.getUniqueId(), menuType);
                holder.openRealAnvil(admin, title, inputItem);
                session.setCurrentAnvilHolder(holder);
            } finally {
                session.setTransferring(false);
            }
        });
    }
}
