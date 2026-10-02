package OverallBans.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class GuiHolder implements InventoryHolder {

    public static final String MENU_MAIN = "main";
    public static final String MENU_PLAYER_LIST = "player_list";
    public static final String MENU_PUNISH_TYPE = "punish_type";
    public static final String MENU_CONFIRM = "confirm";
    public static final String MENU_CHECK = "check";
    public static final String MENU_ANVIL_TIME = "anvil_time";
    public static final String MENU_ANVIL_WHOMUTED = "anvil_whomuted";
    public static final String MENU_ANVIL_REASON = "anvil_reason";
    public static final String MENU_ANVIL_PLAYER_NAME = "anvil_player_name";
    public static final String MENU_ANVIL_CHECK_PLAYER = "anvil_check_player";

    private final String menuType;
    private final UUID sessionId;
    private Inventory inventory;

    public GuiHolder(String menuType, UUID sessionId) {
        this.menuType = menuType;
        this.sessionId = sessionId;
    }

    public String getMenuType() { return menuType; }
    public UUID getSessionId() { return sessionId; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    @Override
    public Inventory getInventory() { return inventory; }
}
