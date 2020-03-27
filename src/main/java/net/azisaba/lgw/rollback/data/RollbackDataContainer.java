package net.azisaba.lgw.rollback.data;

import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class RollbackDataContainer {

    private HashMap<UUID, RollbackData> dataMap = new HashMap<>();

    public RollbackData getRollbackData(UUID uuid) {
        return dataMap.getOrDefault(uuid, null);
    }

    public boolean hasData(UUID uuid) {
        return new File(".").toPath().resolve("plugins/RollbackWeapons/PlayerData/" + uuid.toString() + ".yml").toFile().exists();
    }

    public void loadRollbackData(UUID uuid) {
        if (dataMap.containsKey(uuid)) {
            return;
        }
        if (!hasData(uuid)) {
            return;
        }
        RollbackData data = new RollbackData(uuid);
        data.load();
        dataMap.put(uuid, data);
    }

    public void saveAllData() {
        dataMap.values().forEach(RollbackData::save);
    }

    public void saveAndUnloadOfflineData() {
        for (UUID uuid : new ArrayList<>(dataMap.keySet())) {
            if (!Bukkit.getOfflinePlayer(uuid).isOnline()) {
                boolean success = dataMap.get(uuid).save();
                if (success) {
                    dataMap.remove(uuid);
                }
            }
        }
    }
}
