package OverallBans.gui;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {

    private final Map<UUID, GuiSession> sessions = new ConcurrentHashMap<>();

    public GuiSession startSession(Player admin) {
        GuiSession session = new GuiSession(admin.getUniqueId());
        sessions.put(admin.getUniqueId(), session);
        return session;
    }

    public GuiSession getSession(Player admin) { return sessions.get(admin.getUniqueId()); }
    public GuiSession getSession(UUID adminUuid) { return sessions.get(adminUuid); }
    public void endSession(Player admin) { sessions.remove(admin.getUniqueId()); }
    public void endSession(UUID adminUuid) { sessions.remove(adminUuid); }
    public boolean hasSession(Player admin) { return sessions.containsKey(admin.getUniqueId()); }
    public void clearAll() { sessions.clear(); }
    public int activeCount() { return sessions.size(); }
}
