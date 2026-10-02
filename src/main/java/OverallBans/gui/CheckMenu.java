package OverallBans.gui;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckMenu {

    public static final int SIZE = 27;
    public static final int COLS = 9;
    public static final String TITLE = GuiItems.color("&8OverallBans &7- &fCheck");

    public static final int SLOT_BACK = 18;
    public static final int SLOT_WOOL_BAN = 11;
    public static final int SLOT_WOOL_MUTE = 15;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GuiManager guiManager;
    private final PunishmentPlugin plugin;

    public CheckMenu(GuiManager guiManager, PunishmentPlugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;
    }

    public void open(Player admin) {
        GuiSession session = guiManager.getSession(admin);
        if (session == null || session.getCheckTargetName() == null) {
            new MainMenu(guiManager).open(admin);
            return;
        }
        session.setTransferring(true);
        try {
            GuiHolder holder = new GuiHolder(GuiHolder.MENU_CHECK, session.getAdminUuid());
            Inventory inv = Bukkit.createInventory(holder, SIZE, TITLE);
            holder.setInventory(inv);

            ItemStack[] contents = new ItemStack[SIZE];
            GuiItems.fillBorder(contents, SIZE, COLS);
            GuiItems.fillSpacing(contents, SIZE, COLS, 1, 2);

            if (!session.isCheckFromCommand()) {
                contents[SLOT_BACK] = GuiItems.backButton();
            }

            String name = session.getCheckTargetName();
            UUID uuid = session.getCheckTargetUuid() != null
                    ? UUID.fromString(session.getCheckTargetUuid()) : null;
            String ip = session.getCheckTargetIp();

            PunishmentRecord ban = plugin.getBanManager().getActiveBan(name, uuid, ip);
            PunishmentRecord mute = plugin.getMuteManager().getActiveMute(name, uuid, ip);

            contents[SLOT_WOOL_BAN] = buildWoolBan(name, ban);
            contents[SLOT_WOOL_MUTE] = buildWoolMute(name, mute);

            inv.setContents(contents);
            admin.openInventory(inv);
        } finally {
            session.setTransferring(false);
        }
    }

    private ItemStack buildWoolBan(String name, PunishmentRecord ban) {
        List<String> lore = new ArrayList<>();
        if (ban != null) {
            lore.add(GuiItems.color("&7Player: &f" + name));
            lore.add(GuiItems.color("&7Status: &a&lBANNED"));
            lore.add("");
            lore.add(GuiItems.color("&7Type: &f" + ban.getType().getKey()));
            lore.add(GuiItems.color("&7Banned by: &f" + n(ban.getIssuedBy())));
            lore.add(GuiItems.color("&7Reason: &f" + n(ban.getReason())));
            lore.add(GuiItems.color("&7Issued at: &f" + formatDate(ban.getIssuedAt())));
            lore.add(GuiItems.color("&7Expires at: &f" + (ban.isPermanent()
                    ? "permanent" : formatDate(ban.getExpiresAt()))));
            lore.add(GuiItems.color("&7Time left: &f" + (ban.isPermanent()
                    ? "permanent" : formatRemaining(ban.getExpiresAt()))));
            lore.add(GuiItems.color("&7Silent: &f" + (ban.isSilent() ? "yes" : "no")));
            return GuiItems.make(Material.WOOL, (short) 5, 1, "&a&lBan Info", lore.toArray(new String[0]));
        } else {
            lore.add(GuiItems.color("&7Player: &f" + name));
            lore.add(GuiItems.color("&7Status: &aNOT BANNED"));
            lore.add("");
            lore.add(GuiItems.color("&7This player has no active ban."));
            return GuiItems.make(Material.WOOL, (short) 14, 1, "&c&lBan Info", lore.toArray(new String[0]));
        }
    }

    private ItemStack buildWoolMute(String name, PunishmentRecord mute) {
        List<String> lore = new ArrayList<>();
        if (mute != null) {
            lore.add(GuiItems.color("&7Player: &f" + name));
            lore.add(GuiItems.color("&7Status: &a&lMUTED"));
            lore.add("");
            lore.add(GuiItems.color("&7Type: &f" + mute.getType().getKey()));
            lore.add(GuiItems.color("&7Muted by: &f" + n(mute.getIssuedBy())));
            lore.add(GuiItems.color("&7Reason: &f" + n(mute.getReason())));
            lore.add(GuiItems.color("&7Issued at: &f" + formatDate(mute.getIssuedAt())));
            lore.add(GuiItems.color("&7Expires at: &f" + (mute.isPermanent()
                    ? "permanent" : formatDate(mute.getExpiresAt()))));
            lore.add(GuiItems.color("&7Time left: &f" + (mute.isPermanent()
                    ? "permanent" : formatRemaining(mute.getExpiresAt()))));
            lore.add(GuiItems.color("&7Silent: &f" + (mute.isSilent() ? "yes" : "no")));
            return GuiItems.make(Material.WOOL, (short) 5, 1, "&a&lMute Info", lore.toArray(new String[0]));
        } else {
            lore.add(GuiItems.color("&7Player: &f" + name));
            lore.add(GuiItems.color("&7Status: &aNOT MUTED"));
            lore.add("");
            lore.add(GuiItems.color("&7This player has no active mute."));
            return GuiItems.make(Material.WOOL, (short) 14, 1, "&c&lMute Info", lore.toArray(new String[0]));
        }
    }

    private String n(String v) { return v == null ? "" : v; }

    private String formatDate(long epoch) {
        if (epoch <= 0) return "N/A";
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault()).format(DATE_FMT);
    }

    private String formatRemaining(long expiresAt) {
        long remaining = expiresAt - System.currentTimeMillis();
        if (remaining < 0) remaining = 0;
        return OverallBans.utils.TimeFormatter.formatRemaining(remaining);
    }
}
