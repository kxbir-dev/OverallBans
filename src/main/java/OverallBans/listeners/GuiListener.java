package OverallBans.listeners;

import OverallBans.PunishmentPlugin;
import OverallBans.gui.*;
import OverallBans.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class GuiListener implements Listener {

    private final PunishmentPlugin plugin;

    public GuiListener(PunishmentPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player admin = (Player) event.getWhoClicked();

        if (plugin.isDisabled()) {
            event.setCancelled(true);
            admin.closeInventory();
            admin.sendMessage(MessageUtils.colorize(plugin.getUpdateManager().getDeprecationMessage()));
            return;
        }

        GuiSession session = plugin.getGuiManager().getSession(admin);
        if (session == null) return;

        event.setCancelled(true);

        if (session.getCurrentAnvilHolder() != null) {
            handleAnvilClick(event, admin, session);
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (top == null) return;
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof GuiHolder)) return;
        GuiHolder guiHolder = (GuiHolder) holder;

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) return;

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        GuiManager gm = plugin.getGuiManager();

        switch (guiHolder.getMenuType()) {
            case GuiHolder.MENU_MAIN:
                if (slot == MainMenu.SLOT_QUICK_PUNISH) new PlayerListMenu(gm).open(admin);
                else if (slot == MainMenu.SLOT_OFFLINE_PUNISH) new AnvilInputMenu(gm).openPlayerName(admin);
                else if (slot == MainMenu.SLOT_CHECK_PUNISH) new AnvilInputMenu(gm).openCheckPlayerName(admin);
                break;

            case GuiHolder.MENU_PLAYER_LIST:
                if (slot == PlayerListMenu.SLOT_OFFLINE_PUNISH) {
                    new AnvilInputMenu(gm).openPlayerName(admin);
                    return;
                }
                if (slot == PlayerListMenu.SLOT_BACK) {
                    new MainMenu(gm).open(admin);
                    return;
                }
                Player target = resolvePlayerFromHead(clicked);
                if (target != null) {
                    session.setTargetPlayer(target);
                    new PunishTypeMenu(gm).open(admin);
                }
                break;

            case GuiHolder.MENU_PUNISH_TYPE:
                if (slot == PunishTypeMenu.SLOT_BACK) {
                    if (session.isOfflineTarget()) {
                        new AnvilInputMenu(gm).openPlayerName(admin);
                    } else {
                        new PlayerListMenu(gm).open(admin);
                    }
                    return;
                }
                String typeKey = PunishTypeMenu.typeKeyForSlot(slot);
                if (typeKey != null) {
                    session.setPunishmentTypeKey(typeKey);
                    session.setTemporary(PunishTypeMenu.isTempType(typeKey));
                    session.setNeedsTime(PunishTypeMenu.isTempType(typeKey));
                    startFieldCollection(admin, session);
                }
                break;

            case GuiHolder.MENU_CONFIRM:
                if (slot == ConfirmMenu.SLOT_BACK) new PunishTypeMenu(gm).open(admin);
                else if (slot == ConfirmMenu.SLOT_CANCEL) {
                    gm.endSession(admin);
                    admin.closeInventory();
                    admin.sendMessage(MessageUtils.colorize("&cPunishment cancelled."));
                } else if (slot == ConfirmMenu.SLOT_CONFIRM) {
                    executePunishment(admin, session);
                }
                break;

            case GuiHolder.MENU_CHECK:
                if (slot == CheckMenu.SLOT_BACK && !session.isCheckFromCommand()) {
                    new AnvilInputMenu(gm).openCheckPlayerName(admin);
                }
                break;
        }
    }

    private void handleAnvilClick(InventoryClickEvent event, Player admin, GuiSession session) {
        int slot = event.getRawSlot();
        if (slot != AnvilInputMenu.SLOT_OUTPUT) return;

        AnvilHolder holder = session.getCurrentAnvilHolder();
        if (holder == null) return;

        String text = holder.getRenamedText();
        if (text == null) text = "";

        ItemStack output = holder.getBukkitInventory().getItem(AnvilInputMenu.SLOT_OUTPUT);
        if (output != null && output.hasItemMeta()) {
            ItemMeta meta = output.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String name = meta.getDisplayName();
                if (name != null) {
                    String stripped = ChatColor.stripColor(name).trim();
                    if (!stripped.isEmpty()) text = stripped;
                }
            }
        }

        holder.getBukkitInventory().setItem(0, null);
        holder.getBukkitInventory().setItem(1, null);
        holder.getBukkitInventory().setItem(2, null);

        admin.updateInventory();

        session.setCurrentAnvilHolder(null);
        session.setTransferring(true);

        String step = session.getCurrentStep();
        GuiManager gm = plugin.getGuiManager();

        switch (step) {
            case GuiHolder.MENU_ANVIL_PLAYER_NAME:
                if (text == null || text.trim().isEmpty()) {
                    session.setTransferring(false);
                    admin.sendMessage(MessageUtils.colorize("&cPlayer name is required for offline punishment."));
                    plugin.getGuiManager().endSession(admin);
                    return;
                }
                if (!OverallBans.utils.UsernameValidator.isValid(text.trim())) {
                    session.setTransferring(false);
                    admin.sendMessage(MessageUtils.colorize("&c'" + text.trim() + "' is not a valid Minecraft username."));
                    admin.sendMessage(MessageUtils.colorize("&7" + OverallBans.utils.UsernameValidator.getRules()));
                    new AnvilInputMenu(gm).openPlayerName(admin);
                    return;
                }
                session.setOfflineTarget(text.trim());
                session.setTransferring(false);
                new PunishTypeMenu(gm).open(admin);
                break;
            case GuiHolder.MENU_ANVIL_CHECK_PLAYER:
                if (text == null || text.trim().isEmpty()) {
                    session.setTransferring(false);
                    admin.sendMessage(MessageUtils.colorize("&cPlayer name is required for check."));
                    new AnvilInputMenu(gm).openCheckPlayerName(admin);
                    return;
                }
                if (!OverallBans.utils.UsernameValidator.isValid(text.trim())) {
                    session.setTransferring(false);
                    admin.sendMessage(MessageUtils.colorize("&c'" + text.trim() + "' is not a valid Minecraft username."));
                    admin.sendMessage(MessageUtils.colorize("&7" + OverallBans.utils.UsernameValidator.getRules()));
                    new AnvilInputMenu(gm).openCheckPlayerName(admin);
                    return;
                }
                String checkName = text.trim();
                Player onlineTarget = Bukkit.getPlayerExact(checkName);
                String checkUuid = null;
                String checkIp = null;
                boolean isOnline = (onlineTarget != null);
                if (onlineTarget != null) {
                    checkUuid = onlineTarget.getUniqueId().toString();
                    checkIp = onlineTarget.getAddress() == null ? null
                            : onlineTarget.getAddress().getAddress().getHostAddress();
                } else {
                    try {
                        OfflinePlayer offline = Bukkit.getOfflinePlayer(checkName);
                        if (offline != null && offline.getUniqueId() != null) {
                            checkUuid = offline.getUniqueId().toString();
                        }
                    } catch (Throwable t) {}
                }
                session.setCheckTarget(checkName, checkUuid, checkIp, isOnline);
                session.setTransferring(false);
                new CheckMenu(gm, plugin).open(admin);
                break;
            case GuiHolder.MENU_ANVIL_TIME:
                session.setTimeInput(text);
                new AnvilInputMenu(gm).openWhomuted(admin);
                break;
            case GuiHolder.MENU_ANVIL_WHOMUTED:
                session.setWhomutedInput(text);
                new AnvilInputMenu(gm).openReason(admin);
                break;
            case GuiHolder.MENU_ANVIL_REASON:
                session.setReasonInput(text);
                session.setTransferring(false);
                new ConfirmMenu(gm).open(admin);
                break;
        }
    }

    private void startFieldCollection(Player admin, GuiSession session) {
        if (session.isNeedsTime()) new AnvilInputMenu(plugin.getGuiManager()).openTime(admin);
        else new AnvilInputMenu(plugin.getGuiManager()).openWhomuted(admin);
    }

    @SuppressWarnings("deprecation")
    private Player resolvePlayerFromHead(ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return null;
        ItemMeta meta = clicked.getItemMeta();
        if (!(meta instanceof SkullMeta)) return null;
        SkullMeta skull = (SkullMeta) meta;
        String owner = skull.getOwner();
        if (owner == null || owner.isEmpty()) return null;
        return Bukkit.getPlayerExact(owner);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player admin = (Player) event.getPlayer();

        GuiSession session = plugin.getGuiManager().getSession(admin);
        if (session == null) return;

        if (session.isTransferring()) return;

        if (session.getCurrentAnvilHolder() != null) {
            AnvilHolder holder = session.getCurrentAnvilHolder();
            try {
                holder.getBukkitInventory().setItem(0, null);
                holder.getBukkitInventory().setItem(1, null);
                holder.getBukkitInventory().setItem(2, null);
                admin.updateInventory();
            } catch (Throwable t) {}
            session.setCurrentAnvilHolder(null);
            plugin.getGuiManager().endSession(admin);
            admin.sendMessage(MessageUtils.colorize("&cPunishment flow cancelled: anvil closed."));
            return;
        }

        Inventory top = event.getInventory();
        if (top != null) {
            InventoryHolder holder = top.getHolder();
            if (holder instanceof GuiHolder) {
                GuiHolder gh = (GuiHolder) holder;
                if (gh.getMenuType().equals(GuiHolder.MENU_MAIN) ||
                    gh.getMenuType().equals(GuiHolder.MENU_PLAYER_LIST)) {
                    plugin.getGuiManager().endSession(admin);
                    return;
                }
            }
        }

        plugin.getGuiManager().endSession(admin);
        admin.sendMessage(MessageUtils.colorize("&cPunishment flow cancelled: menu closed."));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player admin = (Player) event.getWhoClicked();

        GuiSession session = plugin.getGuiManager().getSession(admin);
        if (session == null) return;

        Inventory top = event.getView().getTopInventory();
        if (top == null) return;
        InventoryHolder holder = top.getHolder();
        if (holder instanceof GuiHolder || session.getCurrentAnvilHolder() != null) {
            for (int slot : event.getRawSlots()) {
                if (slot < top.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void executePunishment(Player admin, GuiSession session) {
        String typeKey = session.getPunishmentTypeKey();
        String targetName = session.getTargetName();
        String time = session.isTimeExplicit() ? session.getTimeInput() : "";
        String whomuted = session.isWhomutedExplicit() ? session.getWhomutedInput() : "";
        String reason = session.isReasonExplicit() ? session.getReasonInput() : "";

        if (session.isNeedsTime() && time.isEmpty()) {
            admin.sendMessage(MessageUtils.colorize(
                    "&cThis punishment type requires a time. Please start over."));
            plugin.getGuiManager().endSession(admin);
            admin.closeInventory();
            return;
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(typeKey).append(" ").append(targetName);
        if (session.isNeedsTime()) cmd.append(" ").append(time);
        if (!whomuted.isEmpty()) cmd.append(" by:").append(whomuted);
        if (!reason.isEmpty()) cmd.append(" ").append(reason);

        String dispatch = cmd.toString();

        session.setTransferring(true);
        plugin.getGuiManager().endSession(admin);
        plugin.getServer().dispatchCommand(admin, dispatch);
        admin.closeInventory();
    }
}
